package com.subhamgupta.roomiessaver.fragments

import android.widget.ProgressBar
import com.airbnb.lottie.LottieAnimationView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.auth.FirebaseUser
import com.subhamgupta.roomiessaver.adapters.IssueAdapter
import com.subhamgupta.roomiessaver.adapters.ChatAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.subhamgupta.roomiessaver.R
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.auth.FirebaseAuth
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.firestore.*
import java.util.ArrayList
import java.util.HashMap

class IssuesFragment : Fragment() {
    lateinit var progressBar: ProgressBar
    lateinit var lottieAnimationView: LottieAnimationView
    lateinit var db: FirebaseFirestore
    lateinit var recyclerView: RecyclerView
    lateinit var ref: DatabaseReference
    private lateinit var user_ref: DatabaseReference
    lateinit var user: FirebaseUser
    lateinit  var map: HashMap<String?, String>
    lateinit var data: ArrayList<Map<String, Any?>>
    lateinit var key: String
    lateinit var user_name: String
    lateinit var chatAdapter: ChatAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_issues, container, false)
        recyclerView = view.findViewById(R.id.issue_recycler)
        progressBar = view.findViewById(R.id.progress)
        lottieAnimationView = view.findViewById(R.id.animationView)
        recyclerView.setHasFixedSize(true)
        ref = FirebaseDatabase.getInstance().reference.child("ROOMIES")
        user = FirebaseAuth.getInstance().currentUser!!
        user_ref = ref.child(user.uid)
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        db = FirebaseFirestore.getInstance()
        progressBar.visibility = View.VISIBLE
        addItem()
        return view
    }

    private fun addItem() {
        map = HashMap()
        user_ref.get().addOnCompleteListener { task: Task<DataSnapshot> ->
            if (task.isSuccessful) {
                for (ds in task.result!!.children) map[ds.key] = ds.value.toString()
                key = map["ROOM_ID"].toString()
                user_name = map["USER_NAME"].toString()
                progressBar.visibility = View.GONE
                date
            }
        }
    }

    val date: Unit
        get() {
            data = ArrayList()
            val query: Query = db.collection(key + "_ISSUES")
            query.orderBy("DATE", Query.Direction.DESCENDING)
                    .addSnapshotListener { value: QuerySnapshot?, _: FirebaseFirestoreException? ->
                        if (value!!.isEmpty) {
//                            Log.e("VALUE", "empty")
                            lottieAnimationView.visibility = View.VISIBLE
                            lottieAnimationView.playAnimation()
                        } else {
                            lottieAnimationView.pauseAnimation()
                            lottieAnimationView.visibility = View.GONE
                        }
                        data.clear()
                        for (qds in value) {
                            val k: MutableMap<String, Any?> = HashMap()
                            k["ISSUE"] = qds["ISSUE"]
                            k["TIME"] = qds["TIME"]
                            k["DATE"] = qds["DATE"]
                            k["PERSON_TO"] = qds["PERSON_TO"]
                            k["PERSON_FROM"] = qds["PERSON_FROM"]
                            data.add(k)
                        }
                        println(data)
                        val g = data.sortedBy{ it["DATE"].toString() }
                        println(g)


                        chatAdapter = ChatAdapter(g, user_name, requireContext())
                        recyclerView.adapter = chatAdapter
                        try {
                            if (chatAdapter.itemCount != 0) recyclerView.post { recyclerView.smoothScrollToPosition(chatAdapter.itemCount - 1) }
                        } catch (e: Exception) {
                            Log.e("ERROR", e.message!!)
                        }
                    }
        }
}