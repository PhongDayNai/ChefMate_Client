package com.watb.chefmate.helper

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object CommonHelper {
    @SuppressLint("DefaultLocale")
    fun parseNumber(number: Int): String {
        return when {
            number < 1000 -> "$number"
            number < 1000000 -> String.format("%.1fK", number / 1000f)
            else -> String.format("%.1fM", number / 1000000f)
        }
    }

//    fun parseTime(time: String): String {
//        val dateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(time)
//        val dtYear = dateTime!!.year
//        val dtMonth = dateTime.month
//        val dtDay = dateTime.day
//        val dtHour = dateTime.hours
//        val dtMinute = dateTime.minutes
//        val dtSecond = dateTime.seconds
//
//        val now = System.currentTimeMillis()
//        val dateTimeNowStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(now)
//        val dateTimeNow = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(dateTimeNowStr)
//        val dtNowYear = dateTimeNow!!.year
//        val dtNowMonth = dateTimeNow.month
//        val dtNowDay = dateTimeNow.day
//        val dtNowHour = dateTimeNow.hours
//        val dtNowMinute = dateTimeNow.minutes
//        val dtNowSecond = dateTimeNow.seconds
//
//        val diffYear = dtNowYear - dtYear
//        if (diffYear > 0) {
//            return "$diffYear năm trước"
//        } else {
//            val diffMonth = dtNowMonth - dtMonth
//            if (diffMonth > 0) {
//                return "$diffMonth tháng trước"
//            } else {
//                val diffDay = dtNowDay - dtDay
//                if (diffDay > 0) {
//                    if (diffDay < 7) {
//                        return "$diffDay ngày trước"
//                    } else {
//                        val diffWeek = diffDay / 7
//                        return "$diffWeek tuần trước"
//                    }
//                } else {
//                    val diffHour = dtNowHour - dtHour
//                    if (diffHour > 0) {
//                        return "$diffHour giờ trước"
//                    } else {
//                        val diffMinute = dtNowMinute - dtMinute
//                        if (diffMinute > 0) {
//                            return "$diffMinute phút trước"
//                        } else {
//                            val diffSecond = dtNowSecond - dtSecond
//                            return "$diffSecond giây trước"
//                        }
//                    }
//                }
//            }
//        }
//    }

    fun parseTime(time: String): String {
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        isoFormat.timeZone = TimeZone.getTimeZone("UTC")
        val dateTime = isoFormat.parse(time) ?: return ""

        val now = Date()

        val diffMillis = now.time - dateTime.time

        val diffSeconds = diffMillis / 1000
        val diffMinutes = diffSeconds / 60
        val diffHours = diffMinutes / 60
        val diffDays = diffHours / 24
        val diffWeeks = diffDays / 7
        val diffMonths = diffDays / 30
        val diffYears = diffDays / 365

        return when {
            diffYears > 0 -> "$diffYears năm trước"
            diffMonths > 0 -> "$diffMonths tháng trước"
            diffWeeks > 0 -> "$diffWeeks tuần trước"
            diffDays > 0 -> "$diffDays ngày trước"
            diffHours > 0 -> "$diffHours giờ trước"
            diffMinutes > 0 -> "$diffMinutes phút trước"
            else -> "$diffSeconds giây trước"
        }
    }

    fun toIso8601UTC(date: Date): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(date)
    }

    fun String.parseIngredientName(): String {
        return this.trim()
            .lowercase()
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }

}