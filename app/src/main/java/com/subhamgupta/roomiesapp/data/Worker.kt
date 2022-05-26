package com.subhamgupta.roomiesapp.data

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.subhamgupta.roomiesapp.data.repositories.FireBaseRepository
import com.subhamgupta.roomiesapp.models.RoomDetail
import com.subhamgupta.roomiesapp.utils.FirebaseState
import kotlinx.coroutines.flow.MutableStateFlow

class Worker(
    private val context: Context,
    private val params: WorkerParameters
) : CoroutineWorker(context, params) {
    private val _userData = MutableStateFlow<MutableMap<String, Any>>(mutableMapOf())
    private val _roomDetails = MutableStateFlow<FirebaseState<RoomDetail>>(FirebaseState.loading())

    private val repository = FireBaseRepository
    override suspend fun doWork(): Result {
        repository.fetchUserRoomData(_userData, _roomDetails)
        Log.e("Background task", "done..")
        return Result.success()
    }
}