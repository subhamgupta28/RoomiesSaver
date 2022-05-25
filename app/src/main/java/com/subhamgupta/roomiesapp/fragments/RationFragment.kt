package com.subhamgupta.roomiesapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.subhamgupta.roomiesapp.R

class RationFragment: Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ration, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        db = FirebaseFirestore.getInstance()
//        settingsStorage = activity?.let { SettingsStorage(it.applicationContext) }!!
//        roomId = settingsStorage.room_id
//        try {
//            getData()
//        } catch (e: Exception) {
//
//        }
//        swipeRefreshLayout.setOnRefreshListener {
//            try {
//                getData()
//            } catch (e: Exception) {
//
//            }
//        }
//        add_ration_btn.setOnClickListener {
//            val bottomFragment = BottomFragment()
//            fragmentManager?.let { it1 -> bottomFragment.show(it1, "Enter new ration") }
//            val bundle = ActivityOptions.makeSceneTransitionAnimation(activity).toBundle()
//            startActivity(Intent(context, RationActivity::class.java), bundle)


//        }
    }

//    fun openSheet() {
//        val bottomFragment = BottomFragment()
//        fragmentManager?.let { it1 -> bottomFragment.show(it1, "Enter new ration") }
//    }
//
//    private fun getData() {
//        datas = ArrayList()
//        val query: Query = db.collection(roomId + "_RATION")
//        val sdom = LocalDate.now().withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault())
//            .toInstant().epochSecond
//        query.orderBy("TIME_STAMP", Query.Direction.DESCENDING)
//            .addSnapshotListener { value: QuerySnapshot?, _: FirebaseFirestoreException? ->
//                datas.clear()
//                if (value != null) {
//                    emptyText.visibility = View.INVISIBLE
//                    if (!value.isEmpty) {
//                        for (ds in value) {
//                            val d = ds.data as MutableMap<String, Any>
//                            datas.add(d)
//                        }
//                        rationAdapter = fragmentManager?.let {
//                            RationAdapter(datas, requireContext(), it)
//                        }!!
//                       recyclerView.adapter = rationAdapter
//                        swipeRefreshLayout.isRefreshing = false
//                    } else {
//                        emptyText.visibility = View.VISIBLE
//                    }
//
//                } else {
//                    emptyText.visibility = View.VISIBLE
//                }
//
//            }
//
//    }
}


