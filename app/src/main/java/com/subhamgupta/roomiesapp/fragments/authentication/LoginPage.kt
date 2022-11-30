package com.subhamgupta.roomiesapp.fragments.authentication

import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.subhamgupta.roomiesapp.R
import com.subhamgupta.roomiesapp.activities.MainActivity
import com.subhamgupta.roomiesapp.data.viewmodels.AuthViewModel
import com.subhamgupta.roomiesapp.databinding.FragmentLoginPageBinding
import com.subhamgupta.roomiesapp.utils.FirebaseState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.buffer

@AndroidEntryPoint
class LoginPage: Fragment() {
    lateinit var binding: FragmentLoginPageBinding
    private val viewModel: AuthViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentLoginPageBinding.inflate(layoutInflater)
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    findNavController().navigate(R.id.action_loginPage_to_userAuthFragment)
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
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
                        visible()
                        hideKeyboard()
                    }
                    is FirebaseState.Failed -> {
                        gone()
                        showSnackBar(it.message.toString())
                        hideKeyboard()
                    }
                    is FirebaseState.Success -> {
                        gone()
                        hideKeyboard()
                        if (it.data.isLoggedIn==true){
                            startActivity(Intent(activity, MainActivity::class.java))
                            activity?.finish()
                        }
                    }
                    else->Unit
                }

            }
            viewModel.forgetPass.buffer().collect{
                if (it)
                    showSnackBar("Password reset link is sent to your mail")
            }
        }
        binding.forgot.setOnClickListener {
            viewModel.resetPassword(binding.email.text.toString())
        }

        binding.back.setOnClickListener {
            findNavController().navigate(R.id.action_loginPage_to_userAuthFragment)
        }
        return binding.root
    }
    private fun visible(){
        binding.progress.visibility = View.VISIBLE
        binding.loginBtn.isEnabled = false
        binding.email.isEnabled = false
        binding.pass.isEnabled = false
        binding.forgot.isEnabled = false
    }
    private fun gone(){
        binding.progress.visibility = View.GONE
        binding.loginBtn.isEnabled = true
        binding.email.isEnabled = true
        binding.pass.isEnabled = true
        binding.forgot.isEnabled = true
    }
    private fun showSnackBar(msg: String) {
        val snackBarView = Snackbar.make(binding.root, msg , Snackbar.LENGTH_LONG)
        val view = snackBarView.view
        val params = view.layoutParams as FrameLayout.LayoutParams
        params.gravity = Gravity.TOP
        params.topMargin = 100
        view.layoutParams = params
        snackBarView.animationMode = BaseTransientBottomBar.ANIMATION_MODE_FADE
        snackBarView.setBackgroundTint(resources.getColor(R.color.colorSecondary))
            .setTextColor(resources.getColor(R.color.colorOnSecondary))
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