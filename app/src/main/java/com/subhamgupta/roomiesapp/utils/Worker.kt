package com.subhamgupta.roomiesapp.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import kotlin.random.Random

private const val CHANNEL_ID = "roomies.app.notification1"
@HiltWorker
class Worker @AssistedInject constructor(
    private val repository: MainRepository,
    @Assisted private val context: Context,
    @Assisted private val params: WorkerParameters
) : CoroutineWorker(context, params) {
    private val _userData = MutableStateFlow<MutableMap<String, Any>>(mutableMapOf())
    private val _roomDetails = MutableStateFlow<FirebaseState<RoomDetail>>(FirebaseState.loading())
    private val _roomDataLoading = MutableStateFlow(true)

    override suspend fun doWork(): Result {
        withContext(Dispatchers.IO) {
//            showNotification()
            repository.fetchUserRoomData(_userData, _roomDetails, _roomDataLoading)
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
            applicationContext, 0, notifyIntent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat
            .Builder(context, CHANNEL_ID)
            .setContentTitle("Updating")
            .setContentText("Update")
            .setSmallIcon(R.drawable.roomies)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(notifyPendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(notificationID, builder.build())
        }

    }

}