package com.subhamgupta.roomiesapp.fragments

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ScanMode
import com.google.android.material.snackbar.Snackbar
import com.subhamgupta.roomiesapp.R
import com.subhamgupta.roomiesapp.activities.MainActivity
import com.subhamgupta.roomiesapp.data.viewmodels.RoomViewModel
import com.subhamgupta.roomiesapp.databinding.FragmentRoomCreationBinding
import com.subhamgupta.roomiesapp.utils.Constant.Companion.DATE_STRING
import com.subhamgupta.roomiesapp.utils.FirebaseState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

private const val CAMERA_REQUEST_CODE = 101


@AndroidEntryPoint
class RoomCreation: Fragment() {
    lateinit var map: HashMap<String, Any?>
    lateinit var user_name: String
    lateinit var binding: FragmentRoomCreationBinding
    private val viewModel: RoomViewModel by viewModels()
    private lateinit var codeScanner: CodeScanner
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRoomCreationBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.generateIdBtn.setOnClickListener {
            createRoom()
        }
        binding.logout.setOnClickListener {
            logOut()
        }
        binding.cancel.setOnClickListener {
            binding.scannerView.visibility = View.GONE
            binding.cancel.visibility = View.GONE
            binding.line1.visibility = View.VISIBLE
        }
        binding.joinBtn.setOnClickListener {
            viewModel.joinRoom(binding.joinRoom.text.toString())
        }
        binding.joinByQr.setOnClickListener {
            binding.scannerView.visibility = View.VISIBLE
            binding.cancel.visibility = View.VISIBLE
            binding.line1.visibility = View.GONE
        }

        lifecycleScope.launchWhenStarted {
            viewModel.createRoom.buffer().collect{
                when (it){
                    is FirebaseState.Failed ->{

                    }
                    is FirebaseState.Loading ->{

                    }
                    is FirebaseState.Success ->{
                        if (it.data.isRoomJoined || it.data.isCreated)
                            startActivity(Intent(activity, MainActivity::class.java))

                    }
                    else -> Unit
                }
            }
        }



    }

    private fun createRoom(){
        val name = binding.roomName.text.toString()
        val limit = binding.limitPerson.text.toString()
        val id = generateID(name)
        if (name.isNotEmpty()){
            viewModel.createRoom(name, if (limit.isEmpty()) 5 else limit.toInt(), id, date)
        }
    }


    private fun logOut() {


    }

    val date: String
        get() {
            val date = Date()
            val sdm = SimpleDateFormat(DATE_STRING, Locale.getDefault())
            return sdm.format(date)
        }
    private fun removeSpecialCharacter(str: String):String {
        Log.e("string before", str)
        var s = str
//        val re = Regex("[^A-Za-z0-9]")
        s = str.filter { it.isLetterOrDigit() }
//        s = re.replace(s, "")
        Log.e("string after", s)
        return s
    }
    companion object {
        fun generateID(text: String): String {
            val te = text.filter { it.isLetterOrDigit() }.uppercase()
            val n = 10
            val t = System.currentTimeMillis().toString()
            val str = t + te + t + te + t + te
            val sb = StringBuilder(n)
            for (i in 0 until n) {
                val index = (str.length
                        * Math.random()).toInt()
                sb.append(str[index])
            }
            return sb.toString().trim()
        }
    }

    override fun onStart() {
        super.onStart()
        setPermission()
        codeScanner()

    }
    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        super.onPause()
        codeScanner.releaseResources()
    }
    private fun setPermission() {
        val permission = ContextCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.CAMERA
        )

        if (permission != PackageManager.PERMISSION_GRANTED) {
            makeRequest()
        }
    }
    private fun codeScanner() {
        codeScanner = CodeScanner(requireContext(), binding.scannerView)

        codeScanner.apply {
            camera = CodeScanner.CAMERA_BACK
            formats = CodeScanner.ALL_FORMATS
            autoFocusMode = AutoFocusMode.SAFE
            scanMode = ScanMode.CONTINUOUS
            isAutoFocusEnabled = true
            isFlashEnabled = false
            decodeCallback = DecodeCallback {
                lifecycleScope.launch(Dispatchers.Main){
                    val res = it.text.toString()
                    if (res.contains("ID")){
                        binding.joinRoom.setText(res.removeSuffix("ID"))
                        binding.scannerView.visibility = View.GONE
                        binding.cancel.visibility = View.GONE
                        binding.line1.visibility = View.VISIBLE
                        codeScanner.stopPreview()
                    }else{
                        showSnackBar("Not recognized as Room, Scan again")
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


    private fun makeRequest() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(android.Manifest.permission.CAMERA),
            CAMERA_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED){

        }
        else{
            //success
        }
    }
}