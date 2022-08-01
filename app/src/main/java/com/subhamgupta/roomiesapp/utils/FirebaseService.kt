package com.subhamgupta.roomiesapp.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_ONE_SHOT
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.subhamgupta.roomiesapp.R
import com.subhamgupta.roomiesapp.activities.MainActivity
import kotlin.random.Random

private const val CHANNEL_ID = "roomies.app.notification"

class FirebaseService : FirebaseMessagingService() {

    companion object {
        var sharedPref: SharedPreferences? = null

        var token: String?
            get() {
                return "/topics/${sharedPref?.getString("room_id", "")}"
            }
            set(value) {
                sharedPref?.edit()?.putString("room_id", value)?.apply()
            }
        var uid: String?
            get() {
                return sharedPref?.getString("uuid", "")
            }
            set(value) {
                sharedPref?.edit()?.putString("uuid", value)?.apply()
            }
    }


    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        if (!message.data["uid"].equals(uid)) {
            val intent = Intent(this, MainActivity::class.java)
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notificationID = Random.nextInt()

            createNotificationChannel(notificationManager)

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            val pendingIntent = PendingIntent.getActivity(this, 0, intent, FLAG_ONE_SHOT or FLAG_IMMUTABLE)
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(message.data["title"])
                .setContentText(message.data["message"])
                .setSmallIcon(R.drawable.roomies)
                .setColor(Color.parseColor("#006684"))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setColorized(true)
                .setPriority(NotificationManager.IMPORTANCE_MAX)
                .build()


            notificationManager.notify(notificationID, notification)
        }

    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channelName = "roomies.app.notification"
        val channel = NotificationChannel(CHANNEL_ID, channelName, IMPORTANCE_HIGH).apply {
            description = "Roomies App"
            enableLights(true)
            lightColor = Color.RED
        }
        notificationManager.createNotificationChannel(channel)
    }

}