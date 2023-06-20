package com.subhamgupta.roomiesapp.domain.model


data class SplitData(
    var UUID: String = "",
    var BY_NAME: String = "",
    var TOTAL_AMOUNT: Double = 0.0,
    var ITEM: String = "",
    var NOTE: String = "",
    var FOR: MutableList<MutableMap<String, Any>> = mutableListOf(),
    var TIME_STAMP: Long = 0,
    var TIME: String = "",
    var DATE: String = "",
    var COMPLETED: Boolean = false,
    var DELETED:Boolean = false
)
