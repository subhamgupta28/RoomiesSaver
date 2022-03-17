package com.subhamgupta.roomiesapp.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.gms.tasks.Task
import com.google.android.material.card.MaterialCardView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.subhamgupta.roomiesapp.R
import com.subhamgupta.roomiesapp.adapters.SummaryAdapter
import com.subhamgupta.roomiesapp.utility.SettingsStorage
import org.json.JSONObject
import java.time.*
import java.util.*
import java.util.concurrent.atomic.AtomicLong
import kotlin.collections.HashMap

class Summary : Fragment() {
    lateinit var user: FirebaseUser
    lateinit var mAuth: FirebaseAuth
    lateinit var db: FirebaseFirestore
    lateinit var materialCardView: MaterialCardView
    lateinit var total_spend: TextView
    lateinit var emptyBox: LinearLayout
    lateinit var roomRef: String
    lateinit var key: String
    lateinit var switchMonth: SwitchMaterial
    lateinit var user_name: String
    lateinit var map: HashMap<String?, String>
    lateinit var data: MutableList<Map<String, Any?>?>
    lateinit var ref: DatabaseReference
    lateinit var user_ref: DatabaseReference
    lateinit var recyclerView: RecyclerView
    lateinit var testAdapter: SummaryAdapter
    lateinit var settingsStorage: SettingsStorage
    var content: View? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_summary, container, false)
        recyclerView = view.findViewById(R.id.recycler)
        total_spend = view.findViewById(R.id.total_spends)
        emptyBox = view.findViewById(R.id.emptytext)
        switchMonth = view.findViewById(R.id.switch1)
        materialCardView = view.findViewById(R.id.tlayout)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager =
            StaggeredGridLayoutManager(2, LinearLayout.VERTICAL)
        content = view.findViewById(android.R.id.content)
        mAuth = FirebaseAuth.getInstance()
        user = mAuth.currentUser!!
        db = FirebaseFirestore.getInstance()
        ref = FirebaseDatabase.getInstance().reference.child("ROOMIES")
        settingsStorage = SettingsStorage(requireContext())
        user_ref = ref.child(user.uid)
        roomRef = settingsStorage.roomRef.toString()
        try {

            settingsStorage.isMonth?.let {
                switchMonth.text = if (it) "This Month's" else "All Time"
                switchMonth.isChecked = it
                addItem(it)
            }
        } catch (e: Exception) {
            Log.e("SUMMARY", e.message.toString())
        }

        switchMonth.setOnCheckedChangeListener { _, isChecked ->
            settingsStorage.isMonth = isChecked
            switchMonth.text = if (isChecked) "This Month's" else "All Time"
            setData(isChecked)
        }
        return view
    }

    fun addItem(ifMonth: Boolean) {
        map = HashMap()
        user_ref.get().addOnCompleteListener { task: Task<DataSnapshot> ->
            if (task.isSuccessful) {
                for (ds in task.result!!.children)
                    map[ds.key] = ds.value.toString()
                key = map[roomRef].toString()
                user_name = map["USER_NAME"].toString()
//                Log.e("KEY", key)
                try {
                    setData(ifMonth)
                } catch (e: Exception) {
                    //e.printStackTrace();
                }
            }
        }
    }

    private fun setData(ifMonth: Boolean) {
        data = ArrayList()
        val sumMap = HashMap<String, Int>()
        val query: Query = db.collection(key)
        var sdom = 0L
        if (ifMonth){
            sdom = if (settingsStorage.startDateMillis?.toInt()==0)LocalDate.now().withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant().epochSecond*1000
            else settingsStorage.startDateMillis!!
        }
        query.whereGreaterThanOrEqualTo("TIME_STAMP", sdom)
            .orderBy("TIME_STAMP", Query.Direction.DESCENDING)
            .addSnapshotListener { value: QuerySnapshot?, _: FirebaseFirestoreException? ->
                val sum = AtomicLong()
                data.clear()
                sumMap.clear()
                if (value != null && !value.isEmpty) {
                    emptyBox.visibility = View.INVISIBLE
                    try {
                        for (qds in value) {
                            val k = qds.data as MutableMap<String, Any>
                            val by = qds["BOUGHT_BY"].toString()
                            if (sumMap.containsKey(by))
                                sumMap[by] =
                                    sumMap[by]!! + qds["AMOUNT_PAID"].toString().toInt()
                            else
                                sumMap[by] = qds["AMOUNT_PAID"].toString().toInt()
                            sum.addAndGet((qds["AMOUNT_PAID"] as Long?)!!)
                            data.add(k)
                        }
                        val json = JSONObject(sumMap.toMap()).toString()
                        SettingsStorage(requireContext()).json = json
                        "Total ₹$sum".also { total_spend.text = it }
                        testAdapter = SummaryAdapter( requireContext())
                        recyclerView.adapter = testAdapter
                        testAdapter.setDataTo(data)
                    } catch (e: Exception) {
                        Log.e("ERROR", e.message + "")
                    }
                } else{
                    emptyBox.visibility = View.VISIBLE
                }
            }
    }



}