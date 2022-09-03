package com.subhamgupta.roomiesapp.domain.use_case

import android.content.Context
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.subhamgupta.roomiesapp.domain.model.CountryCode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class GetCountryCodes {

    suspend operator fun invoke(
        storage: FirebaseStorage,
        context: Context
    ): List<CountryCode> {
        val oneMByte: Long = 20 * 1024
        val typeToken = object : TypeToken<List<CountryCode>>() {}.type
        val data = suspendCoroutine<List<CountryCode>> { cont ->
            storage.reference.child("country_codes/final.json")
                .getBytes(oneMByte).addOnSuccessListener {
                    val data = String(it)
                    val json = Gson().fromJson<List<CountryCode>>(data, typeToken)
                    Log.e("Data", "${json.size}")
                    cont.resume(json)
                    try {
                        val userFile = File(context.filesDir, "/country_code.json")
                        if (!userFile.exists())
                            userFile.createNewFile()
                        userFile.writeText(Gson().toJson(json))

                    } catch (e: Exception) {
                        Log.e("ERROR", "Saving country code data ${e.message}")
                    }
                }.addOnFailureListener {
                    Log.e("ERROR", "${it.message}")
                }
        }
        return data
    }
}