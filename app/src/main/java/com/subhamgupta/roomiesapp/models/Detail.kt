package com.subhamgupta.roomiesapp.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class Detail(
    @SerializedName("UUID")
    var UUID: String? = null,
    @SerializedName("ITEM_BOUGHT")
    var ITEM_BOUGHT: String? = null,
    @SerializedName("AMOUNT_PAID")
    var AMOUNT_PAID: Long = 0,
    @SerializedName("DATE")
    var DATE: String? = null,
    @SerializedName("TIME_STAMP")
    var TIME_STAMP: Long? = null,
    @SerializedName("BOUGHT_BY")
    var BOUGHT_BY: String? = null,
    @SerializedName("TIME")
    var TIME: String? = null
):Serializable