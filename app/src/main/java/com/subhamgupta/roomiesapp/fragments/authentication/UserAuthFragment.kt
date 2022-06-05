package com.subhamgupta.roomiesapp.fragments.authentication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.subhamgupta.roomiesapp.R
import com.subhamgupta.roomiesapp.databinding.FragmentUserAuthBinding

class UserAuthFragment : Fragment() {
    lateinit var binding: FragmentUserAuthBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserAuthBinding.inflate(layoutInflater)
        binding.lifecycleOwner = this
        binding.signin.setOnClickListener {
            findNavController().navigate(R.id.action_userAuthFragment_to_loginPage)
        }
        binding.signup.setOnClickListener {
            findNavController().navigate(R.id.action_userAuthFragment_to_registerUserFragment)
        }
        return binding.root
    }
}