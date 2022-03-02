package com.subhamgupta.roomiesapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.subhamgupta.roomiesapp.R
import com.subhamgupta.roomiesapp.adapters.RationAdapter
import com.subhamgupta.roomiesapp.utility.SettingsStorage
import java.time.LocalDate
import java.time.ZoneId

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
        } catch (e: Exception) {

        }
        swipeRefreshLayout.setOnRefreshListener {
            try {
                getData()
            } catch (e: Exception) {

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
        val query: Query = db.collection(roomId + "_RATION")
        val sdom = LocalDate.now().withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault())
            .toInstant().epochSecond
        query.orderBy("TIME_STAMP", Query.Direction.DESCENDING)
            .addSnapshotListener { value: QuerySnapshot?, _: FirebaseFirestoreException? ->
                datas.clear()
                if (value != null) {
                    emptyText.visibility = View.INVISIBLE
                    if (!value.isEmpty) {
                        for (ds in value) {
                            val d = ds.data as MutableMap<String, Any>
                            datas.add(d)
                        }
                        rationAdapter = fragmentManager?.let {
                            RationAdapter(datas, requireContext(), it)
                        }!!
                        recyclerView.adapter = rationAdapter
                        swipeRefreshLayout.isRefreshing = false
                    } else {
                        emptyText.visibility = View.VISIBLE
                    }

                } else {
                    emptyText.visibility = View.VISIBLE
                }

            }

    }
}


