package com.subhamgupta.roomiesapp.models

import java.io.Serializable

data class RoomModel( var KEY: String? = null,
                 var MONEY_PAID: Long = 0,
                 var UUID: String? = null,
                 var USER_NAME: String? = null) : Serializable