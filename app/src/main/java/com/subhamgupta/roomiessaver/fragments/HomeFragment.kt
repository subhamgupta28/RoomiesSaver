package com.subhamgupta.roomiessaver.fragments

import android.graphics.Color
import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
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
import com.hadiidbouk.charts.BarData
import com.hadiidbouk.charts.ChartProgressBar
import com.majorik.sparklinelibrary.SparkLineLayout
import com.subhamgupta.roomiessaver.Contenst.Companion.DATE_STRING
import com.subhamgupta.roomiessaver.adapters.HomeAdapter
import com.subhamgupta.roomiessaver.adapters.SummaryAdapter
import com.subhamgupta.roomiessaver.utility.SettingsStorage
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.*


class HomeFragment : DialogFragment() {
    lateinit var personRecycler: RecyclerView
    lateinit var totalSpending: TextView
    lateinit var sparkLineLayout: SparkLineLayout
    lateinit var updatedOn: TextView
    lateinit var noText: TextView
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
    lateinit var mChart: ChartProgressBar
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
        sparkLineLayout = view.findViewById(R.id.spark)
        noText = view.findViewById(R.id.no_text)
        kt = view.findViewById(R.id.kt)
        k = view.findViewById(R.id.k)
        line1 = view.findViewById(R.id.line1)
        mChart = view.findViewById(R.id.ChartProgressBar)
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
        ref.collection(key)
        val dataList: ArrayList<BarData> = ArrayList()

        var data = BarData("Sep", 3.4f, "3.4€")
        dataList.add(data)

        data = BarData("Oct", 8f, "8€")
        dataList.add(data)

        data = BarData("Nov", 1.8f, "1.8€")
        dataList.add(data)

        data = BarData("Dec", 7.3f, "7.3€")
        dataList.add(data)

        data = BarData("Jan", 6.2f, "6.2€")
        dataList.add(data)

        data = BarData("Feb", 3.3f, "3.3€")
        dataList.add(data)


        mChart.setDataList(dataList)
        mChart.build()
        setData()
        setRecentPurchase()

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
                    Log.e("ERROR", "${e.localizedMessage}")
                }

            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
            }
        })

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
        sparkLineLayout.setData(mutableListOf(0, 0) as ArrayList<Int>)
        val sdom = LocalDate.now().withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault())
            .toInstant().epochSecond
//        Log.e("S_DOM","$sdom")
        val query: Query = db.collection(key)
        query.whereGreaterThanOrEqualTo("TIME_STAMP", sdom*1000)//1643673600
            .orderBy("TIME_STAMP", Query.Direction.DESCENDING)
            .addSnapshotListener { value: QuerySnapshot?, _: FirebaseFirestoreException? ->
                data.clear()
                map.clear()
                list.clear()
                todayTotal = 0
                totalAmount = 0
                if (value != null) {

                    try {
                        val donutList = ArrayList<DonutSection>()
                        val userMap = mutableMapOf<String, Int?>()
                        for (qds in value) {
                            val timestamp = Timestamp(qds["TIME_STAMP"].toString().toLong())
                            val k = qds.data as MutableMap<String, Any>
                            val user = qds["BOUGHT_BY"].toString()
                            val amount = qds["AMOUNT_PAID"].toString().toInt()
                            val mon = qds["AMOUNT_PAID"].toString()
//                            Log.e("E_DOM","$timestamp")
                            thisMonthAmount++
                            userMap[user] =
                                if (userMap.containsKey(user))
                                    userMap[user]?.plus(amount)
                                else amount
                            list.add(amount)
                            totalAmount = if (mon.isEmpty())
                                totalAmount.plus(0)
                            else
                                totalAmount.plus(mon.toInt())
                            if (DateUtils.isToday(timestamp.time)) {
                                todayTotal = if (mon.isEmpty())
                                    todayTotal.plus(0)
                                else
                                    todayTotal.plus(mon.toInt())
                                data.add(k)
                            }
                        }
                        sparkLineLayout.visibility = View.VISIBLE
                        sparkLineLayout.setData(list)
                        for (i in userMap) {
                            val mp = mutableMapOf(
                                "USER_NAME" to i.key,
                                "AMOUNT" to i.value.toString()
                            )
                            val section1 = DonutSection(
                                name = i.key,
                                color = getColor(i.key),
                                amount = i.value.toString().toFloat()
                            )
                            donutList.add(section1)
                            map.add(mp)
                        }
                        if (thisMonthAmount == 0) {
                            noText.visibility = View.VISIBLE
                            k.visibility = View.GONE
                            kt.visibility = View.GONE
                            line1.visibility = View.GONE
                        } else
                            noText.visibility = View.INVISIBLE
                        "₹$totalAmount".also { totalSpending.text = it }
                        donutProgressView.cap = totalAmount.toFloat()
                        donutProgressView.submitData(donutList)
                        homeAdapter = HomeAdapter(map, totalAmount, requireContext())
                        personRecycler.adapter = homeAdapter
                        ("₹$todayTotal").also { todayAmount.text = it }
                        summaryAdapter = SummaryAdapter(requireContext())
                        latestItem.adapter = summaryAdapter
                        summaryAdapter.setDataTo(data)
                    } catch (e: java.lang.Exception) {
                        Log.e("ERROR", e.message + "")
                    }
                } else {
                    noText.visibility = View.VISIBLE

                }
            }
    }

    private fun getColor(v: String): Int {
        val color = String.format(
            "#%X",
            "ABC DEF GHI JKL MNO PQR STU VWX YZ$v".hashCode()
        )
        return Color.parseColor(color)
    }


}