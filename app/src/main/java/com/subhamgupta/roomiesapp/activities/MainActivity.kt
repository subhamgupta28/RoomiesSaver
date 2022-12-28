package com.subhamgupta.roomiesapp.activities

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import androidx.work.*
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.messaging.FirebaseMessaging
import com.subhamgupta.roomiesapp.HomeToMainLink
import com.subhamgupta.roomiesapp.R
import com.subhamgupta.roomiesapp.adapter.ViewPagerAdapter
import com.subhamgupta.roomiesapp.data.viewmodels.MainViewModel
import com.subhamgupta.roomiesapp.databinding.*
import com.subhamgupta.roomiesapp.fragments.*
import com.subhamgupta.roomiesapp.utils.AuthState
import com.subhamgupta.roomiesapp.utils.FirebaseState
import com.subhamgupta.roomiesapp.utils.SettingDataStore
import com.subhamgupta.roomiesapp.utils.Worker
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
    private lateinit var rationFragment: RationFragment
    private lateinit var settingDataStore: SettingDataStore
    private var demoStarted: Boolean = true


//    @Inject
//    lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        val i = Intent(this, MainActivity::class.java)
//        i.putExtra("crash", true)
//        i.flags.apply {
//            Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
//        }
//        val pendingIntent = PendingIntent.getActivity(
//            this,
//            0,
//            i,
//            FLAG_IMMUTABLE
//        )
//        Thread.setDefaultUncaughtExceptionHandler(Handler(this, application, pendingIntent))
        init()
    }

    private fun init() {
//        viewModel.getData()


        checkUser()

        settingDataStore = viewModel.getDataStore()
        binding.tablayout.setupWithViewPager(binding.viewpager1)


        if (intent.getBooleanExtra("crash", false)) {
            Toast.makeText(this, "Sorry for inconvenience.", Toast.LENGTH_LONG).show()
        }


        val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager, 0)
        diffUser = DiffUser()
        rationFragment = RationFragment()
        viewPagerAdapter.addFragments(HomeFragment(this@MainActivity), "Home")
        viewPagerAdapter.addFragments(diffUser, "Roomie Expenses")
        viewPagerAdapter.addFragments(Summary(), "All Expenses")

        val badgeDrawable = BadgeDrawable.create(this)
        badgeDrawable.isVisible = true
//        BadgeUtils.attachBadgeDrawable(badgeDrawable, binding.addItem)

        lifecycleScope.launchWhenStarted {

            viewModel.userData.buffer().collect{
                if(it["ROLE"]!=null){
                    val feature = it["ROLE"] as MutableMap<String, Any>
                    if (feature["BETA_ENABLED"].toString().toBoolean()){
                        viewPagerAdapter.addFragments(rationFragment, "Stored Items")
                        viewPagerAdapter.notifyDataSetChanged()
                    }
                }
            }
        }
//        viewPagerAdapter.addFragments(AnalyticsFragment(), "Analytics")
//        viewPagerAdapter.addFragments(MyNotesFragment(), "All Rooms")
        viewModel.addItem.observe(this) {
            if (it) {
                showSnackBar("Item Added")
                viewModel.getDiffData()
//                binding.viewpager1.setCurrentItem(1, true)
//                diffUser.goToUser(0, viewModel.getUser()?.uid!!)
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
                    binding.extendedMenu.visibility = View.GONE
                }
                binding.addItem.setOnClickListener {
                    if (position == 3)
                        rationFragment.openSheet()
                    else
                        addItems()
                }
            }

            override fun onPageSelected(position: Int) {

            }

        })
//        binding.addItem.setOnClickListener {
//            addItems()
//        }



        lifecycleScope.launch(Dispatchers.IO) {

            viewModel.roomDetail.collectLatest {
                withContext(Main) {
                    when (it) {
                        is FirebaseState.Loading -> {
                            Log.e("loading", "loading")
                        }
                        is FirebaseState.Failed -> {
                            binding.progress.visibility = View.GONE
                            Log.e("loading", "failed")
                            showSnackBar("Something went wrong")
                        }
                        is FirebaseState.Success -> {
                            Log.e("loading", "success")
                            binding.idText.text = it.data.ROOM_NAME.toString()
                            binding.progress.visibility = View.GONE
                            if (demoStarted and !settingDataStore.getDemo()) {
                                showDemo()
                                demoStarted = false
                            }
                        }
                        is FirebaseState.Empty ->{
                        }
                    }

                }

            }
        }
        binding.alertCancel.setOnClickListener {
            hideExtendMenu(false)
        }
        lifecycleScope.launchWhenStarted {
            viewModel.alert.buffer().collect { alert ->
                Log.e("ALERT","$alert")
                if (!alert.IS_COMPLETED) {
                    hideExtendMenu(true)
                    binding.alertText.text = alert.ALERT
                }
            }
        }

//        netStat()
        setupClickListener()
//        initializeWorker()
    }
    private fun hideExtendMenu(isVisible: Boolean){
        TransitionManager.beginDelayedTransition(binding.l, ChangeBounds())
        binding.extendedMenu.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    private fun initializeWorker() {
        val workManager = WorkManager.getInstance(this)
        val constraint = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodicWorkRequest = PeriodicWorkRequestBuilder<Worker>(6, TimeUnit.HOURS)
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
        viewModel.initializeStore()
        lifecycleScope.launchWhenStarted {
            viewModel.authState.buffer().collect{
                when(it){
                    is AuthState.NoUserOrError -> {
                        withContext(Main){
                            showSnackBar(it.message.toString())
                            viewModel.clearStorage()
                            startActivity(Intent(this@MainActivity, StartActivity::class.java))
                            finish()
                        }
                    }
                    is AuthState.Loading -> {
//                        loadingDialog.dismiss()

                    }
                    is AuthState.LoggedIn -> {
//                        loadingDialog.dismiss()
                        viewModel.userData.collectLatest {
                            Log.e("CHECK USER", "$it")
                            val fragment = RoomCreation()
                            if (it.isNotEmpty() && !it["IS_ROOM_JOINED"].toString().toBoolean()) {
                                Log.e("room", "true")
                                withContext(Main) {
                                    binding.mainLayout.visibility = View.GONE
                                    binding.l.visibility = View.GONE
                                    binding.settingFragment.visibility = View.VISIBLE
                                    supportFragmentManager.beginTransaction()
                                        .add(R.id.setting_fragment, fragment)
                                        .commit()
                                }
                                settingDataStore.setUpdate(true)
                            }else{
                                Log.e("room", "false")
                                withContext(Main) {
                                    binding.mainLayout.visibility = View.VISIBLE
                                    binding.l.visibility = View.VISIBLE
                                    binding.settingFragment.visibility = View.GONE
                                    supportFragmentManager.beginTransaction().remove(fragment)
                                        .commit()
                                }
                            }
                        }
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
                TapTarget.forView(findViewById(R.id.logout_btn), "Logout", "Logout.")
                    .dimColor(R.color.colorOnSecondary)
                    .titleTextSize(25)
                    .outerCircleColor(R.color.colorRed)
                    .targetCircleColor(R.color.colorSecondary)
                    .tintTarget(false)
                    .textColor(R.color.colorOnPrimary),
                TapTarget.forView(
                    findViewById(R.id.setting_btn),
                    "Setting",
                    "Change app settings, join or create rooms etc."
                )
                    .dimColor(R.color.colorOnSecondary)
                    .titleTextSize(25)
                    .outerCircleColor(R.color.colorRed)
                    .targetCircleColor(R.color.colorSecondary)
                    .tintTarget(false)
                    .textColor(R.color.colorOnPrimary),
                TapTarget.forView(
                    findViewById(R.id.change_room),
                    "Change room",
                    "Select from list of rooms to enter."
                )
                    .dimColor(R.color.colorOnSecondary)
                    .titleTextSize(25)
                    .outerCircleColor(R.color.colorRed)
                    .targetCircleColor(R.color.colorSecondary)
                    .tintTarget(false)
                    .textColor(R.color.colorOnPrimary),
                TapTarget.forView(
                    findViewById(R.id.alert_btn),
                    "Add announcement",
                    "Announce to all members of the room about things."
                )
                    .dimColor(R.color.colorOnSecondary)
                    .titleTextSize(25)
                    .outerCircleColor(R.color.colorRed)
                    .targetCircleColor(R.color.colorSecondary)
                    .tintTarget(false)
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




    private fun logOut() {
        materialAlertDialogBuilder = MaterialAlertDialogBuilder(this)
        materialAlertDialogBuilder.setTitle("Do you want to logout?")
        materialAlertDialogBuilder.setNegativeButton("Cancel") { _, _ ->

        }
        lifecycleScope.launch {

            val id = settingDataStore.getRoomKey()
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
            when (it.itemId) {
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
//            binding.extendedMenu.visibility = if (binding.extendedMenu.isVisible) View.GONE else View.VISIBLE
//            TransitionManager.beginDelayedTransition(binding.extendedMenu, Fade())
            logOut()
        }
        binding.settingBtn.setOnClickListener {
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
                viewModel.fetchAlert()
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
                    if (!isNumeric(charSequence.toString())) "Only numbers are allowed" else ""
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
                    if (!isNumeric(dialogBinding.amountPaid.text.toString())) {
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
                        dialogBinding.amountLayout.error = ""
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

    fun isNumeric(toCheck: String): Boolean {
        val regex = "-?[0-9]+(\\.[0-9]+)?".toRegex()
        return toCheck.matches(regex)
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
//                                showSnackBar("Fetching selected room data...")
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
        val snackBarView = Snackbar.make(binding.root, msg , Snackbar.LENGTH_LONG)
        val view = snackBarView.view
        val params = view.layoutParams as FrameLayout.LayoutParams
        params.gravity = Gravity.TOP
        view.layoutParams = params
        snackBarView.animationMode = BaseTransientBottomBar.ANIMATION_MODE_FADE
        snackBarView.setBackgroundTint(resources.getColor(R.color.colorSecondary))
            .setTextColor(resources.getColor(R.color.colorOnSecondary))
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

//    private fun loadingDialog(){
//        materialAlertDialogBuilder = MaterialAlertDialogBuilder(this)
//        val view = LoadingPopupBinding.inflate(layoutInflater)
//        materialAlertDialogBuilder.setView(view.root)
//        materialAlertDialogBuilder.background = ColorDrawable(Color.TRANSPARENT)
//        loadingDialog = materialAlertDialogBuilder.show()
//        loadingDialog.setCancelable(false)
//    }


}

