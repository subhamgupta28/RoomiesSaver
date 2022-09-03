package com.subhamgupta.roomiesapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import com.google.android.material.color.DynamicColors
import com.subhamgupta.roomiesapp.utils.Constant
import com.subhamgupta.roomiesapp.utils.SettingDataStore
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltAndroidApp
class MyApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var settingDataStore: SettingDataStore


    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
//        GlobalScope.launch(Dispatchers.IO) {
//            val dm = settingDataStore.getDarkMode()
//            if (dm)
//                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
//            else
//                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
//        }
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