package com.subhamgupta.roomiesapp.domain.repository

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.subhamgupta.roomiesapp.domain.model.Detail
import com.subhamgupta.roomiesapp.domain.model.HomeData
import com.subhamgupta.roomiesapp.domain.model.ROOMMATES
import com.subhamgupta.roomiesapp.domain.model.RoomDetail
import com.subhamgupta.roomiesapp.utils.FirebaseState
import kotlinx.coroutines.flow.MutableStateFlow

interface FirebaseRepository {
    suspend fun fetchSummary(
        isMonth: Boolean,
        category: String
    ): MutableStateFlow<FirebaseState<List<Detail?>>>

    suspend fun uploadPic(
        uri: Uri,
        userName: String
    ): MutableStateFlow<FirebaseState<Unit>>

    suspend fun generateExcel(): MutableStateFlow<FirebaseState<Unit>>

    suspend fun fetchDiffData(): ArrayList<ROOMMATES>

    suspend fun fetchHomeData(
        loadingHome: MutableStateFlow<Boolean>,
    ): MutableStateFlow<FirebaseState<HomeData>>

    suspend fun addItem(
        item: String?,
        amount: String,
        note: String,
        tags: List<String>,
        category: String
    ): MutableLiveData<FirebaseState<Unit>>

    suspend fun updateItem(
        item: String?,
        amount: String,
        timeStamp: String,
        note: String,
        tags: List<String>,
        category: String
    ): MutableLiveData<FirebaseState<Unit>>

    suspend fun getRoomFromRemote(): MutableStateFlow<FirebaseState<RoomDetail>>

    suspend fun getUserFromRemote(): MutableStateFlow<MutableMap<String, Any>>


}