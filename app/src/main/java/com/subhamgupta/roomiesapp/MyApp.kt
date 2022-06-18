package com.subhamgupta.roomiesapp

import android.app.Application
import android.util.Log
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.subhamgupta.roomiesapp.data.Worker
import java.util.concurrent.TimeUnit


class MyApp : Application() {
    lateinit var databaseReference: DatabaseReference
    lateinit var db: FirebaseFirestore
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var storage: FirebaseStorage
    lateinit var workManager: WorkManager

    companion object {
        lateinit var instance: MyApp

    }


    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()
        initializeFirebase()
        initializeWorker()
    }


    private fun initializeFirebase() {
        try {
//            FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        } catch (e: Exception) {
            Log.e("ERROR", e.message!!)
        }

        db = Firebase.firestore
//        db.useEmulator("10.0.2.2", 2828)
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings
        firebaseAuth = FirebaseAuth.getInstance()
//        firebaseAuth.useEmulator("10.0.2.2", 9099)
        val database = Firebase.database
//        database.useEmulator("10.0.2.2", 9000)
        databaseReference = database.getReference("ROOMIES")
        storage = FirebaseStorage.getInstance()
//        storage.useEmulator("10.0.2.2", 9199)
    }

    private fun initializeWorker() {
        val constraint = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = PeriodicWorkRequest.Builder(Worker::class.java, 30, TimeUnit.MINUTES)
            .setConstraints(constraint)
            .build()
        workManager = WorkManager.getInstance(this)
        workManager.enqueue(workRequest)
    }
}