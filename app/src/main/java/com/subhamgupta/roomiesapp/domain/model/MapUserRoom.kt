package com.subhamgupta.roomiesapp.domain.model

import java.io.Serializable

data class MapUserRoom(
    val ROOM_ID: String = "",
    val ROOM_NAME: String = "",
    val ROOM_KEY: String = "",
    val UUID: String = "",
    val USER_NAME: String = ""
): Serializable