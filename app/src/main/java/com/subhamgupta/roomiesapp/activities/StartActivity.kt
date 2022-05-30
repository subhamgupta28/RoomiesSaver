package com.subhamgupta.roomiesapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.isVisible
import com.google.android.material.color.DynamicColors
import com.google.firebase.auth.FirebaseAuth
import com.subhamgupta.roomiesapp.R
import com.subhamgupta.roomiesapp.data.viewmodels.FirebaseViewModel
import com.subhamgupta.roomiesapp.databinding.ActivityStartBinding

class StartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStartBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        DynamicColors.applyToActivitiesIfAvailable(application)
        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.lifecycleOwner = this


//        if (viewModel.getSettings().darkMode == true)
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
//        else
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
//        binding.logo.visibility = View.VISIBLE
//        val animZoomIn = AnimationUtils.loadAnimation(this, R.anim.zoom_in)
//        binding.logo.startAnimation(animZoomIn)
        val user = FirebaseAuth.getInstance()
        if (user.currentUser != null) {
            binding.fragmentContainerView.isVisible = false
            startActivity(Intent(this@StartActivity, MainActivity::class.java))
            finish()
        } else {
            binding.fragmentContainerView.isVisible = true
        }


    }


}