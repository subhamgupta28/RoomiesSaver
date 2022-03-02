package com.subhamgupta.roomiesapp.utility

import android.content.Context
import android.text.format.DateUtils
import android.util.Log
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.subhamgupta.roomiesapp.Contenst
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneOffset
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class FirebaseDataUtil(
    var context: Context
) {
    private var db: FirebaseFirestore
    private lateinit var todayData: MutableList<Map<String, Any?>?>
    private lateinit var currentMonthData: MutableList<Map<String, Any?>?>
    private lateinit var allTimeData: MutableList<Map<String, Any?>?>
    private var dbref: DatabaseReference
    private var settingsStorage: SettingsStorage
    private var totalAmount: Long = 0
    private var todayTotal: Long = 0
    private var monthTotal: Long = 0
    private var key = ""

    init {

        db = FirebaseFirestore.getInstance()
        settingsStorage = SettingsStorage(context)
        key = settingsStorage.room_id.toString()
        dbref = FirebaseDatabase.getInstance().reference.child("ROOMIES")

        getData()
    }

    private fun getData() {
        todayData = ArrayList()
        currentMonthData = ArrayList()
        val query: Query = db.collection(key)
        query.orderBy("DATE", Query.Direction.DESCENDING)
            .addSnapshotListener { value: QuerySnapshot?, _: FirebaseFirestoreException? ->
                todayData.clear()
                currentMonthData.clear()
                todayTotal = 0
                monthTotal = 0
                totalAmount = 0
                if (value != null) {
                    try {
                        for (qds in value) {
                            val timestamp =
                                Timestamp(
                                    SimpleDateFormat(Contenst.DATE_STRING, Locale.getDefault()).parse(qds["DATE"].toString()).time
                                )
                            val k: MutableMap<String, Any?> = HashMap()
//                            Log.e("CURRENT MONTH","${isCurrentMonth(qds["DATE"].toString())}")

                            k["ITEM_BOUGHT"] = qds["ITEM_BOUGHT"]
                            k["DATE"] = qds["DATE"]
                            val mon = qds["AMOUNT_PAID"].toString()
                            if (mon.isEmpty())
                                todayTotal = todayTotal.plus(0)
                            else
                                todayTotal = todayTotal.plus(mon.toInt())
                            k["TIME"] = qds["TIME"]
                            k["AMOUNT_PAID"] = qds["AMOUNT_PAID"]
                            k["BOUGHT_BY"] = qds["BOUGHT_BY"]
                            val sum = qds["AMOUNT_PAID"].toString().toInt()
                            if (DateUtils.isToday(timestamp.time)) {
                                todayTotal= todayTotal.plus(sum)
                                todayData.add(k)
                            }
                            if (isCurrentMonth( qds["DATE"].toString())){
                                monthTotal = monthTotal.plus(sum)
                                currentMonthData.add(k)
                            }
                            totalAmount = totalAmount.plus(sum)

                        }


                        val g = todayData.sortedWith(compareBy {
                            it?.get("DATE").toString()
                        }) as MutableList
                        g.reverse()
                        //Log.e("TODAY_LIST","$g")


                    } catch (e: java.lang.Exception) {
                        Log.e("ERROR", e.message + "")
                    }
                }
            }
    }
    @JvmName("getTodayData1")
    public fun getTodayData(): MutableList<Map<String, Any?>?> {
        return todayData
    }
    public fun getMonthData(): MutableList<Map<String, Any?>?> {
        return currentMonthData
    }
    public fun getTodayAmount():Long{
        return todayTotal
    }
    public fun getMonthAmount():Long{
        return monthTotal
    }
    @JvmName("getTotalAmount1")
    public fun getTotalAmount():Long{
        return totalAmount
    }

    private fun isCurrentMonth(date: String):Boolean{

        val gDate = SimpleDateFormat(Contenst.DATE_STRING, Locale.getDefault()).parse(date)
        val timeZone = ZoneOffset.UTC
        val localDate = LocalDateTime.ofInstant(gDate.toInstant(), timeZone)
        val currentMonth = YearMonth.now(timeZone)
        return currentMonth.equals(YearMonth.from(localDate))
    }

}