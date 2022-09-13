package com.subhamgupta.roomiesapp.activities


import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.transition.ChangeBounds
import android.transition.Explode
import android.transition.Fade
import android.transition.TransitionManager
import android.util.Log
import android.view.*
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import androidx.work.*
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.google.android.material.chip.Chip
import com.google.android.material.color.DynamicColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.messaging.FirebaseMessaging
import com.subhamgupta.roomiesapp.HomeToMainLink
import com.subhamgupta.roomiesapp.R
import com.subhamgupta.roomiesapp.adapter.ViewPagerAdapter

import com.subhamgupta.roomiesapp.utils.Worker
import com.subhamgupta.roomiesapp.data.viewmodels.MainViewModel

import com.subhamgupta.roomiesapp.databinding.ActivityMainBinding
import com.subhamgupta.roomiesapp.databinding.AlertPopupBinding
import com.subhamgupta.roomiesapp.databinding.ChangeRoomCardBinding
import com.subhamgupta.roomiesapp.databinding.PopupBinding

import com.subhamgupta.roomiesapp.fragments.*

import com.subhamgupta.roomiesapp.utils.FirebaseState
import com.subhamgupta.roomiesapp.utils.Handler
import com.subhamgupta.roomiesapp.utils.SettingDataStore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), HomeToMainLink {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var materialAlertDialogBuilder: MaterialAlertDialogBuilder
    private lateinit var diffUser: DiffUser
    private lateinit var roomRef: String
    private lateinit var settingDataStore: SettingDataStore

    @Inject
    lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        viewModel.getData()
        checkUser()
//        viewModel.clearStorage()
        settingDataStore = viewModel.getDataStore()
        binding.tablayout.setupWithViewPager(binding.viewpager1)
        val i = Intent(this, MainActivity::class.java)
        i.putExtra("crash", true)
        i.flags.apply {
            Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            i,
            FLAG_IMMUTABLE
        )
        Thread.setDefaultUncaughtExceptionHandler(Handler(this, application, pendingIntent))

        if (intent.getBooleanExtra("crash", false)) {
            Toast.makeText(this, "Sorry for inconvenience.", Toast.LENGTH_LONG).show()
        }


        val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager, 0)
        diffUser = DiffUser()
//        viewPagerAdapter.addFragments(MyNotesFragment(), "My Notes")
        viewPagerAdapter.addFragments(HomeFragment(this@MainActivity), "Home")
        viewPagerAdapter.addFragments(diffUser, "Roomie Expenses")
        viewPagerAdapter.addFragments(Summary(), "All Expenses")
//        viewPagerAdapter.addFragments(AnalyticsFragment(), "Analytics")
//        viewPagerAdapter.addFragments(RationFragment(), "Items")
        viewModel.addItem.observe(this) {
            if (it) {
                showSnackBar("Item Added")
                viewModel.getDiffData()
                binding.viewpager1.setCurrentItem(1, true)
                diffUser.goToUser(0, viewModel.getUser()?.uid!!)
            }
        }

        binding.viewpager1.offscreenPageLimit = 3
        binding.viewpager1.adapter = viewPagerAdapter

        binding.viewpager1.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                if (position == 0) {
                    runTransition(binding.tablayout, false)
                } else {
                    runTransition(binding.tablayout, true)
                }
            }

            override fun onPageSelected(position: Int) {

            }

        })
        binding.addItem.setOnClickListener {
            addItems()
        }
        viewModel.fetchAlert()
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.roomDetail.collectLatest {
                withContext(Main) {
                    when (it) {
                        is FirebaseState.Loading -> {
//                            binding.progress.visibility = View.VISIBLE
                        }
                        is FirebaseState.Failed -> {
                            binding.progress.visibility = View.GONE
                            showSnackBar("Something went wrong")
                        }
                        is FirebaseState.Success -> {
                            binding.idText.text = it.data.ROOM_NAME.toString()
                            binding.progress.visibility = View.GONE
                            lifecycleScope.launchWhenStarted {
                                viewModel.alert.collectLatest { alert ->
                                    if (alert.IS_COMPLETED == false) {
                                        binding.alertLayout.visibility = View.VISIBLE
                                        binding.alertText.text = alert.ALERT
                                        binding.alertCancel.setOnClickListener {
                                            binding.alertLayout.visibility = View.GONE
                                        }
                                    }

                                }
                            }
                        }
                        else -> Unit
                    }

                }

            }
        }
        lifecycleScope.launch {
            if (!settingDataStore.getDemo()) {
                showDemo()
            }
        }

//        netStat()
        setupClickListener()
        initializeWorker()
    }

    private fun initializeWorker() {
        val workManager = WorkManager.getInstance(this)
        val constraint = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodicWorkRequest = PeriodicWorkRequestBuilder<Worker>(1, TimeUnit.HOURS)
            .addTag("Periodic")
            .setConstraints(constraint)
            .build()
        workManager.enqueueUniquePeriodicWork(
            "update",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWorkRequest
        )
    }

    private fun checkUser() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            viewModel.clearStorage()
            startActivity(Intent(this, StartActivity::class.java))
            finish()
        } else {
            viewModel.getData()
            lifecycleScope.launchWhenStarted {
                viewModel.userData.collectLatest {
                    Log.e("CHECK USER", "$it")
                    if (it.isNotEmpty() && !it["IS_ROOM_JOINED"].toString().toBoolean()) {
                        withContext(Main) {
                            binding.mainLayout.visibility = View.GONE
                            binding.settingFragment.visibility = View.VISIBLE
                            supportFragmentManager.beginTransaction()
                                .add(R.id.setting_fragment, RoomCreation())
                                .commit()
                        }
                        settingDataStore.setUpdate(true)
                    }
                }
            }
        }
    }

    private fun showDemo() {
        val tp = TapTargetSequence(this)
            .targets(
                TapTarget.forView(
                    binding.addItem,
                    "Add bought items",
                    "Add item which you have bought."
                )
                    .dimColor(R.color.colorOnSecondary)
                    .titleTextSize(25)
                    .outerCircleColor(R.color.colorRed)
                    .targetCircleColor(R.color.colorSecondary)
                    .tintTarget(false)
                    .textColor(R.color.colorOnPrimary),
                TapTarget.forView(findViewById(R.id.logout), "Logout", "Logout.")
                    .dimColor(R.color.colorOnSecondary)
                    .titleTextSize(25)
                    .outerCircleColor(R.color.colorRed)
                    .targetCircleColor(R.color.colorSecondary)
                    .textColor(R.color.colorOnPrimary),
                TapTarget.forView(
                    findViewById(R.id.info),
                    "Setting",
                    "Change app settings, join or create rooms etc."
                )
                    .dimColor(R.color.colorOnSecondary)
                    .titleTextSize(25)
                    .outerCircleColor(R.color.colorRed)
                    .targetCircleColor(R.color.colorSecondary)
                    .textColor(R.color.colorOnPrimary),
                TapTarget.forView(
                    findViewById(R.id.changeRoom),
                    "Change room",
                    "Select from list of rooms to enter."
                )
                    .dimColor(R.color.colorOnSecondary)
                    .titleTextSize(25)
                    .outerCircleColor(R.color.colorRed)
                    .targetCircleColor(R.color.colorSecondary)
                    .textColor(R.color.colorOnPrimary),
                TapTarget.forView(
                    findViewById(R.id.alert),
                    "Add announcement",
                    "Announce to all members of the room about things."
                )
                    .dimColor(R.color.colorOnSecondary)
                    .titleTextSize(25)
                    .outerCircleColor(R.color.colorRed)
                    .targetCircleColor(R.color.colorSecondary)
                    .textColor(R.color.colorOnPrimary)
            )
            .listener(object : TapTargetSequence.Listener {
                // This listener will tell us when interesting(tm) events happen in regards
                // to the sequence
                override fun onSequenceFinish() {
                    // Yay
                    lifecycleScope.launch {
                        viewModel.getDataStore().setDemo(true)
                    }
                }

                override fun onSequenceStep(lastTarget: TapTarget, targetClicked: Boolean) {
                }

                override fun onSequenceCanceled(lastTarget: TapTarget) {
                    // Boo
                }
            })
        tp.start()
    }


    private fun netStat() {
        viewModel.getNetworkObserver().observe().onEach {
            showSnackBar("Network ${it.name}")

        }.launchIn(lifecycleScope)
    }

    private fun logOut() {
        materialAlertDialogBuilder = MaterialAlertDialogBuilder(this)
        materialAlertDialogBuilder.setTitle("Do you want to logout?")
        materialAlertDialogBuilder.setNegativeButton("Cancel") { _, _ ->

        }
        lifecycleScope.launch {

            val id = settingDataStore.getRoomKey().toString()
            materialAlertDialogBuilder.setPositiveButton("Logout") { _, _ ->
                viewModel.logout()
                FirebaseMessaging.getInstance()
                    .unsubscribeFromTopic("/topics/${id}")
                startActivity(Intent(this@MainActivity, StartActivity::class.java))
                finish()
            }
        }

        materialAlertDialogBuilder.show()
    }
    private fun showMenu(v: View) {
        val popup = PopupMenu(this, v)
        popup.menuInflater.inflate(R.menu.main_menu, popup.menu)
        popup.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.menu_alert -> {
                    newAlert()
                }
                R.id.menu_logout -> {
                    logOut()
                }
                R.id.menu_setting -> {
                    startActivity(Intent(this@MainActivity, SettingActivity::class.java))
                }
                R.id.menu_change_room -> {
                    changeRoom()
                }
            }
            true
        }
        popup.setOnDismissListener {
            // Respond to popup being dismissed.
        }
        popup.show()
    }

    private fun setupClickListener() {
        binding.logoutBtn.setOnClickListener {
            logOut()
        }
        binding.settingBtn.setOnClickListener{
            startActivity(Intent(this@MainActivity, SettingActivity::class.java))
        }
        binding.changeRoom.setOnClickListener {
            changeRoom()
        }
        binding.alertBtn.setOnClickListener {
            newAlert()
        }
    }

    private fun newAlert() {
        materialAlertDialogBuilder = MaterialAlertDialogBuilder(this)
        val binding = AlertPopupBinding.inflate(layoutInflater)
        val alert_et = binding.alertEt.text
        binding.alertBtn.setOnClickListener {
            if (alert_et.toString() != "") {
                viewModel.createAlert(alert_et.toString())
            }
        }
        materialAlertDialogBuilder.setView(binding.root)
        materialAlertDialogBuilder.background = ColorDrawable(Color.TRANSPARENT)
        materialAlertDialogBuilder.show()
    }


    private fun addItems() {
        materialAlertDialogBuilder = MaterialAlertDialogBuilder(this)

        val dialogBinding = PopupBinding.inflate(layoutInflater)

        dialogBinding.amountPaid.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                dialogBinding.amountLayout.error =
                    if (!TextUtils.isDigitsOnly(charSequence)) "Only numbers are allowed" else ""
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        dialogBinding.itemBought.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {
                val text = dialogBinding.itemBought.text.toString().split(" ")
                dialogBinding.tags.removeAllViews()
                text.forEach {
                    val chip = Chip(this@MainActivity)
                    chip.text = it
                    chip.isCheckable = true
                    chip.chipStrokeWidth = 0F
                    chip.isCheckedIconVisible = true
                    dialogBinding.tags.addView(chip)
                }
            }
        })
        var category = "Food"

        dialogBinding.category.children.forEach { view ->
            (view as Chip).setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    category = view.text.toString()
                }
            }
        }
        dialogBinding.saveBtn.setOnClickListener {
            if (dialogBinding.itemBought.text.toString()
                    .isEmpty() || dialogBinding.amountPaid.text.toString()
                    .isEmpty()
            ) showSnackBar("Enter all details") else {
                try {
                    if (!TextUtils.isDigitsOnly(dialogBinding.amountPaid.text.toString())) {
                        dialogBinding.amountLayout.error = "Only numbers are allowed"
                    } else {
                        dialogBinding.saveBtn.isEnabled = false
                        val text = dialogBinding.itemBought.text.toString().trim()
                        val amount = dialogBinding.amountPaid.text.toString().trim()
                        val note = dialogBinding.noteText.text.toString().trim()
                        val tags = ArrayList<String>()
                        dialogBinding.tags.children.forEach { view ->
                            if ((view as Chip).isChecked) {
                                tags.add(view.text.toString())
                            }
                        }
                        Log.e("ADD", "$category $tags $text $amount")
                        viewModel.addItem(
                            item = text,
                            amount = amount,
                            note = note,
                            tags = tags,
                            category = category
                        )

                        viewModel.sendNotification(
                            "bought $text for $amount",
                            "Go to app to see details"
                        )

                        dialogBinding.tags.removeAllViews()
                        dialogBinding.noteText.text = null
                        dialogBinding.amountPaid.text = null
                        dialogBinding.saveBtn.isEnabled = true
                        dialogBinding.itemBought.text = null
                    }
                } catch (e: Exception) {
                    Log.e("ERROR MAIN", "${e.message}")
                }
            }

        }

        materialAlertDialogBuilder.setView(dialogBinding.root)
        materialAlertDialogBuilder.background = ColorDrawable(Color.TRANSPARENT)
        materialAlertDialogBuilder.show()
    }


    private fun changeRoom() {
        materialAlertDialogBuilder = MaterialAlertDialogBuilder(this)
        val binding = ChangeRoomCardBinding.inflate(layoutInflater)
        binding.allRoomChip.isSingleSelection = true
        val temp = viewModel.getTempRoomMaps()
        materialAlertDialogBuilder.setView(binding.root)
        materialAlertDialogBuilder.background = ColorDrawable(Color.TRANSPARENT)
        val dialog = materialAlertDialogBuilder.show()
        lifecycleScope.launch {
            roomRef = viewModel.getDataStore().getRoomRef()
        }
        viewModel.getRoomMap().observe(this) { map ->
            binding.allRoomChip.removeAllViews()
//            val list = map.keys.toMutableList()
//            adapter.setData(list)
            map.forEach { (k, v) ->
                val chip = Chip(this)
                chip.text = k
                chip.isCheckable = true
                chip.isCheckedIconVisible = true
                if (v == roomRef) {
                    chip.isChecked = true
                    chip.isSelected = true
                }

                binding.allRoomChip.addView(chip)
            }
            var roomId: String
            binding.allRoomChip.children.forEach { view ->
                (view as Chip).setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        roomId = map[view.text.toString()].toString()
                        "Enter ${temp.value?.get(roomId)}".also { i -> binding.roomEnter.text = i }
                        binding.roomEnter.setOnClickListener {
                            lifecycleScope.launch {
                                viewModel.getDataStore().setRoomRef(roomId)
                                viewModel.getData()
                                showSnackBar("Fetching selected room data...")
                                viewModel.getLoading.collect {
                                    if (!it)
                                        dialog.dismiss()
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    private fun showSnackBar(msg: String) {
        Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG)
            .setBackgroundTint(resources.getColor(R.color.colorSecondary))
            .setTextColor(resources.getColor(R.color.colorOnSecondary))
            .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
            .show()
    }

    private fun runTransition(view: ViewGroup, isVisible: Boolean) {
        TransitionManager.beginDelayedTransition(view, ChangeBounds())
        view.visibility = if (isVisible)
            View.VISIBLE
        else
            View.GONE
    }

    override fun goToMain(position: Int, uuid: String) {
        binding.viewpager1.setCurrentItem(1, true)
        diffUser.goToUser(position, uuid)
    }

    override fun goToAllExpenses() {
        binding.viewpager1.setCurrentItem(2, true)
    }

    override fun goToDiffUser() {
        binding.viewpager1.setCurrentItem(1, true)
    }


}

