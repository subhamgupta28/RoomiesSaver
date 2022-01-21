package com.subhamgupta.roomiessaver.services

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
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.subhamgupta.roomiessaver.R
import com.subhamgupta.roomiessaver.activities.MainActivity
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
//        Log.e("SERVICE_UID", message.data["uid"].toString())
//        Log.e("SERVICE_UUID", uid.toString())
        if (!message.data["uid"].equals(uid)) {

//            Log.e("SERVICE_UID", message.data["uid"].toString())
//            Log.e("TOPICS_SUB", message.data.keys.toString())
//            Log.e("MESSAGE_RECEIVED_FROM", message.from.toString())
            val intent = Intent(this, MainActivity::class.java)
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notificationID = Random.nextInt()

            createNotificationChannel(notificationManager)

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            val pendingIntent = PendingIntent.getActivity(this, 0, intent, FLAG_IMMUTABLE)
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

    @RequiresApi(Build.VERSION_CODES.O)
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