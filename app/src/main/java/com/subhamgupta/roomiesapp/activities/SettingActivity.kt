package com.subhamgupta.roomiesapp.activities

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ShareCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.subhamgupta.roomiesapp.R
import com.subhamgupta.roomiesapp.data.database.SettingDataStore
import com.subhamgupta.roomiesapp.data.viewmodels.FirebaseViewModel
import com.subhamgupta.roomiesapp.databinding.ActivitySettingBinding
import com.subhamgupta.roomiesapp.databinding.EditDetailsBinding
import com.subhamgupta.roomiesapp.databinding.EditUserPopupBinding
import com.subhamgupta.roomiesapp.fragments.RoomCreation
import com.subhamgupta.roomiesapp.utils.Constant.Companion.TIME_STRING
import com.subhamgupta.roomiesapp.utils.FirebaseState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.*


class SettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingBinding
    private val viewModel: FirebaseViewModel by viewModels()
    private lateinit var materialAlertDialogBuilder: MaterialAlertDialogBuilder
    private lateinit var settingDataStore: SettingDataStore
    private lateinit var name: String
    private lateinit var limit: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.lifecycleOwner = this
        settingDataStore = viewModel.getDataStore()

        viewModel.startDate.observe(this) {
            if (it)
                showSnackBar("Start date of month changed")

        }
        viewModel.getRoomMates().observe(this@SettingActivity) {
            binding.chips.removeAllViews()
            it?.forEach {
                val chip = Chip(this@SettingActivity)
                chip.text = it.USER_NAME.toString().uppercase()
                chip.isCheckable = true
                chip.isChecked = true
                chip.isCheckedIconVisible = true
                binding.chips.addView(chip)
            }
        }
        viewModel.fetchUserData()
        lifecycleScope.launchWhenStarted {
            viewModel.roomDetail.collect() {
                withContext(Main) {
                    when (it) {
                        is FirebaseState.Loading -> {

                        }
                        is FirebaseState.Failed -> {
                            showSnackBar("Something went failed")
                        }
                        is FirebaseState.Success -> {
                            binding.rName.text = it.data.ROOM_NAME
                            binding.cDate.text = it.data.CREATED_ON
                            binding.limit.text = it.data.LIMIT.toString()
                            name = it.data.ROOM_NAME.toString()
                            limit = it.data.LIMIT.toString()
                            binding.rId.text = it.data.ROOM_ID
                            try {
                                val sdf = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
                                val netDate = Date(it.data.START_DATE_MONTH!!)
                                val date = sdf.format(netDate)
                                binding.setDText.text = date
                            } catch (e: Exception) {

                            }
                        }
                        else -> Unit
                    }

                }


            }

        }
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.sheetLoading.buffer().collect {
                when (it) {
                    is FirebaseState.Loading -> {
                        showSnackBar("Please wait generating excel sheet")
                    }
                    is FirebaseState.Failed -> {
                        showSnackBar("Something went failed")
                    }
                    is FirebaseState.Success -> {
                        if (it.data) {
                            showSnackBar("Excel sheet generated")
                        }
                    }
                    else -> Unit
                }
            }

        }
        init()
    }

    private fun init() {

        binding.leaveroom.setOnClickListener {
            leaveRoom()
        }
        binding.shareid.setOnClickListener {
            shareRoomId()
        }
        binding.setDate.setOnClickListener {
            setDateOfMonth()
        }
        binding.joinRoom.setOnClickListener {
            joinRoomPopUp()
        }
        binding.limitBtn.setOnClickListener {
            lifecycleScope.launch {
                viewModel.getDataStore().setUpdate(true)
            }
            editLimit()
        }
        binding.editProfile.setOnClickListener {
            editUserDetail()
        }

        binding.generateExcel.setOnClickListener {
            viewModel.generateSheet()
        }


        viewModel.getTotalAmount().observe(this) {
            "₹$it".also { binding.totalSpends.text = it }
        }
        viewModel.getTodayAmount().observe(this) {
            "₹$it".also { binding.thisMonthAmount.text = it }
        }




        lifecycleScope.launch {
            binding.nameText.text = settingDataStore.getUserName()
            binding.emailText.text = settingDataStore.getEmail()
            binding.noOfRoom.text = settingDataStore.getRoomCount().toString()
            binding.thisMonthData.isChecked = settingDataStore.isMonth()
            binding.darkModeSwitch.isChecked = settingDataStore.getDarkMode()
            binding.thisMonthData.setOnCheckedChangeListener { _, isCheck ->
                lifecycleScope.launch {
                    settingDataStore.setMonth(isCheck)
                }
            }
            binding.darkModeSwitch.setOnCheckedChangeListener { _, isCheck ->
                if (isCheck) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

                }
                lifecycleScope.launch {
                    settingDataStore.setDarkMode(isCheck)
                }

            }
            viewModel.userData.buffer().collect {
                withContext(Main) {
                    Log.e("URL","${it["IMG_URL"]}")
                    Glide.with(this@SettingActivity)
                        .load(it["IMG_URL"].toString())
                        .circleCrop()
                        .placeholder(R.drawable.ic_person)
                        .into(binding.profileImg)
                }
            }
        }


    }

    private fun selectImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(
                intent,
                "Select Image from here..."
            ),
            201
        )
    }

    var filePath: Uri? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 201 && resultCode == RESULT_OK && data != null && data.data != null) {
            val filePath = data.data
            this.filePath = filePath
        }
    }

    private fun editUserDetail() {
        materialAlertDialogBuilder = MaterialAlertDialogBuilder(this)
        val view = EditUserPopupBinding.inflate(layoutInflater)
        lifecycleScope.launch {
            viewModel.getDataStore().setUpdate(true)
        }
        view.selectImg.setOnClickListener {
            selectImage()
        }
        lifecycleScope.launch {
            viewModel.editUser.collect {
                if (it) {
                    withContext(Main){
                        showSnackBar("Saved")
                    }

                }
            }
        }
        view.saveBtn.setOnClickListener {
            if (filePath != null && view.userName.text.toString().isNotEmpty()) {
                viewModel.editUser(filePath!!, view.userName.text.toString())
            }
            else{
                showSnackBar("Enter details")
            }
        }
        materialAlertDialogBuilder.setView(view.root)
        materialAlertDialogBuilder.background = ColorDrawable(Color.TRANSPARENT)
        materialAlertDialogBuilder.show()
    }

    fun editLimit() {
        materialAlertDialogBuilder = MaterialAlertDialogBuilder(this)
        val view = EditDetailsBinding.inflate(layoutInflater)
        lifecycleScope.launch {
            viewModel.getDataStore().setUpdate(true)
        }
        view.rname.setText(name)
        view.rlimit.setText(limit)
        view.saveBtn.setOnClickListener {
            if (view.rname.text.toString().isNotEmpty() || view.rlimit.text.toString()
                    .isNotEmpty()
            ) {
                val map = HashMap<String, Any>()
                map["LIMIT"] = view.rlimit.text.toString().toInt()
                map["ROOM_NAME"] = view.rname.text.toString()

            }

        }
        materialAlertDialogBuilder.setView(view.root)
        materialAlertDialogBuilder.background = ColorDrawable(Color.TRANSPARENT)
        materialAlertDialogBuilder.show()

    }

    private fun joinRoomPopUp() {
        lifecycleScope.launch {
            viewModel.getDataStore().setUpdate(true)
        }
        binding.settingLayout.visibility = View.GONE
        binding.settingFragment.visibility = View.VISIBLE
        supportFragmentManager.beginTransaction()
            .add(R.id.setting_fragment, RoomCreation())
            .commit()
    }

    private fun setDateOfMonth() {
        lifecycleScope.launch {
            viewModel.getDataStore().setUpdate(true)
        }
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select date")
            .setSelection(System.currentTimeMillis())
            .build()
        supportFragmentManager.let { it1 ->
            datePicker.show(it1, "")
        }
        datePicker.addOnPositiveButtonClickListener {
            val sdf = SimpleDateFormat(TIME_STRING, Locale.getDefault())
            val netDate = Date(it)
            val date = sdf.format(netDate)
            Log.e("DATE_SEL", "$it")
            binding.setDText.text = date
            viewModel.modifyStartDate(it)

        }
    }

    private fun shareRoomId() {
        lifecycleScope.launch {
            val shareIntent = ShareCompat.IntentBuilder.from(this@SettingActivity)
                .setType("text/plain")
                .setText("${settingDataStore.getUserName()} has invited you to join the room. Click on link to join. https://roomies.app/${settingDataStore.getRoomKey()}")
                .intent
            if (shareIntent.resolveActivity(packageManager) != null) {
                startActivity(shareIntent)
            }
        }

    }

    private fun showSnackBar(msg: String) {
        Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG)
            .setBackgroundTint(resources.getColor(com.subhamgupta.roomiesapp.R.color.colorSecondary))
            .setTextColor(resources.getColor(com.subhamgupta.roomiesapp.R.color.colorOnSecondary))
            .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
            .setAction("Go") {

            }
            .show()
    }

    private fun leaveRoom() {
//        val dataSnapshotTask = ref.child("ROOM").child(key).child("ROOM_MATES").get()
//        dataSnapshotTask.addOnSuccessListener { dataSnapshot ->
////            Log.e("data", dataSnapshot.toString())
//            val snap: MutableMap<String, Any?> = HashMap()
//            var data: HashMap<String?, Any?>? = null
//            for (d in dataSnapshot.children) {
//                val v = d.value as HashMap<String?, Any?>
//                for (l in v) {
//                    if (l.value.toString() != user.uid) {
//                        data = v
//                        Log.e("VAl", "$v")
//                    }
//                }
//                snap[d.key!!] = d.value as HashMap<String?, Any?>
//
//            }
//            var c = ""
//            for (f in snap) {
//                if (f.value == data)
//                    c = f.key
//            }
//            snap.remove(c)
//            println(snap)
//            println(data)
//            ref.child("ROOM").child(key).child("ROOM_MATES").setValue(snap).addOnSuccessListener {
//                user_ref.child(roomRef).removeValue()
//                user_ref.child("ROOM_NAME").removeValue()
//                user_ref.child("IS_ROOM_JOINED").setValue(false)
//                ref.child("ROOM").child(key).child("JOINED_PERSON").setValue(joinedPerson?.minus(1))
//                settingsStorage.isRoom_joined = false
//                settingsStorage.room_name = ""
//                //supportFinishAfterTransition()
//                settingsStorage.isLoggedIn = false
//                goToRoomCreation(1)
//            }
//        }
    }


}