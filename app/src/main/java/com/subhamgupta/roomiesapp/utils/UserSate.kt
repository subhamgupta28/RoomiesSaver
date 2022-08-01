package com.subhamgupta.roomiesapp.utils

sealed class UserSate<T>(val data:T?=null, val message:String?=null) {
    class Error<T>(message: String?, data: T?=null):UserSate<T>(data, message)
    class IsRoomJoined<T>(data: T?):UserSate<T>(data)
}