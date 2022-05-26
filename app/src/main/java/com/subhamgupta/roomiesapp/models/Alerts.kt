package com.subhamgupta.roomiesapp.models

import java.io.Serializable

data class Alerts(
    val BY:String?=null,
    val UUID:String?=null,
    val ALERT:String?=null,
    val DATE:String?=null,
    val TIME:String?=null,
    val TIME_STAMP:Long?=null,
    val IS_COMPLETED:Boolean?=null
):Serializable
