package com.subhamgupta.roomiesapp.fragments

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.subhamgupta.roomiesapp.Contenst.Companion.DATE_STRING
import com.subhamgupta.roomiesapp.R
import com.subhamgupta.roomiesapp.activities.MainActivity
import com.subhamgupta.roomiesapp.onItemClick
import com.subhamgupta.roomiesapp.utility.SettingsStorage
import java.text.SimpleDateFormat
import java.util.*

class RoomCreation(
    var mAuth: FirebaseAuth,
    var ref: DatabaseReference,
    var settingsStorage: SettingsStorage,
    var contextView: View,
    var onItemClick: onItemClick
) : Fragment() {
    lateinit var join_btn: Button
    lateinit var generate_btn: Button
    lateinit var logout: Button
    lateinit var uid: String
    lateinit var user: FirebaseUser
    lateinit var room_layout: TextInputLayout
    lateinit var join_layout: TextInputLayout
    lateinit var limit_layout: TextInputLayout
    lateinit var room_name: TextInputEditText
    lateinit var join_room: TextInputEditText
    lateinit var limit_person: TextInputEditText
    lateinit var map: HashMap<String, Any?>
    lateinit var user_name: String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_room_creation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        join_btn = view.findViewById(R.id.join_btn)
        generate_btn = view.findViewById(R.id.generate_id_btn)
        join_room = view.findViewById(R.id.join_room)
        join_layout = view.findViewById(R.id.join_layout)
        room_layout = view.findViewById(R.id.room_layout)
        room_name = view.findViewById(R.id.room_name)
        limit_layout = view.findViewById(R.id.limit_layout)
        limit_person = view.findViewById(R.id.limit_person)
        logout = view.findViewById(R.id.logout)

        user = mAuth.currentUser!!

        if (!settingsStorage.room_id.equals("null"))
            join_room.setText(settingsStorage.room_id)

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
        onItemClick.logout()
    }


    private fun createRoom() {
        val name = room_name.text.toString()
        val id = generateID(name)
        val room = "ROOM"
        val l: String = limit_person.text.toString()
        var limit = 10
        if (l != "") limit = l.toInt()
        ref.child(user.uid).get().addOnCompleteListener {
            if (it.isSuccessful) {
                val mp = it.result.value as MutableMap<*, *>
                val ob = getNewKey(mp, id)
                val finalKey = ob["F_KEY"].toString()
                val flag = ob["IS_JOIN"].toString().toBoolean()
                ref.child(room).child(id).child("ROOM_ID").setValue(id)
                ref.child(room).child(id).child("CREATED_ON").setValue(date)
                ref.child(room).child(id).child("ROOM_NAME").setValue(name)
                ref.child(room).child(id).child("START_DATE_MONTH").setValue(0)
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
                    }.addOnSuccessListener {
                        settingsStorage.isRoom_joined = true
                        Snackbar.make(contextView, "Room Created Successfully", Snackbar.LENGTH_LONG).show()
                        ref.child(uid).child(finalKey).setValue(id)
                        ref.child(uid).child("ROOM_NAME").setValue(name)
                        ref.child(uid).child("IS_ROOM_JOINED").setValue(true)
                            .addOnFailureListener { e: Exception -> Log.e("ERROR", e.message!!) }
                            .addOnSuccessListener {
                                settingsStorage.isRoom_joined = true
                                nextActivity()
                            }
                    }
            }
        }
    }

    private fun nextActivity() {
        val bundle = ActivityOptions.makeSceneTransitionAnimation(activity).toBundle()
        startActivity(Intent(activity, MainActivity::class.java), bundle)
    }
    private fun getNewKey(mp: MutableMap<*, *>, room_id:String): MutableMap<String, Any> {
        val kys = mp.keys
        val nxt = ArrayList<Int>()
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
        return mutableMapOf( "F_KEY" to finalKey, "IS_JOIN" to flag)
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
                            map[ds.key] = ds.value
                            if (ds.key == "ROOM_MATES") {
                                list = ds.value as MutableList<Map<String?, Any?>>
                            }
                        }
                        val count = map["JOINED_PERSON"] as Long
                        val limit = map["LIMIT"] as Long
                        if (count <= limit) {
                            val map1: MutableMap<String?, Any?> = HashMap()
                            map1["KEY"] = room_id
                            map1["MONEY_PAID"] = 0
                            map1["IS_JOINED"] = true
                            map1["UUID"] = user.uid
                            map1["USER_NAME"] = user_name
                            list.add(map1)

                            ref.child(user.uid).get().addOnCompleteListener {
                                if (it.isSuccessful) {
                                    val mp = it.result.value as MutableMap<*, *>
                                    val ob = getNewKey(mp, room_id)
                                    val finalKey = ob["F_KEY"].toString()
                                    val flag = ob["IS_JOIN"].toString().toBoolean()
                                    if (!flag) {
                                        Log.e("F_KEY", finalKey)
                                        ref.child("ROOM").child(room_id).child("ROOM_MATES").setValue(list)
                                            .addOnFailureListener { e: Exception ->
                                                Log.e("ERROR", e.message!!)
                                            }
                                            .addOnSuccessListener {
                                                join_btn.text = "Enter"
                                                join_btn.setOnClickListener { nextActivity() }
                                            }
                                        ref.child(user.uid).child("ROOM_NAME")
                                            .setValue(map["ROOM_NAME"])
                                        ref.child(user.uid).child(finalKey).setValue(map["ROOM_ID"])
                                        ref.child(user.uid).child("IS_ROOM_JOINED").setValue(true)
                                            .addOnSuccessListener {
                                                settingsStorage.isRoom_joined = true
                                                ref.child("ROOM").child(room_id)
                                                    .child("JOINED_PERSON").setValue(count + 1)
                                                showSnackBar("Successfully joined the room")
                                            }
                                            .addOnFailureListener { showSnackBar("Something went wrong try again with correct room id") }
                                    } else {
                                        showSnackBar("Already Joined in this room")
                                        settingsStorage.isRoom_joined = true
                                        join_btn.text = "Enter"
                                        join_btn.setOnClickListener { nextActivity() }
                                    }
                                }
                            }
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
            val sdm = SimpleDateFormat(DATE_STRING, Locale.getDefault())
            return sdm.format(date)
        }

    companion object {
        fun generateID(text: String): String {
            var te = text.trim()
            val n = 10
            val t = System.currentTimeMillis().toString()
            te = text.uppercase()
            val str = t + te + t + te + t + te
            val sb = StringBuilder(n)
            for (i in 0 until n) {
                val index = (str.length
                        * Math.random()).toInt()
                sb.append(str[index])
            }
            return sb.toString().trim()
        }
    }
}