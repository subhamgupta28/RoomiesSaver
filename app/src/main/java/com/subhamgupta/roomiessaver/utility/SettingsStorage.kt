package com.subhamgupta.roomiessaver.utility

import android.content.Context
import android.content.SharedPreferences

class SettingsStorage(private val context: Context) {
    private val sharedPreferences: SharedPreferences
    private val editor: SharedPreferences.Editor
    fun clear() {
        val preferences: SharedPreferences =
            context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.clear()
        editor.apply()
    }

    var username: String? = null
        get() {
            field = sharedPreferences.getString("username", "null")
            return field
        }
        set(name) {
            editor.putString("username", name)
            editor.commit()
            field = name
        }
    var uuid: String? = null
        get() {
            field = sharedPreferences.getString("uuid", "null")
            return field
        }
        set(uuid) {
            editor.putString("uuid", uuid)
            editor.commit()
            field = uuid
        }
    var room_name: String? = null
        get() {
            field = sharedPreferences.getString("room_name", "null")
            return field
        }
        set(room_name) {
            editor.putString("room_name", room_name)
            editor.commit()
            field = room_name
        }
    var email: String? = null
        get() {
            field = sharedPreferences.getString("email", "null")
            return field
        }
        set(email) {
            editor.putString("email", email)
            editor.commit()
            field = email
        }
    var isMonth: Boolean? = null
        get() {
            field = sharedPreferences.getBoolean("isMonth", true)
            return field
        }
        set(isMonth) {
            if (isMonth != null) {
                editor.putBoolean("isMonth", isMonth)
            }
            editor.commit()
            field = isMonth
        }
    var darkMode: Boolean? = null
        get() {
            field = sharedPreferences.getBoolean("darkMode", false)
            return field
        }
        set(darkMode) {
            if (darkMode != null) {
                editor.putBoolean("darkMode", darkMode)
            }
            editor.commit()
            field = darkMode
        }
    var json: String? = null
        get() {
            field = sharedPreferences.getString("json", "")
            return field
        }
        set(json) {
            editor.putString("json", json)
            editor.commit()
            field = json
        }
    var room_id: String? = null
        get() {
            field = sharedPreferences.getString("room_id", "null")
            return field
        }
        set(room_id) {
            editor.putString("room_id", room_id)
            editor.commit()
            field = room_id
        }
    var isRoom_joined = false
        get() {
            field = sharedPreferences.getBoolean("room_joined", false)
            return field
        }
        set(room_joined) {
            editor.putBoolean("room_joined", room_joined)
            editor.commit()
            field = room_joined
        }
    var isLoggedIn = false
        get() {
            field = sharedPreferences.getBoolean("logged_in", false)
            return field
        }
        set(logged_in) {
            editor.putBoolean("logged_in", logged_in)
            editor.commit()
            field = logged_in
        }

    init {
        sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()
    }
}