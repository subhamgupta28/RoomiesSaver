package com.subhamgupta.roomiesapp.utils

import com.subhamgupta.roomiesapp.domain.model.ScheduleItem

interface AlarmScheduler {
    fun schedule(scheduleItem: ScheduleItem)
    fun cancel(scheduleItem: ScheduleItem)
}