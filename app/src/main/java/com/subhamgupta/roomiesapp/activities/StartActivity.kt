package com.subhamgupta.roomiesapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.color.DynamicColors
import com.google.firebase.auth.FirebaseAuth
import com.subhamgupta.roomiesapp.R
import com.subhamgupta.roomiesapp.databinding.ActivityStartBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.withContext
<<<<<<< HEAD

=======
import java.io.File
>>>>>>> bc028885d2fc69567c10e880a1180fc67f3a028b
@AndroidEntryPoint
class StartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStartBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        DynamicColors.applyToActivitiesIfAvailable(application)
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
//        binding.fragmentContainerView.isVisible = true

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