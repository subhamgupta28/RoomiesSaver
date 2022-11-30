package com.subhamgupta.roomiesapp.data.repositories

import android.app.Application
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.storage.FirebaseStorage
import com.subhamgupta.roomiesapp.domain.model.CountryCode
import com.subhamgupta.roomiesapp.utils.SettingDataStore
import com.subhamgupta.roomiesapp.domain.model.UserAuth
import com.subhamgupta.roomiesapp.domain.use_case.GetCountryCodes
import com.subhamgupta.roomiesapp.utils.FirebaseState
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class AuthRepository @Inject constructor(
    private val databaseReference: DatabaseReference,
    private val auth: FirebaseAuth,
    private val settingDataStore: SettingDataStore,
    private val application: Application,
    private val storage: FirebaseStorage
) {


    private fun saveUserToRDB(email: String, name: String, user: FirebaseUser, country: CountryCode) {
        val uid = user.uid
        databaseReference.child(uid).child("USER_NAME").setValue(name)
        databaseReference.child(uid).child("ROLE").child("BETA_ENABLED").setValue(false)
        databaseReference.child(uid).child("ROLE").child("ADMIN").setValue(false)
        databaseReference.child(uid).child("ROLE").child("USER").setValue(true)
        databaseReference.child(uid).child("UUID").setValue(uid)
        databaseReference.child(uid).child("USER_EMAIL").setValue(email)
        databaseReference.child(uid).child("COUNTRY").setValue(country)
        databaseReference.child(uid).child("IS_ROOM_JOINED").setValue(false)
            .addOnSuccessListener {

            }
            .addOnFailureListener {

            }
    }

    fun signOut() {
        auth.signOut()
    }


    suspend fun registerUser(
        name: String,
        email: String,
        pass: String,
        country:CountryCode,
        liveData: MutableStateFlow<FirebaseState<UserAuth>>
    ) = coroutineScope {
        liveData.value = FirebaseState.loading()
        val result = suspendCoroutine<Task<AuthResult>> { cont ->
            auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener { task: Task<AuthResult> ->
                    cont.resume(task)
                }
                .addOnFailureListener {
                    Log.e("ERROR_LOGIN_PAGE", it.message.toString())
                    liveData.value = FirebaseState.failed(it.message.toString())
                }

        }
        if (result.isComplete) {
            try {
                val u = result.result.user
                val user = UserAuth(false, u, true, u?.isEmailVerified)
                if (u != null) {
                    saveUserToRDB(email, name, u, country)
                }
                storeUserData(u!!.uid, liveData, user)
            } catch (e: Exception) {
                Log.e("ERROR_LOGIN_PAGE", e.message!!)
                liveData.value = FirebaseState.failed(e.message)
//                        liveData.postValue(UserAuth(false, null, false, e.message.toString()))
            }
        }

    }

    suspend fun loginUser(
        email: String,
        pass: String,
        liveData: MutableStateFlow<FirebaseState<UserAuth>>
    ) = coroutineScope {
        liveData.value = FirebaseState.loading()

        val result = suspendCoroutine<Task<AuthResult?>> { cont ->
            auth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener { task: Task<AuthResult?> ->
                    cont.resume(task)
                }.addOnFailureListener { e: Exception ->
                    Log.e("ERROR", "$e")
                    liveData.value = FirebaseState.failed(e.message.toString())
                }

        }
        if (result.isSuccessful) {
            val user = auth.currentUser!!
            val u = UserAuth(true, user, false, user.isEmailVerified)
            if (!user.isEmailVerified) {
                user.sendEmailVerification()
            }
            liveData.value = FirebaseState.success(u)
            storeUserData(user.uid, liveData, u)

        }

    }

    private suspend fun storeUserData(
        uuid: String,
        liveData: MutableStateFlow<FirebaseState<UserAuth>>,
        u: UserAuth
    ) = coroutineScope {
        val result = suspendCoroutine<MutableMap<String, Any>> { cont ->
            databaseReference.child(uuid).get().addOnCompleteListener {
                if (it.isSuccessful) {
                    val mp = it.result.value as MutableMap<String, Any>
                    Log.e("AUTH", "$mp")
                    cont.resume(mp)

                }
            }
        }
        val key = result[settingDataStore.getRoomRef()].toString()
        settingDataStore.setRoomKey(key)
        settingDataStore.setUUID(result["UUID"].toString())
        settingDataStore.setEmail(result["USER_EMAIL"].toString())
        liveData.value = FirebaseState.success(u)

    }
    suspend fun getCountryCodes(countryCodes: MutableStateFlow<List<CountryCode>>) {
        val data = GetCountryCodes()(storage, application)
        countryCodes.value = data
        Log.e("Result","$data")
    }

    suspend fun resetPassword(email:String, forgetPass: MutableStateFlow<Boolean>) {
        forgetPass.value = false
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
            forgetPass.value = true
        }.addOnFailureListener {
            forgetPass.value = false
            }
    }


}