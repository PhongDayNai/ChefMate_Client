package com.watb.chefmate.helper

import android.annotation.SuppressLint
import java.text.Normalizer
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

    fun parseName(name: String): String {
        val normalized = Normalizer.normalize(name, Normalizer.Form.NFD)
        val noAccents = normalized.replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
            .replace('đ', 'd')
            .replace('Đ', 'D')

        return noAccents
            .trim()
            .split("\\s+".toRegex())
            .joinToString("_")
    }
}