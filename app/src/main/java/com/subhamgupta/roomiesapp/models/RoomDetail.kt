package com.subhamgupta.roomiesapp.models

import java.io.Serializable

data class RoomDetail(
    var CREATED_BY: String? = null,
    var CREATED_ON: String? = null,
    var JOINED_PERSON: Int? = null,
    var LAST_UPDATED: String? = null,
    var LIMIT: Int? = null,
    var ROOM_ID: String? = null,
    var ROOM_MATES: ArrayList<ROOMMATES> = arrayListOf(),
    var ROOM_NAME: String? = null,
    var START_DATE_MONTH: Long? = null
):Serializable

data class ROOMMATES(
    var KEY: String? = null,
    var MONEY_PAID: Int? = null,
    var USER_NAME: String? = null,
    var UUID: String? = null
):Serializable