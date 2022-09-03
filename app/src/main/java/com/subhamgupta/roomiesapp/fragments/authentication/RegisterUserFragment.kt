package com.subhamgupta.roomiesapp.fragments.authentication

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.subhamgupta.roomiesapp.R
import com.subhamgupta.roomiesapp.data.viewmodels.AuthViewModel
import com.subhamgupta.roomiesapp.databinding.FragmentRegisterUserBinding
import com.subhamgupta.roomiesapp.domain.model.CountryCode
import com.subhamgupta.roomiesapp.utils.FirebaseState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.withContext

class RegisterUserFragment : Fragment() {
    lateinit var binding: FragmentRegisterUserBinding
    private val viewModel: AuthViewModel by activityViewModels()
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
        viewModel.getCodes()
        lifecycleScope.launchWhenStarted {
            viewModel.countryCode.buffer().collect{
                val country = ArrayList<String>()
                val map = LinkedHashMap<String, MutableList<String>>()
                for (c in it){
                    if (map.containsKey(c.currencyCode))
                        map[c.currencyCode]?.add(c.countryName)
                    else
                        map[c.currencyCode] = mutableListOf(c.countryName)
                }
                Log.e("c","$map")
//                val arrayAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_menu, R.id.textView, country)
//                withContext(Dispatchers.Main){
//                    binding.country.setAdapter(arrayAdapter)
//                }
            }
        }

    }
    private fun createUser(){
        val email = binding.email.text.toString()
        val pass = binding.pass.text.toString()
        val name = binding.name.text.toString()

        val countryCode = CountryCode()
        viewModel.registerUser(name, email, pass, countryCode)
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
                        if (it.data.isLoggedIn==false)
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