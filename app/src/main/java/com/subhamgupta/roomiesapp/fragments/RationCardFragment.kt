package com.subhamgupta.roomiesapp.fragments

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.subhamgupta.roomiesapp.R
import com.subhamgupta.roomiesapp.databinding.FragmentRationCardBinding
import com.subhamgupta.roomiesapp.databinding.RationCardBinding


class RationCardFragment : DialogFragment() {

    private lateinit var binding: FragmentRationCardBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setStyle(STYLE_NORMAL, R.style.Theme_App_Dialog_FullScreen)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View {
        binding = FragmentRationCardBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar=view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.mappbar)
        toolbar.setNavigationOnClickListener {
            dismiss()
        }
        toolbar.title = "Stored Card"

    }

    override fun getTheme(): Int {
        return R.style.Theme_App_Dialog_FullScreen
    }

    //    fun delete(data: Map<String, Any?>?){
//        val name = data?.get("IMG_NAME").toString()
//        val node = data?.get("TIME_STAMP").toString()
//        db.collection(roomId+"_RATION").document(node).delete().addOnCompleteListener {
//            storage.child(name).delete().addOnCompleteListener {
//                dismiss()
//            }
//        }
//
//    }
    fun setUrl(url: String, context: Context, data: Map<String, Any?>?) {

        try {
            Handler().postDelayed({
                Glide.with(context)
                    .load(url)
                    .into(binding.image)
                Log.e("URL", url)
                var note = data!!["NOTE"].toString()
                val date = data["DATE"].toString()
                if (note.isEmpty())
                    note = ""

//                deleteButton.setOnClickListener {
//                    delete(data)
//                }
//                if (note.equals(""))
//                    noteText.visibility = View.GONE
//                else
//                    noteText.text = note
//                dateText.text = date

            }, 50)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}