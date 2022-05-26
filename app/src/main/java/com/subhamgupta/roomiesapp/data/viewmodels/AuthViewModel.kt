package com.subhamgupta.roomiesapp.data.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subhamgupta.roomiesapp.data.repositories.AuthRepository
import com.subhamgupta.roomiesapp.models.UserAuth
import com.subhamgupta.roomiesapp.utils.FirebaseState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository
    private val _userAuth = MutableStateFlow<FirebaseState<UserAuth>>(FirebaseState.empty())
    val userAuth: StateFlow<FirebaseState<UserAuth>> = _userAuth

    fun registerUser(name: String, email: String, pass: String) =
        viewModelScope.launch(Dispatchers.IO) {
            repository.registerUser(name, email, pass, _userAuth)

        }

    fun loginUser(email: String, pass: String) = viewModelScope.launch(Dispatchers.IO) {
        repository.loginUser(email, pass, _userAuth)

    }
}