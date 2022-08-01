package com.subhamgupta.roomiesapp.data.repositories

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DatabaseReference
import com.google.firebase.storage.FirebaseStorage
import com.subhamgupta.roomiesapp.utils.SettingDataStore
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SettingRepository @Inject constructor(
    private val databaseReference: DatabaseReference,
    private val storage: FirebaseStorage,
    private val dataStore: SettingDataStore
) {
    private var uuid:String=""
    suspend fun init() {
        uuid = dataStore.getUUID()
    }
    suspend fun leaveRoom(leaveRoom: MutableLiveData<Boolean>, key: String) = coroutineScope {
//        val ref = roomIDToRef[key]
//        roomIDToRef.remove(key)
//        if (ref != null) {
//            val res = suspendCoroutine<Task<Void>> { con ->
//                databaseReference.child(uuid!!).child(ref).removeValue().addOnCompleteListener {
//                    con.resume(it)
//                }
//            }
//            if (res.isSuccessful) {
//                val r = roomIDToRef.keys
//                roomIDToRef[r.first()]?.let { dataStore.setRoomRef(it) }
//            }
//            leaveRoom.postValue(res.isSuccessful)
//        }
    }

    suspend fun uploadPic(uri: Uri, userName: String, editUser: MutableStateFlow<Boolean>) =
        coroutineScope {
            val ref = storage.getReference(uuid!! + "/profile_pic.jpg")
            ref.putFile(uri)
                .addOnCompleteListener {
                    if (it.isComplete) {
                        it.result.storage.downloadUrl.addOnCompleteListener {
                            databaseReference.child(uuid!!).child("USER_NAME").setValue(userName)
                            databaseReference.child(uuid!!).child("IMG_URL")
                                .setValue(it.result.toString())
                            editUser.value = true
                        }
                    }
                }
                .addOnFailureListener {
                    editUser.value = false
                }
        }
}