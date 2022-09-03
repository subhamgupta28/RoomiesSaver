package com.subhamgupta.roomiesapp.di

import android.app.Application
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.subhamgupta.roomiesapp.MyApp
import com.subhamgupta.roomiesapp.data.repositories.AuthRepository
import com.subhamgupta.roomiesapp.data.repositories.MainRepository

import com.subhamgupta.roomiesapp.data.repositories.RoomRepository
import com.subhamgupta.roomiesapp.utils.NetworkObserver
import com.subhamgupta.roomiesapp.utils.SettingDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private val firebaseStorage = Firebase.storage
    private val firebaseAuth = Firebase.auth
    private val firebaseFirestore = Firebase.firestore
    private val database = Firebase.database
    private val databaseReference = database.getReference("ROOMIES")



    @Singleton
    @Provides
    fun provideFirestore(): FirebaseFirestore {
        return firebaseFirestore
    }

    @Singleton
    @Provides
    fun provideDatabase(): FirebaseDatabase {
        return database
    }

    @Singleton
    @Provides
    fun provideDatabaseReference(): DatabaseReference {
        return databaseReference
    }

    @Singleton
    @Provides
    fun provideAuth(): FirebaseAuth {
        return firebaseAuth
    }


    @Singleton
    @Provides
    fun provideStorage(): FirebaseStorage {
        return firebaseStorage
    }


    @Singleton
    @Provides
    fun provideFirebaseRepository(
        storage: FirebaseStorage,
        databaseReference: DatabaseReference,
        db: FirebaseFirestore,
        auth: FirebaseAuth,
        settingDataStore: SettingDataStore,
        application: Application
    ): MainRepository = MainRepository(

        storage, databaseReference, db, auth, settingDataStore, application
    )

    @Singleton
    @Provides
    fun provideAuthRepository(
        databaseReference: DatabaseReference,
        auth: FirebaseAuth,
        settingDataStore: SettingDataStore,
        application: Application,
        storage: FirebaseStorage
    ): AuthRepository = AuthRepository(
        databaseReference, auth, settingDataStore, application, storage
    )

    @Singleton
    @Provides
    fun provideRoomRepository(
        databaseReference: DatabaseReference,
        auth: FirebaseAuth,
        settingDataStore: SettingDataStore,
        application: Application
    ): RoomRepository = RoomRepository(
        databaseReference, auth, settingDataStore, application
    )

    @Singleton
    @Provides
    fun provideSettingStorage(
        application: Application
    ): SettingDataStore = SettingDataStore(application)

    @Singleton
    @Provides
    fun provideNetworkObserver(
        application: Application
    ): NetworkObserver = NetworkObserver(application)



}