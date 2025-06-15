package com.watb.chefmate.helper

import android.annotation.SuppressLint

object CommonHelper {
    @SuppressLint("DefaultLocale")
    fun parseNumber(number: Int): String {
        return when {
            number < 1000 -> "$number"
            number < 1000000 -> String.format("%.1fK", number / 1000f)
            else -> String.format("%.1fM", number / 1000000f)
        }
    }
}