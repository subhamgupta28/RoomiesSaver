package com.subhamgupta.roomiesapp.data

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.subhamgupta.roomiesapp.R
import com.subhamgupta.roomiesapp.activities.MainActivity
import com.subhamgupta.roomiesapp.data.repositories.FireBaseRepository
import com.subhamgupta.roomiesapp.domain.model.RoomDetail
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
//        showNotification()
        return Result.success()
    }

    private fun showNotification(){
        val intent = Intent(applicationContext,
            MainActivity::class.java
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent =PendingIntent.getActivity(
            applicationContext, 0,intent, PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(
            applicationContext,
            applicationContext.packageName
        ).setContentTitle("Updating")
            .setContentText("Roomies app data update")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setSmallIcon(R.drawable.roomies)
            .setContentIntent(pendingIntent)

        with(NotificationManagerCompat.from(applicationContext)){
            notify(1, builder.build())
        }
    }
}