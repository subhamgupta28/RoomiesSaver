package com.subhamgupta.roomiesapp.domain.use_case

import com.google.firebase.database.DatabaseReference
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class GetUserUseCase {
    suspend operator fun invoke(
        databaseReference: DatabaseReference,
        uuid:String
    ):MutableMap<String, Any>{
        val userData = suspendCoroutine<MutableMap<String, Any>> { cont ->
            databaseReference.child(uuid).get().addOnCompleteListener {
                if (it.isSuccessful && it.result.exists()) {
                    val mp = it.result.value as MutableMap<String, Any>
                    cont.resume(mp)
                }
            }
        }
        return userData
    }
}