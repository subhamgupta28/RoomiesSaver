package com.subhamgupta.roomiesapp.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.subhamgupta.roomiesapp.R
import com.subhamgupta.roomiesapp.activities.MainActivity
import com.subhamgupta.roomiesapp.data.repositories.MainRepository
import com.subhamgupta.roomiesapp.domain.model.RoomDetail
<<<<<<< HEAD
=======
import com.subhamgupta.roomiesapp.utils.FirebaseState
>>>>>>> bc028885d2fc69567c10e880a1180fc67f3a028b
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
<<<<<<< HEAD
import kotlin.random.Random

private const val CHANNEL_ID = "roomies.app.notification1"
@HiltWorker
class Worker @AssistedInject constructor(
    private val repository: MainRepository,
=======
import javax.inject.Inject
import kotlin.random.Random

private const val CHANNEL_ID = "roomies.app.notification"
@HiltWorker
class Worker @AssistedInject constructor(
    private val repository: FireBaseRepository,
>>>>>>> bc028885d2fc69567c10e880a1180fc67f3a028b
    @Assisted private val context: Context,
    @Assisted private val params: WorkerParameters
) : CoroutineWorker(context, params) {
    private val _userData = MutableStateFlow<MutableMap<String, Any>>(mutableMapOf())
    private val _roomDetails = MutableStateFlow<FirebaseState<RoomDetail>>(FirebaseState.loading())
<<<<<<< HEAD
    private val _roomDataLoading = MutableStateFlow(true)

    override suspend fun doWork(): Result {
        withContext(Dispatchers.IO) {
//            showNotification()
            repository.fetchUserRoomData(_userData, _roomDetails, _roomDataLoading)
=======

    override suspend fun doWork(): Result {
        withContext(Dispatchers.IO) {
            showNotification()
            repository.fetchUserRoomData(_userData, _roomDetails)
>>>>>>> bc028885d2fc69567c10e880a1180fc67f3a028b
            Log.e("Background task", "done..")

        }
        return Result.success()
    }

    private fun showNotification() {
        val notifyIntent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val notificationID = Random.nextInt()
        notifyIntent.putExtra("NOTIFICATION_EXTRA", true)
        notifyIntent.putExtra("NOTIFICATION_ID", notificationID)
        val notifyPendingIntent = PendingIntent.getActivity(
<<<<<<< HEAD
            applicationContext, 0, notifyIntent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
=======
            applicationContext, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
>>>>>>> bc028885d2fc69567c10e880a1180fc67f3a028b
        )

        val builder = NotificationCompat
            .Builder(context, CHANNEL_ID)
            .setContentTitle("Updating")
            .setContentText("Update")
<<<<<<< HEAD
            .setSmallIcon(R.drawable.roomies)
=======
>>>>>>> bc028885d2fc69567c10e880a1180fc67f3a028b
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(notifyPendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(notificationID, builder.build())
        }

    }

}