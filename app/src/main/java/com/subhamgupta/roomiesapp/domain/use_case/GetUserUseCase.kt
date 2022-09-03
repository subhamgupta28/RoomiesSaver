package com.subhamgupta.roomiesapp.domain.use_case

import android.util.Log
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class GetUserUseCase {
    suspend operator fun invoke(
        databaseReference: DatabaseReference,
        uuid:String
    ):MutableMap<String, Any>{
        val userData = suspendCancellableCoroutine<MutableMap<String, Any>> { cont ->
            databaseReference.child(uuid).get().addOnCompleteListener {
                if (it.isSuccessful && it.result.exists()) {
                    val mp = it.result.value as MutableMap<String, Any>
                    cont.resume(mp)
                }else{
                    Log.e("else","${it.exception}")
                }
            }
        }
        return userData
    }
}