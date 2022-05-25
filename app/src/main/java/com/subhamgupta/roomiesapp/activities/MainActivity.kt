package com.subhamgupta.roomiesapp.activities


import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.messaging.FirebaseMessaging
import com.subhamgupta.roomiesapp.HomeToMainLink
import com.subhamgupta.roomiesapp.R
import com.subhamgupta.roomiesapp.adapter.ViewPagerAdapter
import com.subhamgupta.roomiesapp.data.viewmodels.FirebaseViewModel
import com.subhamgupta.roomiesapp.databinding.*
import com.subhamgupta.roomiesapp.fragments.DiffUser
import com.subhamgupta.roomiesapp.fragments.HomeFragment
import com.subhamgupta.roomiesapp.fragments.Summary
import com.subhamgupta.roomiesapp.utils.FirebaseState
import com.subhamgupta.roomiesapp.utils.SettingsStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity(), HomeToMainLink {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: FirebaseViewModel by viewModels()
    private lateinit var settingsStorage: SettingsStorage
    private lateinit var materialAlertDialogBuilder: MaterialAlertDialogBuilder
    private lateinit var diffUser: DiffUser
    private lateinit var loadingDismiss: AlertDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.lifecycleOwner = this

        settingsStorage = viewModel.getSettings()

        binding.tablayout.setupWithViewPager(binding.viewpager1)

        val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager, 0)
        diffUser = DiffUser()
        viewModel.getData()
//        viewPagerAdapter.addFragments(MyNotesFragment(), "My Notes")
        viewPagerAdapter.addFragments(HomeFragment(this@MainActivity), "Home")
        viewPagerAdapter.addFragments(diffUser, "Roomie Expenses")
        viewPagerAdapter.addFragments(Summary(), "All Expenses")
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
        val bottomBarBackground = binding.bottombar.background as MaterialShapeDrawable
        bottomBarBackground.shapeAppearanceModel = bottomBarBackground.shapeAppearanceModel
            .toBuilder()
            .setTopRightCorner(CornerFamily.ROUNDED, 30f)
            .setTopLeftCorner(CornerFamily.ROUNDED, 30f)
            .build()

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

        binding.floatingbtn.setOnClickListener {
            addItems()
        }
        viewModel.fetchAlert()


        lifecycleScope.launch(Dispatchers.IO) {

            viewModel.roomDetail.buffer().collect {
                withContext(Main) {
                    when (it) {
                        is FirebaseState.Loading -> {
                            binding.progress.visibility = View.VISIBLE
                        }
                        is FirebaseState.Failed -> {
                            binding.progress.visibility = View.GONE
                            showSnackBar("Something went wrong")
                        }
                        is FirebaseState.Success -> {
                            binding.idText.text = it.data.ROOM_NAME.toString()
                            binding.progress.visibility = View.GONE
                            lifecycleScope.launchWhenStarted {
                                viewModel.alert.buffer().collect { alert ->
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
        netStat()
        setupClickListener()

    }

    private fun netStat() {
        val connectivityManager = this.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
        } else {
            showSnackBar("No Internet")
        }
    }

    private fun logOut() {
        materialAlertDialogBuilder = MaterialAlertDialogBuilder(this)
        materialAlertDialogBuilder.setTitle("Do you want to logout?")
        materialAlertDialogBuilder.setNegativeButton("Cancel") { _, _ ->

        }
        materialAlertDialogBuilder.setPositiveButton("Logout") { _, _ ->
            FirebaseMessaging.getInstance()
                .unsubscribeFromTopic("/topics/${settingsStorage.room_id.toString()}")
            settingsStorage.clear()
            viewModel.logout()
            startActivity(Intent(this@MainActivity, StartActivity::class.java))
            finish()
        }
        materialAlertDialogBuilder.show()
    }

    private fun setupClickListener() {
        binding.bottombar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.logout -> {
                    logOut()
                    true
                }
                R.id.info -> {
                    startActivity(Intent(this@MainActivity, SettingActivity::class.java))
                    true
                }
                R.id.changeRoom -> {
                    changeRoom()
                    true
                }
                R.id.alert -> {
                    newAlert()
                    true
                }
                else -> false
            }
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
                        viewModel.addItem(
                            dialogBinding.itemBought.text.toString().trim(),
                            dialogBinding.amountPaid.text.toString().trim()
                        )
                        dialogBinding.amountPaid.text = null
                        dialogBinding.saveBtn.isEnabled = true
                        dialogBinding.itemBought.text = null
                        viewModel.sendNotification(
                            "New Buying",
                            dialogBinding.itemBought.text.toString()
                        )
                    }
                } catch (e: Exception) {
                }
            }

        }

        materialAlertDialogBuilder.setView(dialogBinding.root)
        materialAlertDialogBuilder.background = ColorDrawable(Color.TRANSPARENT)
        materialAlertDialogBuilder.show()
    }

    private fun loading(isVisible: Boolean) {
        if (isVisible) {
            materialAlertDialogBuilder = MaterialAlertDialogBuilder(this)
            val dialogBinding = LoadingPopupBinding.inflate(layoutInflater)
            materialAlertDialogBuilder.setView(dialogBinding.root)
            materialAlertDialogBuilder.background = ColorDrawable(Color.TRANSPARENT)
            loadingDismiss = materialAlertDialogBuilder.show()
        }
    }

    private fun changeRoom() {
        materialAlertDialogBuilder = MaterialAlertDialogBuilder(this)
        val binding = ChangeRoomCardBinding.inflate(layoutInflater)


        binding.allRoomChip.isSingleSelection = true
        val temp = viewModel.getTempRoomMaps()
        materialAlertDialogBuilder.setView(binding.root)
        materialAlertDialogBuilder.background = ColorDrawable(Color.TRANSPARENT)
        val dialog = materialAlertDialogBuilder.show()
        viewModel.getRoomMap().observe(this) { map ->
            binding.allRoomChip.removeAllViews()
            map.forEach { (k, v) ->
                val chip = Chip(this)
                chip.text = k
                chip.isCheckable = true
                chip.isCheckedIconVisible = true
                if (v == viewModel.getSettings().roomRef) {
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
                            settingsStorage.roomRef = roomId
                            lifecycleScope.launch {
                                viewModel.getDataStore().setRoomRef(roomId)
                                viewModel.fetchUserData()
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

}

