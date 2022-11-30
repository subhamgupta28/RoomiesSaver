package com.subhamgupta.roomiesapp.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.subhamgupta.roomiesapp.R
import com.subhamgupta.roomiesapp.data.viewmodels.MainViewModel
import com.subhamgupta.roomiesapp.databinding.FragmentBottomBinding
import com.subhamgupta.roomiesapp.databinding.FragmentSummaryBinding
import com.subhamgupta.roomiesapp.utils.FirebaseState
import kotlinx.coroutines.flow.buffer
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class BottomFragment : BottomSheetDialogFragment() {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var binding: FragmentBottomBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBottomBinding.inflate(layoutInflater)

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select date")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        binding.etdate.setOnClickListener {

            fragmentManager?.let { it1 -> datePicker.show(it1, "") }

        }
        binding.btcamera.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, 200)
        }

        datePicker.addOnPositiveButtonClickListener{
            binding.etdate.setText( datePicker.headerText.toString())
            val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val netDate = Date(it)
            println(sdf.format(netDate)+" $time")
//            date = sdf.format(netDate)+" $time"

        }

        lifecycleScope.launchWhenStarted {
            viewModel.uploadStoreCard.buffer().collect{
                when (it) {
                    is FirebaseState.Loading -> {
                        binding.bprogress.visibility = View.VISIBLE
                    }
                    is FirebaseState.Empty -> {
                    }
                    is FirebaseState.Failed -> {
                    }
                    is FirebaseState.Success -> {
                       binding.bprogress.visibility = View.GONE
                    }
                }
            }
        }

        return binding.root
    }

//
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK){
            if (requestCode == 200){

                val bitmap = data?.extras!!["data"] as Bitmap
                val bytes = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)

//                byte = bytes.toByteArray()
                binding.image.setImageBitmap(bitmap)

                binding.btsave.setOnClickListener {
                    viewModel.uploadCard(binding.etdate.text.toString(), binding.etnote.text.toString(), bytes.toByteArray())
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