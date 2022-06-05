package com.subhamgupta.roomiesapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.subhamgupta.roomiesapp.R
import com.subhamgupta.roomiesapp.databinding.ActivityStartBinding
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

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


    }

    override fun onStart() {
        super.onStart()
        val user = FirebaseAuth.getInstance()
        lifecycleScope.launchWhenStarted {
            val animZoomIn = AnimationUtils.loadAnimation(this@StartActivity, R.anim.zoom_in)
            binding.logo.startAnimation(animZoomIn)
            binding.logo.visibility = View.GONE
            withContext(Main) {
                if (user.currentUser != null) {
                    startActivity(Intent(this@StartActivity, MainActivity::class.java))
                    finish()
                } else {
                    binding.fragmentContainerView.isVisible = true
                }
            }

        }
    }


}