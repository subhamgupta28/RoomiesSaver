package com.subhamgupta.roomiesapp.fragments.authentication

import android.app.ActivityOptions
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.subhamgupta.roomiesapp.R
import com.subhamgupta.roomiesapp.activities.MainActivity
import com.subhamgupta.roomiesapp.data.viewmodels.AuthViewModel
import com.subhamgupta.roomiesapp.databinding.FragmentLoginPageBinding
import com.subhamgupta.roomiesapp.utils.FirebaseState
import kotlinx.coroutines.flow.buffer


class LoginPage: Fragment() {


    lateinit var binding: FragmentLoginPageBinding
    private val viewModel: AuthViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentLoginPageBinding.inflate(layoutInflater)

        binding.loginBtn.setOnClickListener {
            loginUser()
        }
        binding.email.doOnTextChanged { text, start, before, count ->
            binding.emailLayout.error = if (!isValidEmail(text.toString())) "Enter correct email address" else ""
        }
        lifecycleScope.launchWhenStarted {
            viewModel.userAuth.buffer().collect {
                hideKeyboard()
                when (it) {
                    is FirebaseState.Loading -> {
                        binding.progress.visibility = View.VISIBLE
                        binding.loginBtn.isEnabled = false
                        binding.email.isEnabled = false
                        binding.pass.isEnabled = false
                        binding.forgot.isEnabled = false
                        hideKeyboard()
                    }
                    is FirebaseState.Failed -> {
                        binding.progress.visibility = View.GONE
                        binding.loginBtn.isEnabled = true
                        binding.email.isEnabled = true
                        binding.pass.isEnabled = true
                        binding.forgot.isEnabled = true
                        showSnackBar(it.message.toString())
                        hideKeyboard()
                    }
                    is FirebaseState.Success -> {
                        binding.progress.visibility = View.GONE
                        binding.loginBtn.isEnabled = true
                        binding.email.isEnabled = true
                        binding.pass.isEnabled = true
                        binding.forgot.isEnabled = true
                        if (it.data.isLoggedIn==true){
                            val bundle = ActivityOptions.makeSceneTransitionAnimation(activity).toBundle()
                            startActivity(Intent(activity, MainActivity::class.java))
                            activity?.finish()
//                findNavController().navigate(R.id.action_loginPage_to_userAuthFragment)
                        }
                    }
                    else->Unit
                }

            }
        }

        binding.back.setOnClickListener {
            findNavController().navigate(R.id.action_loginPage_to_userAuthFragment)
        }
        return binding.root
    }
    private fun showSnackBar(msg: String) {
        Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG)
            .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
            .show()
    }
    private fun loginUser() {
        val email = binding.email.text.toString()
        val pass = binding.pass.text.toString()
        hideKeyboard()
        viewModel.loginUser(email, pass)

    }


    private fun isValidEmail(email: String): Boolean {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun hideKeyboard() {

        val inputManager = activity?.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
    }



}