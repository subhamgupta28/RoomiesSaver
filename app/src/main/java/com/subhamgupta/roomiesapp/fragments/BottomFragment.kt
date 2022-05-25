package com.subhamgupta.roomiesapp.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.subhamgupta.roomiesapp.R
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class BottomFragment : BottomSheetDialogFragment() {




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_bottom, container, false)

//        storage = FirebaseStorage.getInstance().getReference("ration_images/"+room_id!!)
//        db = FirebaseFirestore.getInstance()
//
//
//        val datePicker = MaterialDatePicker.Builder.datePicker()
//            .setTitleText("Select date")
//            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
//            .build()
//
//
//        dateChooser.setOnClickListener {
//
//        }
//        cameraBtn.setOnClickListener {
//            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//            startActivityForResult(intent, 200)
//        }
//
//        datePicker.addOnPositiveButtonClickListener  {
//            dateText.setText( datePicker.headerText.toString())
//            val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
//            val netDate = Date(it)
//            println(sdf.format(netDate)+" $time")
//            date = sdf.format(netDate)+" $time"
//
//        }
//        upload.setOnClickListener {
//            note = noteText.text.toString()
//            println(date)
//            println(byte)
//            if (date.isNullOrEmpty() || byte!!.isEmpty())
//                Toast.makeText(context, "Enter all details", Toast.LENGTH_SHORT).show()
//            else
//                setRation(date!!, note!!, byte!!)
//        }
        return view
    }
    private fun showSnackBar(msg: String) {
//        Snackbar.make(v, msg, Snackbar.LENGTH_LONG)
//            .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
//            .show()
    }
//    private fun setRation(date: String, note: String, byt: ByteArray){
//        val n = if(note.isNotEmpty()) note else ""
//        val ts = System.currentTimeMillis()
//        val ref = storage.child("$ts.jpg")
//        val task = ref.putBytes(byt)
//
//        progress.visibility = View.VISIBLE
//        task.addOnCompleteListener { t ->
//            if (t.isSuccessful)
//                storage.child("$ts.jpg").downloadUrl.addOnSuccessListener {
//                    val map = HashMap<String, Any>()
//                    println(Uri.parse(it.toString()))
//                    map["DATE"] = date
//                    map["NOTE"] = n
//                    map["TIME_STAMP"] = ts
//                    map["IMG_NAME"] = "$ts.jpg"
//                    map["IMG_URL"] = Uri.parse(it.toString()).toString()
//                    db.collection(room_id+"_RATION").document(ts.toString()).set(map)
//                        .addOnCompleteListener {
//                            showSnackBar("Ration saved")
//                            progress.progress = 50
//                            progress.visibility = View.INVISIBLE
//                            Toast.makeText(context, "Ration saved", Toast.LENGTH_SHORT).show()
//                            sendNotify("New ration uploaded",note)
//                        }
//                }
//
//
//        }.addOnProgressListener {
//            val p: Double =
//                100.0 * it.bytesTransferred / it.totalByteCount
//            progress.progress = p.toInt()
//        }
//
//
//    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK){
            if (requestCode == 200){

                val bitmap = data?.extras!!["data"] as Bitmap
                val bytes = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)

//                byte = bytes.toByteArray()
//                imageView.setImageBitmap(bitmap)


            }
        }
    }
//    private fun sendNotify(title: String, msg: String) {
//        val notification = JSONObject()
//        val notifyBody = JSONObject()
//        val uid = firebaseAuth.uid.toString()
//        try {
//            notifyBody.put("title", title)
//            notifyBody.put("message", msg)
//            notifyBody.put("uid", uid)
//            notifyBody.put("time", time)
////            Log.e("NOTIFY_UID", uid)
//            notification.put("to", "/topics/$room_id")
//            notification.put("data", notifyBody)
//        }catch (e: Exception){
//
//        }
//        context?.let { NotificationSender().sendNotification(notification, it) }
//        sendNotification(notification)
//    }
    val time: String
        get() {
            val date = Date()
            val sdm = SimpleDateFormat("hh:mm:ss", Locale.getDefault())
            return sdm.format(date)
        }

}