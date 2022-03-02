package com.subhamgupta.roomiesapp.activities

import android.app.ActivityOptions
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.gms.tasks.Task
import com.google.android.material.color.DynamicColors
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.subhamgupta.roomiesapp.R
import com.subhamgupta.roomiesapp.utility.SettingsStorage

class SplashScreen : AppCompatActivity() {
    var user: FirebaseUser? = null
    lateinit var splash_img: View
    lateinit var bundle: Bundle
    lateinit var settingsStorage: SettingsStorage
    lateinit var ref: DatabaseReference

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        settingsStorage = SettingsStorage(this)
        if (settingsStorage.darkMode==true)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DynamicColors.applyToActivitiesIfAvailable(application)
        setContentView(R.layout.activity_splash_screen)
        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        settingsStorage = SettingsStorage(this)

        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        } catch (e: Exception) {
            Log.e("ERROR", e.message!!)
        }
        user = FirebaseAuth.getInstance().currentUser
        splash_img = findViewById(R.id.splash_img)
        ref = FirebaseDatabase.getInstance().reference.child("ROOMIES")
        bundle = ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
        settingsStorage.email?.let { Log.e("EMAIL", it) }
        val animZoomIn = AnimationUtils.loadAnimation(
            this,
            R.anim.zoom_in
        )
        splash_img.startAnimation(animZoomIn)
        Handler().postDelayed({
            runSetup()
        }, 1000)



    }
    private fun runSetup() {
        if (user != null && user!!.isEmailVerified) {
            if (settingsStorage.isRoom_joined) {
                nextActivity()
//                Log.e("SPLASH", "if-setting")
            } else {
                try {
                    ref.child(user!!.uid).child("IS_ROOM_JOINED").get()
                        .addOnCompleteListener { task: Task<DataSnapshot> ->
                            if (task.isSuccessful) {
                                try {
                                    val b = task.result!!.value as Boolean
//                                    Log.e("firebase", b.toString())
                                    if (b) {
                                        nextActivity()
                                    } else {
                                        goToRoomCreation(1)
                                    }
                                } catch (e: Exception) {
                                    Log.e("ERROR", e.message!!)
                                   goToRoomCreation(1)
                                }
                            }
                        }
                } catch (e: Exception) {
                    Log.e("ERROR", e.message!!)
                }
            }
        } else {
            goToRoomCreation(2)
        }
    }

    override fun onStop() {
        super.onStop()
        supportFinishAfterTransition()
    }

    private fun goToRoomCreation(int: Int){
        val intent =  Intent(applicationContext, AccountCreation::class.java)
        intent.putExtra("CODE", int)
        startActivity(intent, bundle)
    }
    private fun nextActivity() {
        startActivity(Intent(applicationContext, MainActivity::class.java), bundle)
    }
}