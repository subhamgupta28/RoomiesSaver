package com.subhamgupta.roomiessaver.fragments

import android.content.Context
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.google.android.material.appbar.AppBarLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.subhamgupta.roomiessaver.R
import com.subhamgupta.roomiessaver.utility.SettingsStorage
import java.lang.Exception


class RationCardFragment : DialogFragment() {
    lateinit var fView: View
    lateinit var imageView: ImageView
    lateinit var dateText: TextView
    lateinit var noteText: TextView
    lateinit var db: FirebaseFirestore
    lateinit var deleteButton: Button
    lateinit var storage: StorageReference
    lateinit var settingsStorage: SettingsStorage
    var roomId: String? = null
    lateinit var datas: MutableList<Map<String, Any?>?>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setStyle(STYLE_NORMAL, R.style.Theme_App_Dialog_FullScreen)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View {
        //dialog?.window!!.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
      //  dialog?.window!!.attributes.blurBehindRadius = blurRadius
        fView = inflater.inflate(R.layout.fragment_ration_card, container, false)
//        val blurEffect = RenderEffect.createBlurEffect(32f, 32f, Shader.TileMode.MIRROR)
//        view?.setRenderEffect(blurEffect)
        return fView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        imageView = view.findViewById(R.id.image)
        dateText = view.findViewById(R.id.date)
        noteText = view.findViewById(R.id.note)
        deleteButton = view.findViewById(R.id.deletebtn)
        val toolbar=view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.mappbar)
        toolbar.setNavigationOnClickListener {
            dismiss()
        }

        db = FirebaseFirestore.getInstance()
        settingsStorage = activity?.let { SettingsStorage(it.applicationContext) }!!
        roomId = settingsStorage.room_id
        storage = FirebaseStorage.getInstance().getReference("ration_images/"+roomId!!)

    }
    override fun getTheme(): Int {
        return R.style.Theme_App_Dialog_FullScreen
    }
    fun delete(data: Map<String, Any?>?){
        val name = data?.get("IMG_NAME").toString()
        val node = data?.get("TIME_STAMP").toString()
        db.collection(roomId+"_RATION").document(node).delete().addOnCompleteListener {
            storage.child(name).delete().addOnCompleteListener {
                dismiss()
            }
        }

    }
    fun setUrl(url: String, context: Context, data: Map<String, Any?>?) {

        try {
            Handler().postDelayed({
                Glide.with(context)
                    .load(url)
                    .into(imageView)
//                Log.e("URL", url)
                var note = data!!["NOTE"].toString()
                val date = data["DATE"].toString()
                if (note.isEmpty())
                    note=""

                deleteButton.setOnClickListener {
                    delete(data)
                }
                if (note.equals(""))
                    noteText.visibility = View.GONE
                else
                    noteText.text = note
                dateText.text = date

            }, 50)
        }catch (e: Exception){
            e.printStackTrace()
        }



    }
}