package com.subhamgupta.roomiessaver.activities

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity

class Splash: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
        startActivity(Intent(this, SplashScreen::class.java), bundle)
        finish()
    }
}