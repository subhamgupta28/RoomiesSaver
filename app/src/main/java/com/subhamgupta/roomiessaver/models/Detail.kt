package com.subhamgupta.roomiessaver.models

import java.io.Serializable

data class Detail(var UUID: String? = null,
                  var ITEM_BOUGHT: String? = null,
                  var AMOUNT_PAID: Long = 0,
                  var DATE: String? = null,
                  var TIME_STAMP: Long? = null,
                  var BOUGHT_BY: String? = null,
                  var TIME: String? = null) : Serializable