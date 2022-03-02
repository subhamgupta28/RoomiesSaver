package com.subhamgupta.roomiesapp.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.widget.Button
import android.widget.ImageView
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.subhamgupta.roomiesapp.R
import com.subhamgupta.roomiesapp.utility.SettingsStorage
import java.io.ByteArrayOutputStream
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class RationActivity : AppCompatActivity() {
    lateinit var dateChooser: TextInputEditText
    lateinit var noteText: TextInputEditText
    lateinit var layoutDate: TextInputLayout
    lateinit var cameraBtn: Button
    lateinit var upload: Button
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var storage: StorageReference
    lateinit var db: FirebaseFirestore
    lateinit var imageView: ImageView
    lateinit var settingsStorage: SettingsStorage
    var date: String?=null
    var byte: ByteArray?=null
    var note: String?=null
    var room_id: String?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ration)

        dateChooser = findViewById(R.id.etdate)
        noteText = findViewById(R.id.etnote)
        cameraBtn = findViewById(R.id.btcamera)
        upload = findViewById(R.id.btsave)
        layoutDate = findViewById(R.id.layoutdate)
        imageView = findViewById(R.id.image)

        firebaseAuth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance().getReference("ration_images/"+firebaseAuth.uid!!)
        db = FirebaseFirestore.getInstance()
        settingsStorage = SettingsStorage(this)
        room_id = settingsStorage.room_id.toString()
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select date")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()
        dateChooser.inputType = InputType.TYPE_NULL
        dateChooser.keyListener = null

        dateChooser.setOnClickListener {
            datePicker.show(supportFragmentManager, "")
        }
        cameraBtn.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, 200)
        }
        datePicker.addOnPositiveButtonClickListener {
            dateChooser.setText( datePicker.headerText.toString())
            val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val netDate = Date(it)
            println(sdf.format(netDate)+" $time")
            date = sdf.format(netDate)+" $time"

        }
        upload.setOnClickListener {
            note = noteText.text.toString()
            if (date.isNullOrEmpty() || byte!!.isEmpty())
                showSnackBar("Enter all details")
            else
                setRation(date!!, note!!, byte!!)
        }
    }
    private fun showSnackBar(msg: String) {
        Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG)
            .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
            .show()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK){
            if (requestCode == 200){
                val bitmap = data?.extras!!["data"] as Bitmap
                val bytes = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes)
                byte = bytes.toByteArray()
                imageView.setImageBitmap(bitmap)


            }
        }
    }
    private fun setRation(date: String, note: String, byt: ByteArray){
        var n = if(note.isNotEmpty()) note else ""
        val timeStamp = System.currentTimeMillis()
        storage.child("$timeStamp.jpg").putBytes(byt).addOnCompleteListener { task ->
            if (task.isSuccessful)
                storage.child("$timeStamp.jpg").downloadUrl.addOnSuccessListener {
                    val map = HashMap<String, Any>()
                    println(Uri.parse(it.toString()))
                    map["DATE"] = date
                    map["IMG_NAME"] = "$timeStamp.jpg"
                    map["IMG_URL"] = Uri.parse(it.toString()).toString()
                    db.collection(room_id+"_RATION").add(map)
                        .addOnCompleteListener {
                            showSnackBar("Ration saved")
                        }
                }

        }


    }
    val time: String
        get() {
            val date = Date()
            val sdm = SimpleDateFormat("hh:mm:ss", Locale.getDefault())
            return sdm.format(date)
        }
}