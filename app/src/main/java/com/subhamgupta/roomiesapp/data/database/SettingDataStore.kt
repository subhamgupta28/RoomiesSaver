package com.subhamgupta.roomiesapp.data.database

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.subhamgupta.roomiesapp.MyApp
import kotlinx.coroutines.flow.first


object SettingDataStore {
    private val context = MyApp.instance
    //fields present
    /*
    roomCount
    uuid
    isMonth
    RoomJoined
    UserName
    StartDate
    RoomSize
    LoggedIn
    Update
    Email
    RoomKey
    RoomRef
    clear
    demo
    demo2
    */
    private val Context.dataStore by preferencesDataStore(name = "settings")
    private suspend fun save(key: String, value: String) {
        val dataStoreKey = stringPreferencesKey(key)
        context.dataStore.edit {
            it[dataStoreKey] = value
        }
    }
    suspend fun isRoomJoined(): Boolean {
        val res = read("isRoomJoined")?:"false"
        return res.toBoolean()
    }

    suspend fun setRoomJoined(str: Boolean) {
        save("isRoomJoined", str.toString())
    }

    suspend fun getDarkMode(): Boolean {
        val res = read("getDarkMode")?:"false"
        return res.toBoolean()
    }

    suspend fun setDarkMode(str: Boolean) {
        save("getDarkMode", str.toString())
    }
    suspend fun getDemo(): Boolean {
        val res = read("demo")?:"false"
        return res.toBoolean()
    }

    suspend fun setDemo(str: Boolean) {
        save("demo", str.toString())
    }
    suspend fun getDemo2(): Boolean {
        val res = read("demo")?:"false"
        return res.toBoolean()
    }

    suspend fun setDemo2(str: Boolean) {
        save("demo", str.toString())
    }

    suspend fun isMonth(): Boolean {
        val res = read("isMonth")?:"true"
        return res.toBoolean()
    }

    suspend fun setMonth(str: Boolean) {
        save("isMonth", str.toString())
    }

    suspend fun isLoggedIn(): Boolean {
        val res = read("isLoggedIn")?:"false"
        return res.toBoolean()
    }

    suspend fun setLoggedIn(str: Boolean) {
        save("isLoggedIn", str.toString())
    }
    suspend fun isUpdate(): Boolean {
        val res = read("isUpdate")?:"false"
        return res.toBoolean()
    }

    suspend fun setUpdate(str: Boolean) {
        save("isUpdate", str.toString())
    }

    suspend fun getEmail(): String {
        return read("email")?:""
    }

    suspend fun setEmail(str: String) {
        save("email", str)
    }

    suspend fun getRoomCount(): Int {
        val res = read("getRoomCount")?:"0"
        return res.toInt()
    }

    suspend fun setRoomCount(str: Int) {
        save("getRoomCount", str.toString())
    }

    suspend fun getUUID(): String {
        return read("uuid")?:""
    }

    suspend fun setUUID(str: String) {
        save("uuid", str)
    }
    suspend fun getUserName(): String {
        return read("userName")?:""
    }

    suspend fun setUserName(str: String) {
        save("userName", str)
    }


    suspend fun getRoomKey(): String {
        return read("roomKey")?:"0"
    }

    suspend fun setRoomKey(str: String) {
        save("roomKey", str)
    }

    suspend fun getStartDate(): String {
        return read("startDate")?:"0"
    }

    suspend fun setStartDate(str: String) {
        save("startDate", str)
    }

    suspend fun getRoomSize(): Int {
        val res = read("roomSize")?:"0"
        return res.toInt()
    }

    suspend fun setRoomSize(str: Int) {
        save("roomSize", str.toString())
    }

    suspend fun getRoomRef(): String {
        return read("roomRef") ?: "ROOM_ID1"
    }

    suspend fun setRoomRef(str: String) {
        save("roomRef", str)
    }

    suspend fun clear(){
        context.dataStore.edit {
            it.clear()
        }
    }

    private suspend fun read(key: String): String? {
        val dataStoreKey = stringPreferencesKey(key)
        val preferences = context.dataStore.data.first()
        return preferences[dataStoreKey]
    }
}