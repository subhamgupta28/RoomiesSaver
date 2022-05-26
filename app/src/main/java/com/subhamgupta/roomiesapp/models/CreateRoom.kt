package com.subhamgupta.roomiesapp.models

import java.io.Serializable

data class CreateRoom(
    var error :String = "",
    var isCreated: Boolean = false,
    var isRoomJoined: Boolean = false
):Serializable
