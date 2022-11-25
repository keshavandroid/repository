package com.reloop.reloop.utils

import java.util.*

object TimeComparison {

    fun checkTime(time: String): Boolean {
        val fromTime: Calendar
        val toTime: Calendar
        val currentTime: Calendar
        try {
            val times = time.split("-").toTypedArray()
            val from = times[0].split(":").toTypedArray()
            val until = times[1].split(":").toTypedArray()
            fromTime = Calendar.getInstance()
            fromTime.set(Calendar.HOUR_OF_DAY, Integer.valueOf(from[0]))
            fromTime.set(Calendar.MINUTE, Integer.valueOf(from[1]))
            toTime = Calendar.getInstance()
            toTime.set(Calendar.HOUR_OF_DAY, Integer.valueOf(until[0]))
            toTime.set(Calendar.MINUTE, Integer.valueOf(until[1]))
            val calendar=  Calendar.getInstance()
            calendar.timeZone= TimeZone.getDefault()
            calendar.timeInMillis=System.currentTimeMillis()
            currentTime = calendar
//            currentTime.set(Calendar.HOUR, Calendar.HOUR)
//            currentTime.set(Calendar.MINUTE, Calendar.MINUTE)
            if (currentTime.after(fromTime) && currentTime.before(toTime)) {
                return true
            }
        } catch (e: Exception) {
            return false
        }
        return false
    }
}