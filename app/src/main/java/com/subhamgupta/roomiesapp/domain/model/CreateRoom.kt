package com.subhamgupta.roomiesapp.domain.model

import java.io.Serializable

data class CreateRoom(
    var error :String = "",
    var isCreated: Boolean = false,
    var isRoomJoined: Boolean = false
):Serializable
