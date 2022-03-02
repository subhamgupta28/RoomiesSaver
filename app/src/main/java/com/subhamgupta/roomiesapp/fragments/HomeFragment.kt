package com.subhamgupta.roomiesapp.fragments

import android.graphics.Color
import android.os.Bundle
import android.text.format.DateUtils
import android.transition.Fade
import android.transition.TransitionManager
import android.util.Log
import com.subhamgupta.roomiesapp.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import app.futured.donut.DonutProgressView
import app.futured.donut.DonutSection
import com.google.android.material.card.MaterialCardView
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.majorik.sparklinelibrary.SparkLineLayout
import com.subhamgupta.roomiesapp.Contenst.Companion.DATE_STRING
import com.subhamgupta.roomiesapp.adapters.HomeAdapter
import com.subhamgupta.roomiesapp.adapters.SummaryAdapter
import com.subhamgupta.roomiesapp.interfaces.HAdapToHFrag
import com.subhamgupta.roomiesapp.interfaces.HomeToMain
import com.subhamgupta.roomiesapp.utility.SettingsStorage
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.*


class HomeFragment(var homeToMain: HomeToMain? = null) : Fragment(), HAdapToHFrag {
    lateinit var personRecycler: RecyclerView
    lateinit var totalSpending: TextView
    lateinit var sparkLineLayout: SparkLineLayout
    lateinit var updatedOn: TextView
    lateinit var welcomeText: TextView
    lateinit var emptyLayout: LinearLayout
    lateinit var db: FirebaseFirestore
    lateinit var latestItem: RecyclerView
    lateinit var ref: FirebaseFirestore
    lateinit var k: TextView
    lateinit var todayAmount: TextView
    lateinit var data: MutableList<Map<String, Any?>?>
    lateinit var dbref: DatabaseReference
    lateinit var settingsStorage: SettingsStorage
    lateinit var key: String
    lateinit var donutProgressView: DonutProgressView
    lateinit var homeAdapter: HomeAdapter
    lateinit var kt: LinearLayout
    lateinit var summaryAdapter: SummaryAdapter
    lateinit var line1: MaterialCardView
    lateinit var topAppBar: Toolbar
    var totalAmount: Int = 0
    var todayTotal: Int = 0


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)


    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        personRecycler = view.findViewById(R.id.p_recycle)
        latestItem = view.findViewById(R.id.item_recycle)
        updatedOn = view.findViewById(R.id.updated_on)
        totalSpending = view.findViewById(R.id.total_spends)
        todayAmount = view.findViewById(R.id.today_amount)
        donutProgressView = view.findViewById(R.id.donut_view)
        welcomeText = view.findViewById(R.id.welcometext)
        sparkLineLayout = view.findViewById(R.id.spark)
        emptyLayout = view.findViewById(R.id.emptytext)
        topAppBar = view.findViewById(R.id.topAppBar)
        kt = view.findViewById(R.id.kt)
        k = view.findViewById(R.id.k)
        line1 = view.findViewById(R.id.line1)


        personRecycler.setHasFixedSize(true)
        personRecycler.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        latestItem.setHasFixedSize(true)
        latestItem.layoutManager =
            StaggeredGridLayoutManager(2, LinearLayout.VERTICAL)
        ref = FirebaseFirestore.getInstance()
        db = FirebaseFirestore.getInstance()
        settingsStorage = SettingsStorage(requireContext())
        key = settingsStorage.room_id.toString()
        dbref = FirebaseDatabase.getInstance().reference.child("ROOMIES")
//        welcomeText.text = "Welcome ${settingsStorage.username}"
        ref.collection(key)
        setData()
        setRecentPurchase()
        topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.mspends -> {
                    true
                }
                R.id.msummary -> {

                    true
                }
                R.id.mration -> {

                    true
                }
                else -> false
            }
        }
    }

    private fun runTransition(view: ViewGroup, isVisible: Boolean) {
        TransitionManager.beginDelayedTransition(view, Fade())
        view.visibility = if (isVisible)
            View.VISIBLE
        else
            View.GONE
    }


    private fun setData() {
        dbref.child("ROOM").child(key).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    for (ds in dataSnapshot.children) {
                        if (ds.key.toString() == "LAST_UPDATED") {
                            updatedOn.text = "Updated ${getTimeAgo(ds.value.toString())}"
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ERROR", e.localizedMessage)
                }

            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
            }
        })

    }

    override fun onResume() {
        super.onResume()
        setRecentPurchase()
    }

    private fun getTimeAgo(time: String): String {
        val sdf = SimpleDateFormat(DATE_STRING, Locale.getDefault())
        var ago = ""
        try {
            val time = sdf.parse(time).time
            ago = DateUtils.getRelativeTimeSpanString(
                time,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
            ).toString()

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ago
    }

    private fun setRecentPurchase() {
        data = ArrayList()
        val map = ArrayList<MutableMap<String, String>>()
        val list: ArrayList<Int> = ArrayList()

        var thisMonthAmount = 0
        sparkLineLayout.setData(mutableListOf(0, 1, 0, 1, 0, 1, 0) as ArrayList<Int>)
        val sdom = if (settingsStorage.startDateMillis?.toInt()==0)LocalDate.now().withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant().epochSecond*1000
                    else settingsStorage.startDateMillis

        val query: Query = db.collection(key)
        query.whereGreaterThanOrEqualTo("TIME_STAMP", sdom!!)//1643673600
            .orderBy("TIME_STAMP", Query.Direction.DESCENDING)
            .addSnapshotListener { value: QuerySnapshot?, _: FirebaseFirestoreException? ->
                data.clear()
                map.clear()
                list.clear()
                todayTotal = 0
                totalAmount = 0
                if (value != null && !value.isEmpty) {
                    runTransition(line1, true)
                    runTransition(emptyLayout, false)
                    runTransition(kt, true)
                    k.visibility = View.VISIBLE
                    try {
                        val donutList = ArrayList<DonutSection>()
                        val userMap = mutableMapOf<String, HashMap<String, Any?>>()
                        for (qds in value) {
                            val k = qds.data as MutableMap<String, Any?>
                            val user = qds["BOUGHT_BY"].toString()
                            val amount = qds["AMOUNT_PAID"].toString().toInt()
                            val mon = qds["AMOUNT_PAID"].toString()
                            val uuid = qds["UUID"].toString()
                            thisMonthAmount++
                            if (userMap.containsKey(uuid)) {
                                userMap[uuid]?.set("USER", user)
                                userMap[uuid]?.set(
                                    "AMOUNT",
                                    userMap[uuid]?.get("AMOUNT").toString().toInt().plus(amount)
                                )
                            } else {
                                userMap[uuid] = mutableMapOf<String, Any?>(
                                    "USER" to user,
                                    "AMOUNT" to amount
                                ) as HashMap<String, Any?>
                            }

                            list.add(amount)
                            totalAmount = if (mon.isEmpty())
                                totalAmount.plus(0)
                            else
                                totalAmount.plus(mon.toInt())
                            if (DateUtils.isToday(qds["TIME_STAMP"].toString().toLong())) {
                                todayTotal = if (mon.isEmpty())
                                    todayTotal.plus(0)
                                else
                                    todayTotal.plus(mon.toInt())
                                data.add(k)
                            }
                        }
                        sparkLineLayout.visibility = View.VISIBLE
                        Log.e("USER_MAP", "$userMap")

                        for (i in userMap) {
                            val mj = i.value as HashMap<*, *>
                            val mp = mutableMapOf(
                                "USER_NAME" to mj["USER"].toString(),
                                "AMOUNT" to mj["AMOUNT"].toString(),
                                "UUID" to i.key
                            )
                            try {
                                val section1 = DonutSection(
                                    name = mj["USER"].toString(),
                                    color = getColor(i.key),
                                    amount = mj["AMOUNT"].toString().toFloat()
                                )
                                donutList.add(section1)
                            } catch (e: Exception) {
                                Log.e("HOME_ERROR", "$e")
                            }
                            map.add(mp)
                        }
                        sparkLineLayout.setData(list)
                        "₹$totalAmount".also { totalSpending.text = it }
                        donutProgressView.cap = totalAmount.toFloat()
                        donutProgressView.submitData(donutList)
                        homeAdapter = HomeAdapter(map, totalAmount, requireContext(), this)
                        personRecycler.adapter = homeAdapter
                        ("₹$todayTotal").also { todayAmount.text = it }
                        summaryAdapter = SummaryAdapter(requireContext())
                        latestItem.adapter = summaryAdapter
                        summaryAdapter.setDataTo(data)
                    } catch (e: java.lang.Exception) {
                        Log.e("ERROR", e.message + "")
                    }
                } else {
                    runTransition(line1, false)
                    runTransition(emptyLayout, true)
                    runTransition(kt, false)
                    k.visibility = View.GONE

                }
            }
    }

    private fun getColor(v: String): Int {
        val color = String.format(
            "#%X",
            "ABC DEF GHI JKL MNO PQR STU VWX YZ$v".hashCode()
        )
        return Color.parseColor(randColors.random())
    }

    override fun goToHome(position: Int, uuid: String) {
//        Log.e("FROM_HOME",uuid)
        homeToMain?.goToMain(position, uuid)
    }

    private val randColors = arrayOf(
        "#FFB300",
        "#803E75",
        "#FF6800",
        "#A6BDD7",
        "#C10020",
        "#CEA262",
        "#817066",
        "#007D34",
        "#F6768E",
        "#00538A",
        "#FF7A5C",
        "#53377A",
        "#FF8E00",
        "#B32851",
        "#F4C800",
        "#7F180D",
        "#93AA00",
        "#593315",
        "#F13A13",
        "#232C16"
    )
}