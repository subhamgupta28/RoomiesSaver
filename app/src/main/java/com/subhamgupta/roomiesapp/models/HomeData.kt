package com.subhamgupta.roomiesapp.models

import app.futured.donut.DonutSection
import java.io.Serializable

data class HomeData(
    var todayTotal: Int = 0,
    var allTotal: Int= 0,
    var eachPersonAmount:String?=null,
    var todayData: MutableList<Detail>?=null,
    var donutList: List<DonutSection>?=null,
    var userMap: ArrayList<MutableMap<String, String>>?=null,
    var startDate: String?=null,
    var updatedOn:String?=null,
    var chartData: ArrayList<Int>?=null
):Serializable
