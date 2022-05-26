package com.subhamgupta.roomiesapp.utils

sealed class FirebaseState<T> {
    class Loading<T> : FirebaseState<T>()
    data class Success<T>(val data: T) : FirebaseState<T>()
    data class Failed<T>(val message: String?) : FirebaseState<T>()
    class Empty<T> :FirebaseState<T>()

    companion object {
        fun <T> loading() = Loading<T>()
        fun <T> success(data: T) = Success(data)
        fun <T> failed(message: String?) = Failed<T>(message)
        fun <T> empty() = Empty<T>()
    }
}
