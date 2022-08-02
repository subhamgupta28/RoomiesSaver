package com.subhamgupta.roomiesapp.data.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subhamgupta.roomiesapp.data.repositories.AuthRepository
import com.subhamgupta.roomiesapp.domain.model.UserAuth
import com.subhamgupta.roomiesapp.utils.FirebaseState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository : AuthRepository
) : ViewModel() {

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