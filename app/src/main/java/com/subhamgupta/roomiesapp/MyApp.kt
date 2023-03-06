package com.subhamgupta.roomiesapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.provider.Settings
import android.util.Log
import android.widget.NumberPicker
import android.widget.NumberPicker.OnValueChangeListener
import androidx.appcompat.app.AppCompatDelegate
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import com.google.android.material.color.DynamicColors
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.subhamgupta.roomiesapp.domain.use_case.GetUserUseCase
import com.subhamgupta.roomiesapp.utils.Constant
import com.subhamgupta.roomiesapp.utils.SettingDataStore
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.system.measureTimeMillis

@HiltAndroidApp
class MyApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    companion object {
        lateinit var uuid: String
        lateinit var deviceId: String
    }


    override fun onCreate() {
        super.onCreate()
        Log.e("myappUser", "${System.currentTimeMillis()} ")
        DynamicColors.applyToActivitiesIfAvailable(this@MyApp)
        createChannel()
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }

    private fun createChannel() {
        val newBuyingChannel = NotificationChannel(
            Constant.ITEM_BOUGHT_CHANNEL,
            "New buying",
            NotificationManager.IMPORTANCE_HIGH
        )
        newBuyingChannel.description = "Shows new bought items"
        val updateChannel = NotificationChannel(
            Constant.UPDATE_CHANNEL,
            "Update",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        updateChannel.description = "Background tasks"
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(newBuyingChannel)
        notificationManager.createNotificationChannel(updateChannel)
    }

}