package com.subhamgupta.roomiesapp.activities

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.google.android.material.chip.Chip
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.subhamgupta.roomiesapp.BuildConfig
import com.subhamgupta.roomiesapp.R
import com.subhamgupta.roomiesapp.utils.SettingDataStore
import com.subhamgupta.roomiesapp.data.viewmodels.MainViewModel
import com.subhamgupta.roomiesapp.databinding.ActivitySettingBinding
import com.subhamgupta.roomiesapp.databinding.EditDetailsBinding
import com.subhamgupta.roomiesapp.databinding.EditUserPopupBinding
import com.subhamgupta.roomiesapp.databinding.QrcodePopupBinding
import com.subhamgupta.roomiesapp.fragments.RoomCreation
import com.subhamgupta.roomiesapp.utils.Constant.Companion.TIME_STRING
import com.subhamgupta.roomiesapp.utils.FirebaseState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class SettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var materialAlertDialogBuilder: MaterialAlertDialogBuilder
    private lateinit var settingDataStore: SettingDataStore
    private lateinit var name: String
    private lateinit var roomKey: String
    private lateinit var imageView: ImageView
    private lateinit var limit: String
<<<<<<< HEAD
    private lateinit var alertDialog: AlertDialog
=======
    private lateinit var alertDialog:AlertDialog
>>>>>>> bc028885d2fc69567c10e880a1180fc67f3a028b
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.lifecycleOwner = this

    }

    override fun onStart() {
        super.onStart()
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
        lifecycleScope.launchWhenStarted {


            val it = viewModel.getRoomDataFromLocal()?.get(settingDataStore.getRoomKey()) as MutableMap<*,*>

            name = it["ROOM_NAME"].toString()
            limit = it["LIMIT"].toString()
            roomKey = it["ROOM_ID"].toString()
            binding.rId.text = roomKey
            binding.rName.text = name
            binding.cDate.text = it["CREATED_ON"].toString()
            binding.limit.text = limit
            try {
                val sdf = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
                val netDate = Date(it["START_DATE_MONTH"].toString().toLong())
                val date = sdf.format(netDate)
                binding.setDText.text = date
            } catch (e: Exception) {

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
                            withContext(Main) {
                                showSnackBar("Excel sheet generated")
                                shareFile()
                            }

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
        binding.showQr.setOnClickListener {
            generateQR()
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


        viewModel.leaveRoom.observe(this) {
            if (it)
                viewModel.refreshData()
        }

        lifecycleScope.launch {
            if (!viewModel.getDataStore().getDemo2()) {
                showDemo()
            }
            binding.nameText.text = settingDataStore.getUserName()
            binding.emailText.text = settingDataStore.getEmail()
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
<<<<<<< HEAD
            val data = viewModel.getUserDataFromLocal()
//            Log.e("URL", "${data?.get("IMG_URL")}")
            val requestOptions =
                RequestOptions().diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            val glide = Glide.with(this@SettingActivity)
                .load(data?.get("IMG_URL")?.toString())
                .apply(requestOptions)
                .placeholder(R.drawable.ic_person)
            glide.into(binding.bgImg)
            binding.bgImg.imageAlpha = 100
            glide.circleCrop().into(binding.profileImg)


=======
            viewModel.userData.buffer().collect {
                withContext(Main) {
                    Log.e("URL", "${it["IMG_URL"]}")
                    val requestOptions =
                        RequestOptions().diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    val glide = Glide.with(this@SettingActivity)
                        .load(it["IMG_URL"].toString())
                        .apply(requestOptions)
                        .placeholder(R.drawable.ic_person)
                    glide.into(binding.bgImg)
                    binding.bgImg.imageAlpha = 100
                    glide.circleCrop().into(binding.profileImg)
                }
            }
>>>>>>> bc028885d2fc69567c10e880a1180fc67f3a028b
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

    private fun generateQR() {
        materialAlertDialogBuilder = MaterialAlertDialogBuilder(this)
        val view = QrcodePopupBinding.inflate(layoutInflater)
        try {
            val barcodeEncoder = BarcodeEncoder()
            val bitmap =
                barcodeEncoder.encodeBitmap(roomKey + "ID", BarcodeFormat.QR_CODE, 500, 500)
            view.qrImage.setImageBitmap(bitmap)
        } catch (e: java.lang.Exception) {
        }
        materialAlertDialogBuilder.setView(view.root)
        materialAlertDialogBuilder.background = ColorDrawable(Color.TRANSPARENT)
        materialAlertDialogBuilder.show()
    }

    private fun showDemo() {
        val tp = TapTargetSequence(this)
            .targets(
                TapTarget.forView(binding.joinRoom, "Join room or create one")
                    .dimColor(R.color.colorOnSecondary)
                    .titleTextSize(25)
                    .outerCircleColor(R.color.colorRed)
                    .tintTarget(false)
                    .targetCircleColor(R.color.colorSecondary)
                    .textColor(R.color.colorOnPrimary),
                TapTarget.forView(binding.setDate, "Set start date of the month")
                    .dimColor(R.color.colorOnSecondary)
                    .titleTextSize(25)
                    .outerCircleColor(R.color.colorRed)
                    .tintTarget(false)
                    .targetCircleColor(R.color.colorSecondary)
                    .textColor(R.color.colorOnPrimary),
                TapTarget.forView(
                    binding.editProfile,
                    "Edit Profile",
                    "Change user name and profile pic"
                )
                    .dimColor(R.color.colorOnSecondary)
                    .titleTextSize(25)
                    .tintTarget(false)
                    .targetCircleColor(R.color.colorSecondary)
                    .outerCircleColor(R.color.colorRed)
                    .textColor(R.color.colorOnPrimary),
                TapTarget.forView(binding.shareid, "Share room", "Share the room key to others.")
                    .dimColor(R.color.colorOnSecondary)
                    .titleTextSize(25)
                    .tintTarget(false)
                    .targetCircleColor(R.color.colorSecondary)
                    .outerCircleColor(R.color.colorRed)
                    .textColor(R.color.colorOnPrimary),
                TapTarget.forView(
                    binding.showQr,
                    "Show QR Code",
                    "Show QR Code to let other people join your room."
                )
                    .dimColor(R.color.colorOnSecondary)
                    .titleTextSize(25)
                    .tintTarget(false)
                    .targetCircleColor(R.color.colorSecondary)
                    .outerCircleColor(R.color.colorRed)
                    .textColor(R.color.colorOnPrimary),
                TapTarget.forView(
                    binding.limitBtn,
                    "Edit Room",
                    "Edit details of the room"
                )
                    .dimColor(R.color.colorOnSecondary)
                    .titleTextSize(25)
                    .tintTarget(false)
                    .targetCircleColor(R.color.colorSecondary)
                    .outerCircleColor(R.color.colorRed)
                    .textColor(R.color.colorOnPrimary),
                TapTarget.forView(
                    binding.generateExcel,
                    "Generate and share excel sheet",
                    "All the spending of month converted to excel sheet."
                )
                    .dimColor(R.color.colorOnSecondary)
                    .titleTextSize(25)
                    .tintTarget(false)
                    .targetCircleColor(R.color.colorSecondary)
                    .outerCircleColor(R.color.colorRed)
                    .textColor(R.color.colorOnPrimary)
            )
            .listener(object : TapTargetSequence.Listener {
                // This listener will tell us when interesting(tm) events happen in regards
                // to the sequence
                override fun onSequenceFinish() {
                    // Yay
                    lifecycleScope.launch {
                        viewModel.getDataStore().setDemo2(true)
                    }
                }

                override fun onSequenceStep(lastTarget: TapTarget, targetClicked: Boolean) {
                    // Perform action for the current target
                    Log.e("target", lastTarget.toString())
                }

                override fun onSequenceCanceled(lastTarget: TapTarget) {
                    // Boo
                }
            })
        tp.start()
    }

    private fun shareFile() {
        try {
            val file = File(application.filesDir, "sheet.csv")
            if (file.exists()) {
                val uri =
                    FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", file)
                val intent = Intent(Intent.ACTION_SEND)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intent.setType("*/*")
                intent.putExtra(Intent.EXTRA_STREAM, uri)
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            Log.e("File sharing error", "${e.message}")
        }
    }

    var filePath: Uri? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 201 && resultCode == RESULT_OK && data != null && data.data != null) {
            val filePath = data.data
            this.filePath = filePath
            val bitmap = MediaStore.Images.Media
                .getBitmap(
                    contentResolver,
                    filePath
                )
            imageView.setImageBitmap(bitmap)
        }
    }

    private fun editUserDetail() {
        materialAlertDialogBuilder = MaterialAlertDialogBuilder(this)
        val view = EditUserPopupBinding.inflate(layoutInflater)
        imageView = view.image
        lifecycleScope.launch {
            viewModel.getDataStore().setUpdate(true)
        }
        view.selectImg.setOnClickListener {
            selectImage()
        }
        lifecycleScope.launch {
            viewModel.editUser.collect {
                when (it) {
                    is FirebaseState.Loading -> {
                        binding.progress.visibility = View.VISIBLE
                    }
                    is FirebaseState.Failed -> {
                        binding.progress.visibility = View.GONE
                    }
                    is FirebaseState.Success -> {
                        withContext(Main) {
                            showSnackBar("Saved")
                            binding.progress.visibility = View.GONE
                            alertDialog.dismiss()
                        }
                    }
                    is FirebaseState.Empty -> {
                        binding.progress.visibility = View.GONE
                    }
                }
            }
        }
        view.saveBtn.setOnClickListener {
            if (filePath != null && view.userName.text.toString().isNotEmpty()) {
                viewModel.editUser(filePath!!, view.userName.text.toString())
            } else {
                showSnackBar("Enter details")
            }
        }
        materialAlertDialogBuilder.setView(view.root)
        materialAlertDialogBuilder.background = ColorDrawable(Color.TRANSPARENT)
        alertDialog = materialAlertDialogBuilder.show()
    }

    private fun editLimit() {
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
            .setBackgroundTint(resources.getColor(R.color.colorSecondary))
            .setTextColor(resources.getColor(R.color.colorOnSecondary))
            .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
            .show()
    }

    private fun leaveRoom() {
        Snackbar.make(binding.root, "Do you want to leave the room?", Snackbar.LENGTH_LONG)
            .setBackgroundTint(resources.getColor(R.color.colorRed))
            .setTextColor(resources.getColor(R.color.colorOnSecondary))
            .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
            .setDuration(10000)
            .setAction("Leave") {
                viewModel.leaveRoom(roomKey)
            }
            .show()
    }


}