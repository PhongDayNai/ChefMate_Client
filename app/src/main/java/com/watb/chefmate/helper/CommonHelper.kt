package com.watb.chefmate.helper

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.Locale

object CommonHelper {
    @SuppressLint("DefaultLocale")
    fun parseNumber(number: Int): String {
        return when {
            number < 1000 -> "$number"
            number < 1000000 -> String.format("%.1fK", number / 1000f)
            else -> String.format("%.1fM", number / 1000000f)
        }
    }

    fun parseTime(time: String): String {
        val dateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(time)
        val dtYear = dateTime!!.year
        val dtMonth = dateTime.month
        val dtDay = dateTime.day
        val dtHour = dateTime.hours
        val dtMinute = dateTime.minutes
        val dtSecond = dateTime.seconds

        val now = System.currentTimeMillis()
        val dateTimeNowStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(now)
        val dateTimeNow = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(dateTimeNowStr)
        val dtNowYear = dateTimeNow!!.year
        val dtNowMonth = dateTimeNow.month
        val dtNowDay = dateTimeNow.day
        val dtNowHour = dateTimeNow.hours
        val dtNowMinute = dateTimeNow.minutes
        val dtNowSecond = dateTimeNow.seconds

        val diffYear = dtNowYear - dtYear
        if (diffYear > 0) {
            return "$diffYear năm trước"
        } else {
            val diffMonth = dtNowMonth - dtMonth
            if (diffMonth > 0) {
                return "$diffMonth tháng trước"
            } else {
                val diffDay = dtNowDay - dtDay
                if (diffDay > 0) {
                    if (diffDay < 7) {
                        return "$diffDay ngày trước"
                    } else {
                        val diffWeek = diffDay / 7
                        return "$diffWeek tuần trước"
                    }
                } else {
                    val diffHour = dtNowHour - dtHour
                    if (diffHour > 0) {
                        return "$diffHour giờ trước"
                    } else {
                        val diffMinute = dtNowMinute - dtMinute
                        if (diffMinute > 0) {
                            return "$diffMinute phút trước"
                        } else {
                            val diffSecond = dtNowSecond - dtSecond
                            return "$diffSecond giây trước"
                        }
                    }
                }
            }
        }
    }
}