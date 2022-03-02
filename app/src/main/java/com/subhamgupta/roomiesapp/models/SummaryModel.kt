package com.subhamgupta.roomiesapp.models

import java.io.Serializable

data class SummaryModel(   var ITEM_BOUGHT: String? = null,
                      var AMOUNT_PAID: Long = 0,
                      var UUID: String? = null,
                      var BOUGHT_BY: String? = null,
                      var DATE: String? = null) : Serializable