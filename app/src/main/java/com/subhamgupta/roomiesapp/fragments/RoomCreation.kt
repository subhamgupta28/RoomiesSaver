package com.subhamgupta.roomiesapp.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.subhamgupta.roomiesapp.activities.MainActivity
import com.subhamgupta.roomiesapp.data.viewmodels.RoomViewModel
import com.subhamgupta.roomiesapp.databinding.FragmentRoomCreationBinding
import com.subhamgupta.roomiesapp.utils.Constant.Companion.DATE_STRING
import com.subhamgupta.roomiesapp.utils.FirebaseState
import kotlinx.coroutines.flow.buffer
import java.text.SimpleDateFormat
import java.util.*

class RoomCreation: Fragment() {
    lateinit var map: HashMap<String, Any?>
    lateinit var user_name: String
    lateinit var binding: FragmentRoomCreationBinding
    lateinit var viewModel: RoomViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRoomCreationBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this)[RoomViewModel::class.java]

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
        binding.joinBtn.setOnClickListener {
            viewModel.joinRoom(binding.joinRoom.text.toString())
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
        val limit = binding.limitPerson.text.toString().toInt()
        val id =  generateID(name)
        if (name.isNotEmpty()){
            viewModel.createRoom(name, limit, id, date)
        }

    }
    private fun getUser() {

    }

    private fun logOut() {


    }

    val date: String
        get() {
            val date = Date()
            val sdm = SimpleDateFormat(DATE_STRING, Locale.getDefault())
            return sdm.format(date)
        }

    companion object {
        fun generateID(text: String): String {
            var te = text.trim()
            val n = 10
            val t = System.currentTimeMillis().toString()
            te = text.uppercase()
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
}