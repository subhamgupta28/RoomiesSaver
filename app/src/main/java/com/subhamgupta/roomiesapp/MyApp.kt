package com.subhamgupta.roomiesapp

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MyApp : Application(), Configuration.Provider {
<<<<<<< HEAD
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
=======

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


>>>>>>> bc028885d2fc69567c10e880a1180fc67f3a028b
    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }
<<<<<<< HEAD
=======


>>>>>>> bc028885d2fc69567c10e880a1180fc67f3a028b
}