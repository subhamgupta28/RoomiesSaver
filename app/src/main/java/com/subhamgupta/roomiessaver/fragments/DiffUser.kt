package com.subhamgupta.roomiessaver.fragments

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
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
import com.google.firebase.database.*
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.subhamgupta.roomiessaver.R
import com.subhamgupta.roomiessaver.adapters.PersonAdapter
import com.subhamgupta.roomiessaver.adapters.RoomAdapter
import com.subhamgupta.roomiessaver.models.RoomModel
import com.subhamgupta.roomiessaver.onClickPerson
import com.subhamgupta.roomiessaver.utility.SettingsStorage
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs

class DiffUser : Fragment(), onClickPerson {
    lateinit var ref: DatabaseReference
    lateinit var user_ref: DatabaseReference
    lateinit var personAdapter: PersonAdapter
    lateinit private var viewPager2: ViewPager2
    lateinit var list: List<String>
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

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
        user = mAuth.currentUser!!
        db = FirebaseFirestore.getInstance()
        ref = FirebaseDatabase.getInstance().reference.child("ROOMIES")
        reference = db
        user_ref = ref.child(user.uid)
        settingsStorage = SettingsStorage(requireContext())

        try {
            addItem()
        } catch (e: Exception) {

        }

        return view
    }

    fun addItem() {
        map = HashMap()
        user_ref.get().addOnCompleteListener { task: Task<DataSnapshot> ->
            if (task.isSuccessful) {
                for (ds in task.result?.children!!) map[ds.key] = ds.value.toString()
                key = map["ROOM_ID"].toString()
                user_name = map["USER_NAME"].toString()
                setData()
                showPerson()
            }
        }
    }

    fun setData() {
        room_mates = ArrayList()
        ref.child("ROOM").child(key).child("ROOM_MATES")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        user_key = HashMap()
                        for (ds in snapshot.children) {
                            user_key[(ds.key)!!.toInt()] =
                                (ds.value as Map<String?, Any?>?)!!["USER_NAME"]
                        }
                        val list = snapshot.value as List<Map<String, Any>>?
                        val uid: MutableList<Map<String, String>> = ArrayList()
                        for (i in list!!.indices) {
                            val g: MutableMap<String, String> = HashMap()
                            g["UUID"] = list[i]["UUID"].toString()
                            g["USER_NAME"] = list[i]["USER_NAME"].toString()
                            room_mates.add(list[i]["USER_NAME"].toString())
                            uid.add(g)
                        }
                        if (uid.size != 0) {
                            personAdapter = PersonAdapter(
                                uid,
                                activity!!.applicationContext,
                                db,
                                ref,
                                key,
                                this@DiffUser
                            )
                            viewPager2.adapter = personAdapter
                            viewPager2.clipToPadding = false
                            viewPager2.clipChildren = false
                            viewPager2.offscreenPageLimit = 10
                            val transformer = CompositePageTransformer()
                            transformer.addTransformer { page: View, position: Float ->
                                val a = 1 - abs(position)
                                page.scaleY = 0.85f + a * 0.15f
                            }
                            viewPager2.setPageTransformer(transformer)
                            Handler().postDelayed({
                                var t = 0
                                for (i in 0 until user_key.size) {
                                    val g = user_key.get(i).toString()
                                    if (g == user_name) {
                                        t = i
                                        break
                                    }
                                }
                                viewPager2.currentItem = t
                            }, 200)
                            indicator()

                        }
                    } catch (e: Exception) {

                    }

                }

                override fun onCancelled(error: DatabaseError) {}
            })
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

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            }
        })
    }

    fun showPerson() {
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

    override fun onIssue() {
        materialAlertDialogBuilder = MaterialAlertDialogBuilder(requireContext())
        val contactPopupView = layoutInflater.inflate(R.layout.issue_popup, null)
        issue_name = contactPopupView.findViewById(R.id.issue)
        issue_person = contactPopupView.findViewById(R.id.issue_person)
        issue_btn = contactPopupView.findViewById(R.id.issue_btn)
        issue_btn.setOnClickListener { createIssue() }
        val adapter =
            activity?.let { ArrayAdapter(requireContext(), R.layout.list_popup_item, room_mates) }
        issue_person.setAdapter(adapter)
        materialAlertDialogBuilder.setView(contactPopupView)
        materialAlertDialogBuilder.background = ColorDrawable(Color.TRANSPARENT)
        materialAlertDialogBuilder.show()
    }

    override fun sendSumMap(sumMap: MutableMap<Int, Int>) {
//        Log.e("SUMMAP", "$sumMap")
    }

    override fun sendSum(sum: Int) {}
    override fun openEdit() {
        editPopup()
    }

    fun editPopup() {
        var mcard = MaterialAlertDialogBuilder(requireContext())
        var view = View.inflate(context, R.layout.popup, null)
        var item = view.findViewById(R.id.item_bought) as TextInputEditText
        var amount = view.findViewById(R.id.amount_paid) as TextInputEditText
        val save = view.findViewById(R.id.save_btn) as Button
        var item_name = item.text.toString()
        var amount_paid = amount.text.toString().toInt()
        save.setOnClickListener {

        }
        mcard.setView(view)
        mcard.background = ColorDrawable(Color.TRANSPARENT)
        mcard.show()
    }

    fun createIssue() {
        val map1: MutableMap<String, String?> = HashMap()
        val issue = issue_name.text.toString()
        val person = issue_person.text.toString()
        val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT)
            .format(Calendar.getInstance().time)
        map1["ISSUE"] = issue
        map1["TIME"] = timeStamp
        map1["PERSON_TO"] = person
        map1["PERSON_FROM"] = user_name
        reference.collection(key + "_ISSUES")
            .add(map1)
            .addOnSuccessListener { documentReference: DocumentReference ->
                issue_name.setText("")
                issue_person.setText("")
//                Log.e("ISSUE_CREATED", documentReference.id)
            }
    }


}