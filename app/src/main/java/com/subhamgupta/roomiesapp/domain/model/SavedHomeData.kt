package com.subhamgupta.roomiesapp.domain.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class SavedHomeData(
    @SerializedName("UUID")
    var UUID: String? = null,
    @SerializedName("home")
    var home: HomeData? = null,
//    @SerializedName("eachPersonAmount")
//    var eachPersonAmount: String? = null,
    @SerializedName("homeUserMap")
    var homeUserMap: HomeUserMap? =null,
    @SerializedName("donutList")
    var donutList: List<Any> = emptyList(),
    @SerializedName("detailList")
    var detailList: List<Detail> = emptyList(),
    @SerializedName("monthTotalAmount")
    var monthTotalAmount: Int? = null,
    @SerializedName("todayTotalAmount")
    var todayTotalAmount: Int? = null
) : Serializable