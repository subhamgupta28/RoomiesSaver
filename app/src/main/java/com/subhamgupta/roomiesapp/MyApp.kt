package com.subhamgupta.roomiesapp

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MyApp : Application(), Configuration.Provider {

    private lateinit var instance: MyApp

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    fun getInstance(): MyApp {
        return instance
    }


    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }


}