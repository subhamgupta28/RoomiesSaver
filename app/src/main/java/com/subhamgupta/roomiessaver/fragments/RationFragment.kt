package com.subhamgupta.roomiessaver.fragments

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.subhamgupta.roomiessaver.R
import com.subhamgupta.roomiessaver.activities.RationActivity
import com.subhamgupta.roomiessaver.adapters.RationAdapter
import com.subhamgupta.roomiessaver.utility.SettingsStorage
import io.reactivex.rxjava3.internal.util.LinkedArrayList
import java.lang.Exception
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class RationFragment : Fragment() {
    lateinit var recyclerView: RecyclerView

    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    lateinit var add_ration_btn: Button
    lateinit var emptyText: LinearLayout
    lateinit var db: FirebaseFirestore
    lateinit var settingsStorage: SettingsStorage
    var roomId: String? = null
    lateinit var datas: MutableList<Map<String, Any?>?>
    lateinit var rationAdapter: RationAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ration, container, false)
        recyclerView = view.findViewById(R.id.ration_recycler)
        swipeRefreshLayout = view.findViewById(R.id.ration_swipe)
        emptyText = view.findViewById(R.id.emptytext)
        add_ration_btn = view.findViewById(R.id.new_ration)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager =
            StaggeredGridLayoutManager(2, LinearLayout.VERTICAL)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseFirestore.getInstance()
        settingsStorage = activity?.let { SettingsStorage(it.applicationContext) }!!
        roomId = settingsStorage.room_id
        try {
            getData()
        }catch (e:Exception){

        }
        swipeRefreshLayout.setOnRefreshListener {
            try {
                getData()
            }catch (e:Exception){

            }
        }
        add_ration_btn.setOnClickListener {
            val bottomFragment = BottomFragment()
            fragmentManager?.let { it1 -> bottomFragment.show(it1, "Enter new ration") }
//            val bundle = ActivityOptions.makeSceneTransitionAnimation(activity).toBundle()
//            startActivity(Intent(context, RationActivity::class.java), bundle)


        }
    }
    fun openSheet() {
        val bottomFragment = BottomFragment()
        fragmentManager?.let { it1 -> bottomFragment.show(it1, "Enter new ration") }
    }

    private fun getData() {
        datas = ArrayList()
        db.collection(roomId + "_RATION")
            .addSnapshotListener { value: QuerySnapshot?, _: FirebaseFirestoreException? ->
                datas.clear()

                if (value != null) {
                    emptyText.visibility = View.INVISIBLE
                    if (!value.isEmpty){
                        for (ds in value) {
                            val k: MutableMap<String, Any?> = HashMap()
                            k["DATE"] = ds["DATE"]
                            k["IMG_NAME"] = ds["IMG_NAME"]
                            k["IMG_URL"] = ds["IMG_URL"]
                            k["NOTE"] = ds["NOTE"]
                            datas.add(k)
                        }



                        val g = datas.sortedWith(compareBy {
                            LocalDate.parse(it?.get("DATE").toString(), DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm:ss"))
                                .atStartOfDay(ZoneId.systemDefault()).toInstant().epochSecond
                        }) as MutableList
                        println(g)
                        g.reverse()
                        try {
                            rationAdapter = fragmentManager?.let {
                                RationAdapter(g, requireContext(),
                                    it
                                )
                            }!!
                        }catch (e:Exception){

                        }

                        recyclerView.adapter = rationAdapter
                        swipeRefreshLayout.isRefreshing = false
                    }else{
                        emptyText.visibility = View.VISIBLE
                    }

                }else{
                    emptyText.visibility = View.VISIBLE
                }

            }

    }
}


