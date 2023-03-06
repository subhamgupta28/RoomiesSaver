package com.subhamgupta.roomiesapp.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.subhamgupta.roomiesapp.domain.model.ScheduleItem
import com.subhamgupta.roomiesapp.utils.AlarmScheduler
import java.time.ZoneId

class AndroidAlarmScheduler(
    private val context: Context
) : AlarmScheduler {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    override fun schedule(scheduleItem: ScheduleItem) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("MESSAGE", scheduleItem.message)
            putExtra("TIME", scheduleItem.time)
        }
        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            scheduleItem.time.atZone(ZoneId.systemDefault()).toEpochSecond()*1000,
            PendingIntent.getBroadcast(
                context,
                scheduleItem.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
    }

    override fun cancel(scheduleItem: ScheduleItem) {
        alarmManager.cancel(
            PendingIntent.getBroadcast(
                context,
                scheduleItem.hashCode(),
                Intent(context, AlarmReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
    }
}