package com.subhamgupta.roomiessaver.activities

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ShareCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Task
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.subhamgupta.roomiessaver.R
import com.subhamgupta.roomiessaver.fragments.RoomCreation
import com.subhamgupta.roomiessaver.utility.SettingsStorage
import java.util.*


class RoomDetails : AppCompatActivity() {
    lateinit var user: FirebaseUser
    lateinit var mAuth: FirebaseAuth
    lateinit var chipGroup: ChipGroup
    lateinit var db: FirebaseFirestore
    lateinit var details: MutableMap<String?, Any?>
    lateinit var ref: DatabaseReference
    lateinit var user_ref: DatabaseReference
    lateinit var recyclerView: RecyclerView
    lateinit var r_id: TextView
    lateinit var r_name: TextView
    lateinit var limit: TextInputEditText
    lateinit var rname: TextInputEditText
    lateinit var c_date: TextView
    lateinit var materialAlertDialogBuilder: MaterialAlertDialogBuilder
    lateinit var r_limit: TextView
    lateinit var editrdetail: Button
    lateinit var leaveRoom: Button
    lateinit var shareId: Button
    lateinit var save_btn: Button
    lateinit var darkMode: SwitchMaterial
    lateinit var thisMonthData: SwitchMaterial
    lateinit var settingsStorage: SettingsStorage
    var name: String? = null
    var l: String? = null
    var joinedPerson: Int? = null
    lateinit var map: MutableMap<String?, String>
    lateinit var key: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_details)
        recyclerView = findViewById(R.id.detail_recycler)
        chipGroup = findViewById(R.id.chips)
        r_id = findViewById(R.id.r_id)
        r_name = findViewById(R.id.r_name)
        c_date = findViewById(R.id.c_date)
        r_limit = findViewById(R.id.limit)
        shareId = findViewById(R.id.shareid)
        leaveRoom = findViewById(R.id.leaveroom)
        editrdetail = findViewById(R.id.limit_btn)
        thisMonthData = findViewById(R.id.this_month_data)
        darkMode = findViewById(R.id.dark_mode_switch)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        mAuth = FirebaseAuth.getInstance()
        user = mAuth.currentUser!!
        ref = FirebaseDatabase.getInstance().reference.child("ROOMIES")
        user_ref = ref.child(user.uid)
        db = FirebaseFirestore.getInstance()
        settingsStorage = SettingsStorage(this)

        leaveRoom.setOnClickListener {
            leaveRoom()
        }
        shareId.setOnClickListener {
            shareRoomId()
        }
        thisMonthData.isChecked = settingsStorage.isMonth == true
        darkMode.isChecked = settingsStorage.darkMode == true
        thisMonthData.setOnCheckedChangeListener { _, isCheck ->
            settingsStorage.isMonth = isCheck
        }
        darkMode.setOnCheckedChangeListener { _, isCheck ->
            settingsStorage.darkMode = isCheck
            if (isCheck)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        addItem()
    }

    fun shareRoomId() {
        val shareIntent = ShareCompat.IntentBuilder.from(this)
            .setType("text/plain")
            .setText("${settingsStorage.username} has invited you to join the room. Click on link to join. https://roomies.app/${settingsStorage.room_id}")
            .intent
        if (shareIntent.resolveActivity(packageManager) != null) {
            startActivity(shareIntent)
        }
    }

    fun addItem() {
        map = HashMap()
        details = HashMap()
        user_ref.get().addOnCompleteListener { task: Task<DataSnapshot> ->
            if (task.isSuccessful) {
                for (ds in task.result!!.children) (map as HashMap<String?, String>)[ds.key] =
                    ds.value.toString()
                key = map["ROOM_ID"].toString()
                ref.child("ROOM").child(key).addValueEventListener(object : ValueEventListener {
                    @SuppressLint("SetTextI18n")
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (ds in snapshot.children) {
                            (details as HashMap<String?, Any?>)[ds.key] = ds.value
                        }
                        val list = details["ROOM_MATES"] as List<Map<String, Any>>?
                        for (i in list!!.indices) {
                            val chip = Chip(this@RoomDetails)
                            chip.text = list[i]["USER_NAME"].toString()
                            chip.chipStrokeWidth = 0f
                            if(user.uid==list[i]["UUID"].toString())
                                chip.setTextColor(Color.parseColor("#4285F4"))
                            chipGroup.addView(chip)
                        }
                        r_id.text = "ROOM ID: $key"
                        r_name.text = "ROOM NAME: " + details["ROOM_NAME"]
                        name = details["ROOM_NAME"].toString()
                        l = details["LIMIT"].toString()
                        c_date.text = "CREATED ON: " + details["CREATED_ON"]
                        r_limit.text = "LIMIT: " + details["LIMIT"]
                        joinedPerson = details["JOINED_PERSON"].toString().toInt()
                        editrdetail.setOnClickListener {
                            editLimit()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
            }
        }
    }

    fun editLimit() {
        materialAlertDialogBuilder = MaterialAlertDialogBuilder(this)
        val view = View.inflate(this, R.layout.edit_details, null)
        rname = view.findViewById(R.id.rname)
        limit = view.findViewById(R.id.rlimit)
        save_btn = view.findViewById(R.id.save_btn)
        rname.setText(name)
        limit.setText(l)
        save_btn.setOnClickListener {
            if (rname.text.toString().isNotEmpty() || limit.text.toString().isNotEmpty()) {
                val map = HashMap<String, Any>()
                map["LIMIT"] = limit.text.toString().toInt()
                map["ROOM_NAME"] = rname.text.toString()
                ref.child("ROOM").child(key).updateChildren(map).addOnCompleteListener {

                }
            }

        }
        materialAlertDialogBuilder.setView(view)
        materialAlertDialogBuilder.background = ColorDrawable(Color.TRANSPARENT)
        materialAlertDialogBuilder.show()

    }

    fun leaveRoom() {
        val dataSnapshotTask = ref.child("ROOM").child(key).child("ROOM_MATES").get()
        dataSnapshotTask.addOnSuccessListener { dataSnapshot ->
//            Log.e("data", dataSnapshot.toString())
            val snap: MutableMap<String, Any?>? = HashMap()
            var data: HashMap<String?, Any?>? = null
            for (d in dataSnapshot.children) {
                val v = d.value as HashMap<String?, Any?>
                for (l in v) {
                    if (l.value.toString() != user.uid) {
                        data = v
                    }
                }
                snap!![d.key!!] = d.value as HashMap<String?, Any?>

            }
            var c = ""
            for (f in snap!!) {
                if (f.value == data)
                    c = f.key
            }
            snap.remove(c)
            println(snap)
            println(data)
            ref.child("ROOM").child(key).child("ROOM_MATES").setValue(snap).addOnSuccessListener {
                user_ref.child("ROOM_ID").removeValue()
                user_ref.child("ROOM_NAME").removeValue()
                user_ref.child("IS_ROOM_JOINED").setValue(false)
                ref.child("ROOM").child(key).child("JOINED_PERSON").setValue(joinedPerson?.minus(1))
                settingsStorage.isRoom_joined = false
                settingsStorage.room_name = ""
                //supportFinishAfterTransition()
                settingsStorage.isLoggedIn = false
                goToRoomCreation(1)
            }
        }
    }
    private fun goToRoomCreation(int: Int){
        val bundle = ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
        val intent =  Intent(applicationContext, AccountCreation::class.java)
        intent.putExtra("CODE", int)
        startActivity(intent, bundle)
    }
    override fun onStop() {
        super.onStop()
        supportFinishAfterTransition()
    }
}