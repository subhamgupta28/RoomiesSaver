package com.subhamgupta.roomiesapp.utils

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
<<<<<<< HEAD
import android.os.Process.killProcess
import android.util.Log
=======
>>>>>>> bc028885d2fc69567c10e880a1180fc67f3a028b
import com.subhamgupta.roomiesapp.MyApp
import com.subhamgupta.roomiesapp.activities.MainActivity
import kotlin.system.exitProcess


class Handler(
<<<<<<< HEAD
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
=======
    private var activity: Activity?=null
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(p0: Thread, p1: Throwable) {
        val intent = Intent(activity, MainActivity::class.java)
        intent.putExtra("crash", true)
        intent.addFlags(
            Intent.FLAG_ACTIVITY_CLEAR_TOP
                    or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    or Intent.FLAG_ACTIVITY_NEW_TASK
        )
        val pendingIntent = PendingIntent.getActivity(
            MyApp().getInstance().baseContext,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT
        )
        val mgr = MyApp().getInstance().baseContext
            .getSystemService(Context.ALARM_SERVICE) as AlarmManager
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pendingIntent)
        activity?.finish()
        exitProcess(2)
>>>>>>> bc028885d2fc69567c10e880a1180fc67f3a028b
    }
}