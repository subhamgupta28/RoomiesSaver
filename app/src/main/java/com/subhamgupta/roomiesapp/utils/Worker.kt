package com.subhamgupta.roomiesapp.data

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
import com.subhamgupta.roomiesapp.data.repositories.FireBaseRepository
import com.subhamgupta.roomiesapp.domain.model.RoomDetail
import com.subhamgupta.roomiesapp.utils.FirebaseState
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.random.Random

private const val CHANNEL_ID = "roomies.app.notification"
@HiltWorker
class Worker @AssistedInject constructor(
    private val repository: FireBaseRepository,
    @Assisted private val context: Context,
    @Assisted private val params: WorkerParameters
) : CoroutineWorker(context, params) {
    private val _userData = MutableStateFlow<MutableMap<String, Any>>(mutableMapOf())
    private val _roomDetails = MutableStateFlow<FirebaseState<RoomDetail>>(FirebaseState.loading())

    override suspend fun doWork(): Result {
        withContext(Dispatchers.IO) {
            showNotification()
            repository.fetchUserRoomData(_userData, _roomDetails)
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
            applicationContext, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat
            .Builder(context, CHANNEL_ID)
            .setContentTitle("Updating")
            .setContentText("Update")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(notifyPendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(notificationID, builder.build())
        }

    }

}