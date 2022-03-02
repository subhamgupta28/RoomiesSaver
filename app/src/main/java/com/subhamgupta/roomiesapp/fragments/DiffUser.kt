package com.subhamgupta.roomiesapp.fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.Button
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.gms.tasks.Task
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.subhamgupta.roomiesapp.R
import com.subhamgupta.roomiesapp.adapters.PersonAdapter
import com.subhamgupta.roomiesapp.adapters.RoomAdapter
import com.subhamgupta.roomiesapp.models.PersonModel
import com.subhamgupta.roomiesapp.models.RoomModel
import com.subhamgupta.roomiesapp.onClickPerson
import com.subhamgupta.roomiesapp.utility.SettingsStorage
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.abs

class DiffUser : Fragment(), onClickPerson {
    lateinit var ref: DatabaseReference
    lateinit var user_ref: DatabaseReference
    private lateinit var viewPager2: ViewPager2
    lateinit var list: List<String>
    lateinit var roomRef:String
    lateinit var issue_name: TextInputEditText
    lateinit var issue_person: AutoCompleteTextView
    lateinit var issue_btn: Button
    lateinit var settingsStorage: SettingsStorage
    lateinit var uuids: List<String>
    lateinit var room_mates: MutableList<String>
    lateinit var user: FirebaseUser
    lateinit var key: String
    lateinit var user_name: String
    lateinit var materialAlertDialogBuilder: MaterialAlertDialogBuilder
    lateinit var reference: FirebaseFirestore
    lateinit var map: HashMap<String?, String>
    lateinit var mAuth: FirebaseAuth
    lateinit var roomAdapter: RoomAdapter
    lateinit var recyclerView: RecyclerView
    lateinit var db: FirebaseFirestore
    lateinit var user_key: HashMap<Int, Any?>
    lateinit var userMap: HashMap<String, Int?>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_diff_user, container, false)
        viewPager2 = view.findViewById(R.id.viewpager)
        recyclerView = view.findViewById(R.id.person_recycler)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        mAuth = FirebaseAuth.getInstance()
        userMap = HashMap()
        user = mAuth.currentUser!!
        db = FirebaseFirestore.getInstance()
        ref = FirebaseDatabase.getInstance().reference.child("ROOMIES")
        reference = db
        user_ref = ref.child(user.uid)
        settingsStorage = SettingsStorage(requireContext())
        roomRef = settingsStorage.roomRef.toString()
        try {
            addItem()
        } catch (e: Exception) {

        }

        return view
    }

    fun goToUser(position: Int, uuid: String){
//        Log.e("FROM_DIFF",uuid)
       try {
           viewPager2.setCurrentItem(userMap[uuid]!!, true)
       }catch (e:Exception){

       }

    }
    private fun addItem() {
        map = HashMap()
        user_ref.get().addOnCompleteListener { task: Task<DataSnapshot> ->
            userMap.clear()
            if (task.isSuccessful) {
                for (ds in task.result?.children!!) map[ds.key] = ds.value.toString()
                key = map[roomRef].toString()
                Log.e("KEY", key)
                user_name = map["USER_NAME"].toString()
                setTestData()
                showPerson()
            }
        }
    }
    private fun setTestData(){
        val query = ref.child("ROOM").child(key).child("ROOM_MATES").limitToFirst(100)
        val options = FirebaseRecyclerOptions.Builder<PersonModel>()
            .setQuery(query, PersonModel::class.java)
            .build()
        query.get().addOnCompleteListener { task: Task<DataSnapshot> ->

            for (i in task.result.children){
                val ds = i.value as HashMap<*,*>
//                Log.e("TASK 2", "${i.key}")
                userMap[ds["UUID"].toString()] = i.key.toString().toInt()
//                Log.e("TASK 1", "${ds["UUID"]}")
            }
//            Log.e("TASK UU", "$userMap")
        }
        val sdom = if (settingsStorage.startDateMillis?.toInt()==0) LocalDate.now().withDayOfMonth(1).atStartOfDay(
            ZoneId.systemDefault()).toInstant().epochSecond*1000
        else settingsStorage.startDateMillis
        val personTest = sdom?.let { PersonAdapter(options, requireActivity(), reference, key, it) }
        viewPager2.adapter = personTest
        personTest?.startListening()
        viewPager2.clipToPadding = false
        viewPager2.clipChildren = false
        viewPager2.offscreenPageLimit = 10
        val transformer = CompositePageTransformer()
        transformer.addTransformer { page: View, position: Float ->
            val a = 1 - abs(position)
            page.scaleY = 0.85f + a * 0.15f
        }
        viewPager2.setPageTransformer(transformer)
        indicator()
    }
    fun recP(position: Int, boolean: Boolean){
        val temp = recyclerView[position].findViewById<MaterialCardView>(R.id.materialcard)
        if (boolean){
            temp.setCardBackgroundColor(Color.parseColor("#814285F4"))
            temp.scaleX = 1.05F
            temp.scaleY = 1.05F
        }else{
            temp.setCardBackgroundColor(Color.parseColor("#284285F4"))
            temp.scaleX = 0.95F
            temp.scaleY = 0.95F
        }
    }
    private fun indicator() {
        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                try {
                    for (i in 0..roomAdapter.itemCount) {
                        if (
                            i == position
                        ) {
                            recP(position, true)

                        } else {
                           recP(i, false)
                        }
                    }
                } catch (e: java.lang.Exception) {

                }

            }
        })
    }

    private fun showPerson() {
        list = ArrayList()
        uuids = ArrayList()
        val options = FirebaseRecyclerOptions.Builder<RoomModel>()
            .setQuery(ref.child("ROOM").child(key).child("ROOM_MATES"), RoomModel::class.java)
            .build()
        roomAdapter = RoomAdapter(options, requireContext(), ref, user_name, this@DiffUser)
        recyclerView.adapter = roomAdapter
        roomAdapter.startListening()

    }

    override fun onClick(position: Int) {
        viewPager2.currentItem = position
    }

}