package com.subhamgupta.roomiesapp.domain.model

//import app.futured.donut.DonutSection
import java.io.Serializable

data class HomeData(
    var todayTotal: Int = 0,
    var allTotal: Int = 0,
    var startDate: String? = null,
    var updatedOn: String? = null,
    var chartData: ArrayList<Int>? = null,
    var isEmpty: Boolean = false,
    var count: Int = 0,
    var roomSize: Int = 0,
    var eachPersonAmount: String = ""
) : Serializable
