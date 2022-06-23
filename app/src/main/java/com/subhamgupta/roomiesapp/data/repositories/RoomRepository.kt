package com.subhamgupta.roomiesapp.data.repositories

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import com.subhamgupta.roomiesapp.MyApp
import com.subhamgupta.roomiesapp.utils.SettingDataStore
import com.subhamgupta.roomiesapp.domain.model.CreateRoom
import com.subhamgupta.roomiesapp.domain.model.ROOMMATES
import com.subhamgupta.roomiesapp.domain.model.RoomDetail
import com.subhamgupta.roomiesapp.utils.FirebaseState
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Singleton
object RoomRepository {
    private val databaseReference = MyApp.instance.databaseReference
    private var roomKey: String = ""
    private var user_name: String = ""
    private val auth = MyApp.instance.firebaseAuth
    private val user = auth.currentUser
    private val uuid = user?.uid
    @SuppressLint("StaticFieldLeak")
    private val settingsStorage: SettingDataStore

    init {
        Log.e("INIT", "ROOM-REPO")
        settingsStorage = SettingDataStore

    }

    suspend fun getUser() {
        val userData = suspendCoroutine<MutableMap<String, Any>> { cont ->
            MyApp.instance.databaseReference.child(uuid!!).get().addOnCompleteListener {
                if (it.isSuccessful && it.result.exists()) {
                    val mp = it.result.value as MutableMap<String, Any>
                    cont.resumeWith(Result.success(mp))
                }
            }
        }
        user_name = userData["USER_NAME"].toString()
    }

    //join room if previously not joined any
    //join if already a member of other room
    //check for room id if present then increment and join
    //if not then start with ROOM_ID1 as reference

    suspend fun joinRoom(
        room_id: String,
        liveData: MutableStateFlow<FirebaseState<CreateRoom>>
    ) = coroutineScope {
        val map: MutableMap<String?, Any?> = HashMap()
        if (room_id != "") {
            liveData.value = FirebaseState.loading()
            val result = suspendCoroutine {
                databaseReference.child("ROOM").child(room_id).get()
                    .addOnCompleteListener { value ->
                        if (value.result != null) {
                            val result = value.result.getValue(RoomDetail::class.java)
                            it.resume(result)
                        }
                    }
            }
            val count = result?.JOINED_PERSON ?: 0
            val limit = result?.LIMIT ?: 0
            if (count <= limit) {
                val mate = ROOMMATES(
                    KEY = room_id,
                    MONEY_PAID = 0,
                    UUID = uuid,
                    USER_NAME = user_name
                )
                result?.ROOM_MATES?.add(mate)

                if (uuid != null) {
                    val userData = suspendCoroutine{ cont ->
                        databaseReference.child(uuid).get().addOnCompleteListener {
                            if (it.isSuccessful) {
                                val mp = it.result.value as MutableMap<String, Any>
                                cont.resume(mp)
                            }
                        }
                    }
                    val ob = getNewKey(userData, room_id)
                    val finalKey = ob["F_KEY"].toString()
                    val flag = ob["IS_JOIN"].toString().toBoolean()
                    if (!flag) {
                        Log.e("F_KEY", finalKey)
                        databaseReference.child("ROOM").child(room_id)
                            .child("ROOM_MATES").setValue(result?.ROOM_MATES)
                            .addOnFailureListener { e: Exception ->
                                Log.e("ERROR", e.message!!)
                                liveData.value = FirebaseState.success(
                                    CreateRoom(
                                        error = e.message.toString(),
                                        isCreated = false,
                                        isRoomJoined = false
                                    )
                                )
                            }
                            .addOnSuccessListener {
                                databaseReference.child(uuid).child("ROOM_NAME").setValue(map["ROOM_NAME"])
                                databaseReference.child(uuid).child(finalKey).setValue(room_id)
                                databaseReference.child(uuid).child("IS_ROOM_JOINED").setValue(true)
                                    .addOnSuccessListener {
                                        databaseReference.child("ROOM").child(room_id)
                                            .child("JOINED_PERSON").setValue(count + 1)
                                        showToast("Successfully joined the room")
                                        liveData.value = FirebaseState.success(
                                            CreateRoom(
                                                error = "",
                                                isCreated = false,
                                                isRoomJoined = true
                                            )
                                        )

                                    }
                                    .addOnFailureListener { showToast("Something went wrong try again with correct room id") }
                            }

                    } else {
                        showToast("Already Joined in this room")
                        liveData.value =
                            FirebaseState.failed("Already Joined in this room")
                    }
                }
            }
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(MyApp.instance, msg, Toast.LENGTH_LONG).show()
    }

    suspend fun createRoom(
        name: String,
        limit: Int,
        id: String,
        date: String,
        liveData: MutableStateFlow<FirebaseState<CreateRoom>>
    ) = coroutineScope {
        val room = "ROOM"
        liveData.value = FirebaseState.loading()
        val userData = suspendCoroutine{ cont ->
            databaseReference.child(uuid!!).get().addOnCompleteListener {
                if (it.isSuccessful) {
                    val mp = it.result.value as MutableMap<String, Any>
                    cont.resume(mp)
                }
            }
        }
        val ob = getNewKey(userData, id)
        val finalKey = ob["F_KEY"].toString()
//        val flag = ob["IS_JOIN"].toString().toBoolean()

        val map = mutableMapOf(
            "ROOM_ID" to id,
            "CREATED_ON" to date,
            "ROOM_NAME" to name,
            "START_DATE_MONTH" to System.currentTimeMillis(),
            "LIMIT" to limit,
            "ROOM_MATES" to mutableListOf(
                mutableMapOf(
                    "KEY" to id,
                    "MONEY_PAID" to 0,
                    "UUID" to uuid,
                    "USER_NAME" to user_name
                )
            ),
            "JOINED_PERSON" to 1,
            "CREATED_BY" to uuid
        )
        databaseReference.child(room).child(id).setValue(map)
            .addOnFailureListener { e: Exception ->
                Log.e("ERROR", e.message!!)
            }.addOnSuccessListener {
                databaseReference.child(uuid!!).child("IS_ROOM_JOINED").setValue(true)
                databaseReference.child(uuid).child(finalKey).setValue(id)
                    .addOnFailureListener { e: Exception ->
                        liveData.value = FirebaseState.failed(e.message)
                        Log.e("ERROR", e.message!!)
                    }
                    .addOnSuccessListener {
                        liveData.value = FirebaseState.success(CreateRoom("", true))
                    }
            }
    }
    private fun getNewKey(mp: MutableMap<*, *>, room_id: String): MutableMap<String, Any> {
        val kys = mp.keys
        val nxt = java.util.ArrayList<Int>()
        for (i in kys) {
            val r = i.toString()
            if (r.contains("ROOM_ID")) {
                val k = r.substring(7).toInt()
                nxt.add(k)
            }
        }
        val finalKey =
            if (nxt.size != 0)
                "ROOM_ID${(nxt.maxOrNull() ?: 0) + 1}"
            else
                "ROOM_ID1"
        var flag = false
        for (i in mp.values) {
            Log.e("F_KEY", "$i")
            if (i.toString() == room_id)
                flag = true
        }
        return mutableMapOf("F_KEY" to finalKey, "IS_JOIN" to flag)
    }

}




