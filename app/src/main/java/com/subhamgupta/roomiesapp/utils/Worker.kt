package com.subhamgupta.roomiesapp.utils

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.subhamgupta.roomiesapp.R
import com.subhamgupta.roomiesapp.activities.MainActivity
import com.subhamgupta.roomiesapp.data.repositories.MainRepository
import com.subhamgupta.roomiesapp.domain.model.RoomDetail
import com.subhamgupta.roomiesapp.domain.use_case.GetUserUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.random.Random

@HiltWorker
class Worker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val params: WorkerParameters
) : CoroutineWorker(context, params) {

    private lateinit var notificationManagerCompat: NotificationManagerCompat
    private val notificationID = 1
    override suspend fun doWork(): Result {
//        withContext(Dispatchers.IO) {
//            showNotification()
//            dismissNotification()
//        }
        return Result.success()
    }

    private suspend fun showNotification() {
        val notifyIntent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }


        notifyIntent.putExtra("NOTIFICATION_EXTRA", true)
        notifyIntent.putExtra("NOTIFICATION_ID", notificationID)
        val notifyPendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            notifyIntent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat
            .Builder(context, Constant.UPDATE_CHANNEL)
            .setContentTitle("Migration")
            .setContentText("In progress")
            .setSmallIcon(R.drawable.roomies)
//            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(notifyPendingIntent)
            .setColorized(true)
            .setAutoCancel(true)
//        setForeground(
//            ForegroundInfo(
//                Random.nextInt(),
//                builder.build()
//            )
//        )


//        with(NotificationManagerCompat.from(context)) {
//            Log.e("Permission", "granted")
//            notify(notificationID, builder.build())
//            notificationManagerCompat = this
//        }


    }


    private fun dismissNotification() {
        notificationManagerCompat.cancel(notificationID)
    }

}