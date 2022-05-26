package com.subhamgupta.roomiesapp.fragments.authentication

import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.subhamgupta.roomiesapp.R
import com.subhamgupta.roomiesapp.data.viewmodels.AuthViewModel
import com.subhamgupta.roomiesapp.databinding.FragmentRegisterUserBinding
import com.subhamgupta.roomiesapp.utils.FirebaseState
import kotlinx.coroutines.flow.buffer

class RegisterUserFragment : Fragment() {
    lateinit var binding: FragmentRegisterUserBinding
    private val viewModel: AuthViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterUserBinding.inflate(layoutInflater)

        binding.email.doOnTextChanged { text, start, before, count ->
            binding.emailLayout.error = if (!isValidEmail(text.toString())) "Enter correct email address" else ""
        }
        binding.back.setOnClickListener {
            findNavController().navigate(R.id.action_registerUserFragment_to_userAuthFragment)
        }
        return binding.root
    }
    private fun isValidEmail(email: String): Boolean {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.createAcc.setOnClickListener {
            createUser()
        }

    }
    private fun createUser(){
        val email = binding.email.text.toString()
        val pass = binding.pass.text.toString()
        val name = binding.name.text.toString()

        viewModel.registerUser(name, email, pass)
        lifecycleScope.launchWhenStarted {
            viewModel.userAuth.buffer().collect {
                when (it) {
                    is FirebaseState.Loading -> {
                        binding.progress.visibility = View.VISIBLE
                    }
                    is FirebaseState.Failed -> {
                        binding.progress.visibility = View.GONE
                        showSnackBar(it.message.toString())
                    }
                    is FirebaseState.Success -> {
                        binding.progress.visibility = View.GONE
                        if (it.data.isRegistered==true){
                            viewModel.loginUser(email, pass)
                        }
                        if (it.data.isLoggedIn==true)
                            findNavController().navigate(R.id.action_registerUserFragment_to_roomCreation)
                    }
                    else->Unit
                }

            }
        }


    }
    private fun showSnackBar(msg: String) {
        Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG)
            .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
            .show()
    }


}