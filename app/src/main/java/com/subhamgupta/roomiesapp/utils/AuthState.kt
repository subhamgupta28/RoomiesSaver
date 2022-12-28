package com.subhamgupta.roomiesapp.utils

sealed class AuthState<T> {
    data class LoggedIn<T>(val user: T) : AuthState<T>()
    data class NoUserOrError<T>(val message: String?): AuthState<T>()
    class Loading<T> : AuthState<T>()

    companion object{
        fun <T> loading() = Loading<T>()
        fun <T> loggedIn(user: T) = LoggedIn(user)
        fun <T> noUserOrError(message: String?) = NoUserOrError<T>(message)
    }
}