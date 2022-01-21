package com.subhamgupta.roomiessaver.activities

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.subhamgupta.roomiessaver.R
import com.subhamgupta.roomiessaver.utility.SettingsStorage
import java.text.SimpleDateFormat
import java.util.*

class RoomCreation : AppCompatActivity() {
    lateinit var join_btn: Button
    lateinit var generate_btn: Button
    lateinit var logout: Button
    lateinit var contextView: View
    lateinit var ref: DatabaseReference
    lateinit var uid: String
    lateinit var user: FirebaseUser
    lateinit var mAuth: FirebaseAuth
    lateinit var room_layout: TextInputLayout
    lateinit var join_layout: TextInputLayout
    lateinit var limit_layout: TextInputLayout
    lateinit var room_name: TextInputEditText
    lateinit var join_room: TextInputEditText
    lateinit var limit_person: TextInputEditText
    lateinit var settingsStorage: SettingsStorage
    lateinit var map: HashMap<String, Any?>
    lateinit var user_name: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_creation)
        join_btn = findViewById(R.id.join_btn)
        generate_btn = findViewById(R.id.generate_id_btn)
        join_room = findViewById(R.id.join_room)
        join_layout = findViewById(R.id.join_layout)
        room_layout = findViewById(R.id.room_layout)
        room_name = findViewById(R.id.room_name)
        limit_layout = findViewById(R.id.limit_layout)
        limit_person = findViewById(R.id.limit_person)
        logout = findViewById(R.id.logout)
        contextView = findViewById(android.R.id.content)
        settingsStorage = SettingsStorage(this)

        if (!settingsStorage.room_id.equals("null"))
            join_room.setText(settingsStorage.room_id)
        mAuth = FirebaseAuth.getInstance()
        user = mAuth.currentUser!!
        ref = FirebaseDatabase.getInstance().reference.child("ROOMIES")

        uid = user.uid
        join_btn.setOnClickListener { joinRoom() }
        getUser()
        generate_btn.setOnClickListener { createRoom() }
        logout.setOnClickListener {
            logOut()
        }


    }

    private fun getUser() {
        val data: MutableMap<String?, String> = HashMap()
        ref.child(user.uid).get().addOnCompleteListener { task: Task<DataSnapshot> ->
            if (task.isSuccessful) {
                for (ds in task.result!!.children)
                    data[ds.key] = ds.value.toString()
                user_name = data["USER_NAME"].toString()
            }
        }
    }
    private fun logOut() {
        mAuth.signOut()
        val bundle = ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
        supportFinishAfterTransition()
        startActivity(Intent(applicationContext, LoginPage::class.java), bundle)

    }
    override fun onStop() {
        super.onStop()
        supportFinishAfterTransition()
    }
    private fun createRoom() {
        val name = room_name.text.toString()
        val id = generateID(name)
//        Log.e("ID", id)
        val room = "ROOM"
        val l: String = limit_person.text.toString()
        var limit = 100
        if (l != "") limit = l.toInt()
        //String t = String.valueOf((System.currentTimeMillis()));
        ref.child(room).child(id).child("ROOM_ID").setValue(id)
        ref.child(room).child(id).child("CREATED_ON").setValue(date)
        ref.child(room).child(id).child("ROOM_NAME").setValue(name)
        ref.child(room).child(id).child("LIMIT").setValue(limit)
        val list: MutableList<Map<String, Any?>> = ArrayList()
        map = HashMap()
        map["KEY"] = id
        map["MONEY_PAID"] = 0
        map["UUID"] = user.uid
        map["USER_NAME"] = user_name
        list.add(map)
        ref.child(room).child(id).child("ROOM_MATES").setValue(list)
        ref.child(room).child(id).child("JOINED_PERSON").setValue(1)
        ref.child(room).child(id).child("CREATED_BY").setValue(uid)
            .addOnFailureListener { e: Exception ->
                Log.e("ERROR", e.message!!)
                Snackbar.make(contextView, "Something went wrong", Snackbar.LENGTH_LONG).show()
            }.addOnSuccessListener { unused: Void? ->
                settingsStorage.isRoom_joined = true
                Snackbar.make(contextView, "Room Created Successfully", Snackbar.LENGTH_LONG).show()
                ref.child(uid).child("ROOM_ID").setValue(id)
                ref.child(uid).child("ROOM_NAME").setValue(name)
                ref.child(uid).child("IS_ROOM_JOINED").setValue(true)
                ref.child(uid).child("KEY").setValue(id)
                    .addOnFailureListener { e: Exception -> Log.e("ERROR", e.message!!) }
                    .addOnSuccessListener { unused1: Void? ->
                        settingsStorage.isRoom_joined = true
                        nextActivity()
                    }
            }
    }

    private fun nextActivity() {
        val bundle = ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
        startActivity(Intent(applicationContext, MainActivity::class.java), bundle)
    }

    private fun joinRoom() {
        val room_id = join_room.text.toString()
        val map: MutableMap<String?, Any?> = HashMap()
        if (room_id != "") {
            ref.child("ROOM").child(room_id).get()
                .addOnSuccessListener { dataSnapshot: DataSnapshot ->
                    if (dataSnapshot.value != null) {
                        var list: MutableList<Map<String?, Any?>> = ArrayList()
                        for (ds in dataSnapshot.children) {
//                            Log.e("DATA", ds.key + " " + ds.value)
                            map[ds.key] = ds.value
                            if (ds.key == "ROOM_MATES") {
                                list = ds.value as MutableList<Map<String?, Any?>>
                            }
                        }

                        val count = map["JOINED_PERSON"] as Long
                        val limit = map["LIMIT"] as Long

//                        Log.e("count", count.toString())
//                        Log.e("limit", limit.toString())

                        if (count <= limit) {
                            val map1: MutableMap<String?, Any?> = HashMap()
                            map1["KEY"] = room_id
                            map1["MONEY_PAID"] = 0
                            map1["UUID"] = user.uid
                            map1["USER_NAME"] = user_name
                            list.add(map1)
                            ref.child("ROOM").child(room_id).child("ROOM_MATES").setValue(list)
                                .addOnFailureListener { e: Exception ->
                                    Log.e(
                                        "ERROR",
                                        e.message!!
                                    )
                                }
                                .addOnSuccessListener {
//                                    Log.e("SUCCESS", "unused.toString()")
                                    join_btn.text = "Enter"
                                    join_btn.setOnClickListener { nextActivity() }
                                }
                            ref.child(user.uid).child("ROOM_NAME").setValue(map["ROOM_NAME"])
                            ref.child(user.uid).child("ROOM_ID").setValue(map["ROOM_ID"])
                            ref.child(user.uid).child("IS_ROOM_JOINED").setValue(true)
                                .addOnSuccessListener {
                                    settingsStorage.isRoom_joined = true
                                    ref.child("ROOM").child(room_id).child("JOINED_PERSON")
                                        .setValue(count + 1)
                                    showSnackBar("Successfully joined the room")
                                }
                                .addOnFailureListener { showSnackBar("Something went wrong try again with correct room id") }
                        } else showSnackBar("Joined person limit exceeded")
                    } else showSnackBar("No room exist with provided id")
                }.addOnFailureListener { showSnackBar("Something went wrong") }
        }
    }

    private fun showSnackBar(msg: String) {
        Snackbar.make(contextView, msg, Snackbar.LENGTH_LONG)
            .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
            .show()
    }

    val date: String
        get() {
            val date = Date()
            val sdm = SimpleDateFormat("dd-MM-yyyy hh:mm:ss", Locale.getDefault())
            return sdm.format(date)
        }

    companion object {
        fun generateID(text: String): String {
            var te = text
            val n = 16
            val t = System.currentTimeMillis().toString()
            te = text.uppercase()
            val str = t + te + t + te + t + te
            val sb = StringBuilder(n)
            for (i in 0 until n) {
                val index = (str.length
                        * Math.random()).toInt()
                sb.append(str[index])
            }
            return sb.toString()
        }
    }
}