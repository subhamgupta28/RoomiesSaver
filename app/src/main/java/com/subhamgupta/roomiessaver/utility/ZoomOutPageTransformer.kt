package com.subhamgupta.roomiessaver.utility

import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2


private const val MIN_SCALE = 0.98f
private const val MIN_ALPHA = 0.8f
class ZoomOutPageTransformer(view: ViewGroup) : ViewPager2.PageTransformer {

    override fun transformPage(view: View, position: Float) {
        view.apply {
            val pageWidth = view.width


            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.alpha = 1F
            } else if (position <= 1) { // [-1,1]
                view.translationX = -position * (pageWidth / 2) //Half the normal speed
            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.alpha = 1F
            }
        }
    }
}