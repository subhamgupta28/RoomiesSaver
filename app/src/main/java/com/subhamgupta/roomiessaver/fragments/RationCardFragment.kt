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
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.google.android.material.appbar.AppBarLayout
import com.subhamgupta.roomiessaver.R
import java.lang.Exception


class RationCardFragment : DialogFragment() {
    lateinit var fView: View
    lateinit var imageView: ImageView
    lateinit var dateText: TextView
    lateinit var noteText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_App_Dialog_FullScreen)

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
        imageView = view.findViewById(R.id.image)
        dateText = view.findViewById(R.id.date)
        noteText = view.findViewById(R.id.note)
        val toolbar=view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.mappbar)
        toolbar.setNavigationOnClickListener {
            dismiss()
        }
        super.onViewCreated(view, savedInstanceState)

    }
    fun setUrl(url: String, context: Context, date: String, note:String) {
        try {
            Handler().postDelayed({
                Glide.with(context)
                    .load(url)
                    .into(imageView)
//                Log.e("URL", url)

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