package com.subhamgupta.roomiesapp.di

import android.app.Application
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
<<<<<<< HEAD
import com.google.firebase.database.FirebaseDatabase
=======
>>>>>>> bc028885d2fc69567c10e880a1180fc67f3a028b
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.subhamgupta.roomiesapp.data.repositories.AuthRepository
<<<<<<< HEAD
import com.subhamgupta.roomiesapp.data.repositories.MainRepository
=======
import com.subhamgupta.roomiesapp.data.repositories.FireBaseRepository
>>>>>>> bc028885d2fc69567c10e880a1180fc67f3a028b
import com.subhamgupta.roomiesapp.data.repositories.RoomRepository
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

<<<<<<< HEAD

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
=======
//    init {
//        firebaseStorage.useEmulator("10.0.2.2", 2828)
//        firebaseAuth.useEmulator("10.0.2.2", 9099)
//        database.useEmulator("10.0.2.2", 9000)
//        firebaseStorage.useEmulator("10.0.2.2", 9199)
//    }

    @Singleton
    @Provides
    fun provideFirestore(): FirebaseFirestore = firebaseFirestore

    @Singleton
    @Provides
    fun provideDatabaseReference(): DatabaseReference = databaseReference

    @Singleton
    @Provides
    fun provideAuth(): FirebaseAuth = firebaseAuth

    @Singleton
    @Provides
    fun provideStorage(): FirebaseStorage = firebaseStorage
>>>>>>> bc028885d2fc69567c10e880a1180fc67f3a028b

    @Singleton
    @Provides
    fun provideFirebaseRepository(
        storage: FirebaseStorage,
        databaseReference: DatabaseReference,
        db: FirebaseFirestore,
        auth: FirebaseAuth,
        settingDataStore: SettingDataStore,
<<<<<<< HEAD
        application: Application
    ): MainRepository = MainRepository(
=======
        application : Application
    ): FireBaseRepository = FireBaseRepository(
>>>>>>> bc028885d2fc69567c10e880a1180fc67f3a028b
        storage, databaseReference, db, auth, settingDataStore, application
    )

    @Singleton
    @Provides
    fun provideAuthRepository(
<<<<<<< HEAD
        databaseReference: DatabaseReference,
        auth: FirebaseAuth,
        settingDataStore: SettingDataStore
    ): AuthRepository = AuthRepository(
=======
         databaseReference:DatabaseReference,
         auth: FirebaseAuth,
         settingDataStore: SettingDataStore
    ):AuthRepository = AuthRepository(
>>>>>>> bc028885d2fc69567c10e880a1180fc67f3a028b
        databaseReference, auth, settingDataStore
    )

    @Singleton
    @Provides
    fun provideRoomRepository(
<<<<<<< HEAD
        databaseReference: DatabaseReference,
        auth: FirebaseAuth,
        settingDataStore: SettingDataStore,
        application: Application
    ): RoomRepository = RoomRepository(
=======
        databaseReference:DatabaseReference,
        auth: FirebaseAuth,
        settingDataStore: SettingDataStore,
        application: Application
    ):RoomRepository = RoomRepository(
>>>>>>> bc028885d2fc69567c10e880a1180fc67f3a028b
        databaseReference, auth, settingDataStore, application
    )

    @Singleton
    @Provides
    fun provideSettingStorage(
        application: Application
<<<<<<< HEAD
    ): SettingDataStore = SettingDataStore(application)
=======
    ):SettingDataStore = SettingDataStore(application)

>>>>>>> bc028885d2fc69567c10e880a1180fc67f3a028b


}