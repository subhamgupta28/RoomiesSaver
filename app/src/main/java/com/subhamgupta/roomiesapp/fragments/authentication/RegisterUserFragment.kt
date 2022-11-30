package com.subhamgupta.roomiesapp.fragments.authentication

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
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
import com.subhamgupta.roomiesapp.data.viewmodels.AuthViewModel
import com.subhamgupta.roomiesapp.databinding.FragmentRegisterUserBinding
import com.subhamgupta.roomiesapp.domain.model.CountryCode
import com.subhamgupta.roomiesapp.utils.FirebaseState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap

class RegisterUserFragment : Fragment() {
    lateinit var binding: FragmentRegisterUserBinding
    private val viewModel: AuthViewModel by activityViewModels()
    private val country = ArrayList<String>()
    private val map = LinkedHashMap<String, MutableList<String>>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterUserBinding.inflate(layoutInflater)
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    findNavController().navigate(R.id.action_registerUserFragment_to_userAuthFragment)
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
        binding.email.doOnTextChanged { text, _, _, _ ->
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

        val cCode = Locale.getDefault().displayCountry
        binding.country.setText(cCode.toString())
        viewModel.getCodes()
        lifecycleScope.launchWhenStarted {
            viewModel.countryCode.buffer().collect{
                for (c in it){
                    country.add(c.countryName)
                    if (map.containsKey(c.currencyCode))
                        map[c.currencyCode]?.add(c.countryName)
                    else
                        map[c.currencyCode] = mutableListOf(c.countryName)
                }
                Log.e("c","$map")
                val arrayAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_menu, R.id.textView, country)
                withContext(Dispatchers.Main){
                    binding.country.setAdapter(arrayAdapter)
                    binding.country.threshold = 3
                }
            }
        }

    }
    private fun createUser(){
        val email = binding.email.text.toString()
        val pass = binding.pass.text.toString()
        val name = binding.name.text.toString()
        val country = binding.country.text.toString().trim()

        var currCode = ""
        map.forEach{ (code, name) ->
            val n = name.find { it.lowercase() == country.lowercase() }
            if (n!=null)
                currCode = code
        }
        val countryCode = CountryCode()
        countryCode.countryName = country
        countryCode.currencyCode = currCode

        val check = email.isEmpty() and pass.isEmpty() and name.isEmpty() and currCode.isEmpty()
        if (check){
            showSnackBar("Enter all details correctly")
        }else{
            viewModel.registerUser(name, email, pass, countryCode)
        }
        Log.e("c data","$country $countryCode")
        lifecycleScope.launchWhenStarted {
            viewModel.userAuth.buffer().collect {
                when (it) {
                    is FirebaseState.Loading -> {
                        binding.progress.visibility = View.VISIBLE
                        binding.name.isEnabled = false
                        binding.pass.isEnabled = false
                        binding.email.isEnabled = false
                        binding.country.isEnabled = false
                    }
                    is FirebaseState.Failed -> {
                        binding.progress.visibility = View.GONE
                        binding.name.isEnabled = true
                        binding.pass.isEnabled = true
                        binding.email.isEnabled = true
                        binding.country.isEnabled = true
                        showSnackBar(it.message.toString())
                    }
                    is FirebaseState.Success -> {
                        binding.progress.visibility = View.GONE
                        binding.name.isEnabled = true
                        binding.pass.isEnabled = true
                        binding.email.isEnabled = true
                        binding.country.isEnabled = true
                        if (it.data.isRegistered==true){
                            viewModel.loginUser(email, pass)
                        }
                        if (it.data.isLoggedIn==false)
                            findNavController().navigate(R.id.action_registerUserFragment_to_roomCreation)
                    }
                    else->Unit
                }

            }
        }


    }
    private fun showSnackBar(msg: String) {
        val snackBarView = Snackbar.make(binding.root, msg , Snackbar.LENGTH_LONG)
        val view = snackBarView.view
        val params = view.layoutParams as FrameLayout.LayoutParams
        params.gravity = Gravity.TOP
        view.layoutParams = params
        snackBarView.animationMode = BaseTransientBottomBar.ANIMATION_MODE_FADE
        snackBarView.setBackgroundTint(resources.getColor(R.color.colorSecondary))
            .setTextColor(resources.getColor(R.color.colorOnSecondary))
            .show()
    }


}