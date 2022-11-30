package com.subhamgupta.roomiesapp.utils

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_ONE_SHOT
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.subhamgupta.roomiesapp.R
import com.subhamgupta.roomiesapp.activities.SettingActivity
import kotlin.random.Random


class FirebaseService : FirebaseMessagingService() {

    companion object {
        var token: String? = null
        var uid: String? = null
    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
    }


    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.e("RECEIVED_UUID", message.data["uid"].toString())
        Log.e("MY_UUID", uid.toString())
        if (!message.data["uid"].equals(uid)) {

            Log.e("SERVICE_UID", message.data["uid"].toString())
            val keys = message.data.keys
            Log.e("TOPICS_SUB", "$keys")
            Log.e("MESSAGE_RECEIVED_FROM", message.from.toString())
            val intent = Intent(this, SettingActivity::class.java)
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notificationID = Random.nextInt()

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
            val pendingIntent = PendingIntent.getActivity(this, 0, intent, FLAG_ONE_SHOT or FLAG_IMMUTABLE)

//            val mgr = getSystemService(Context.ALARM_SERVICE) as AlarmManager
//            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pendingIntent)

            val notification = NotificationCompat.Builder(this, Constant.ITEM_BOUGHT_CHANNEL)
                .setContentTitle(message.data["title"])
                .setContentText(message.data["message"])
                .setSmallIcon(R.drawable.ic_outline_home)
                .setColor(Color.parseColor("#006684"))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setColorized(true)
                .setPriority(NotificationManager.IMPORTANCE_MAX)
                .build()


            notificationManager.notify(notificationID, notification)
        }

    }


}