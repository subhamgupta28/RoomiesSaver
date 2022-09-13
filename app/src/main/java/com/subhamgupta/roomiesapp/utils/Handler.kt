package com.subhamgupta.roomiesapp.utils

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.util.Log
import kotlin.system.exitProcess


class Handler(
    private var activity: Activity?=null,
    private var context: Context,
    private var pendingIntent: PendingIntent
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(p0: Thread, p1: Throwable) {
        val mgr = context
            .getSystemService(Context.ALARM_SERVICE) as AlarmManager
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 200, pendingIntent)
        activity?.finish()
        Log.e("APP ERROR","${p1.message}")
        exitProcess(0)
    }
}