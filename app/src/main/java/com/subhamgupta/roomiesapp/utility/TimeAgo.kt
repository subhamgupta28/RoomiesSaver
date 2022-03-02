package com.subhamgupta.roomiesapp.utility

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import android.text.format.DateUtils




class TimeAgo {
    fun getTimeDur(time: String): String{
        val formatter: DateFormat = SimpleDateFormat("dd-MM-yyyy hh:mm:ss", Locale.getDefault())
        val date = formatter.parse(time) as Date
        val t = getTimeAgo(date.time)
        return if (t!="N"){
            t
        }else{
            time
        }
    }
    private fun getTimeAgo(duration: Long): String {

        val now = Date()
//        Log.e("NOW", "${now.time} $duration")
        val seconds = TimeUnit.MILLISECONDS.toSeconds(now.time - duration)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(now.time - duration)
        val hours = TimeUnit.MILLISECONDS.toHours(now.time - duration)
        val days = TimeUnit.MILLISECONDS.toDays(now.time - duration)
//        Log.e("TIME", "$seconds $minutes $hours $days")
        return if (seconds < 60) {
            "just now"
        } else if (minutes == 1L) {
            "a minute ago"
        } else if (minutes in 2..59) {
            "$minutes minutes ago"
        } else if (hours == 1L) {
            "an hour ago"
        } else if (hours in 2..23) {
            "$hours hours ago"
        } else if (days == 1L) {
            "a day ago"
        } else if (days<=3L) {
            "$days days ago"
        }else{
            "N"
        }
    }
    val AVERAGE_MONTH_IN_MILLIS = DateUtils.DAY_IN_MILLIS * 30

    private fun getRelationTime(time: Long): String? {
        val now = Date().time
        val delta = now - time
        val resolution: Long
        resolution = if (delta <= DateUtils.MINUTE_IN_MILLIS) {
            DateUtils.SECOND_IN_MILLIS
        } else if (delta <= DateUtils.HOUR_IN_MILLIS) {
            DateUtils.MINUTE_IN_MILLIS
        } else if (delta <= DateUtils.DAY_IN_MILLIS) {
            DateUtils.HOUR_IN_MILLIS
        } else if (delta <= DateUtils.WEEK_IN_MILLIS) {
            DateUtils.DAY_IN_MILLIS
        } else return if (delta <= AVERAGE_MONTH_IN_MILLIS) {
            Integer.toString((delta / DateUtils.WEEK_IN_MILLIS).toInt()) + " weeks(s) ago"
        } else if (delta <= DateUtils.YEAR_IN_MILLIS) {
            Integer.toString((delta / AVERAGE_MONTH_IN_MILLIS).toInt()) + " month(s) ago"
        } else {
            Integer.toString((delta / DateUtils.YEAR_IN_MILLIS).toInt()) + " year(s) ago"
        }
        return DateUtils.getRelativeTimeSpanString(time, now, resolution).toString()
    }
}