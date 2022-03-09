package com.subhamgupta.roomiesapp.utility

import android.content.Context
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class GetAllRooms(
    var ref: DatabaseReference,
    var context: Context,
    var uuid: String,

) {
    fun getRooms(ref: DatabaseReference, uuid: String){
        ref.child(uuid)
            .get()
    }
}