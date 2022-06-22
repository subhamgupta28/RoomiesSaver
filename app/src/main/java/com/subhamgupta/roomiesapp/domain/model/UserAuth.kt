package com.subhamgupta.roomiesapp.domain.model

import com.google.firebase.auth.FirebaseUser
import java.io.Serializable

data class UserAuth(
    var isLoggedIn :Boolean? = false,
    var user: FirebaseUser? = null,
    var isRegistered: Boolean? = false,
    var isVerified: Boolean? = false,
    var error: String = ""
):Serializable
