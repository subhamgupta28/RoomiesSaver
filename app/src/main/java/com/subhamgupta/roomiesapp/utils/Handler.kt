package com.subhamgupta.roomiesapp.utils

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Process.killProcess
import android.util.Log
import com.subhamgupta.roomiesapp.MyApp
import com.subhamgupta.roomiesapp.activities.MainActivity
import kotlin.system.exitProcess


class Handler(
    private var activity: Activity?=null,
    private var context: Context,
    private var pendingIntent: PendingIntent
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(p0: Thread, p1: Throwable) {
        val mgr = context
            .getSystemService(Context.ALARM_SERVICE) as AlarmManager
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pendingIntent)
        activity?.finish()
        Log.e("error","${p1.message}")
//        killProcess(android.os.Process.myPid());
        exitProcess(0)
    }
}