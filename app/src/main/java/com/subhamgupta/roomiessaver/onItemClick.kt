package com.subhamgupta.roomiessaver

import com.google.firebase.auth.FirebaseUser

interface onItemClick {
    fun loginComplete(user: FirebaseUser)
    fun roomCreated()
    fun logout()
    fun signInComplete()

}