package com.watb.chefmate.data

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object AppConstant {
    val headerGradient = Brush.horizontalGradient(
        colorStops = arrayOf(
            0.0f to Color(0xFFF97316),
            0.5f to Color(0xFFFB923C),
            1.0f to Color(0xFFFDBA74)
        )
    )

    val backgroundGradient = Brush.verticalGradient(
        colorStops = arrayOf(
            0.0f to Color(0xFFF97316),
            0.075f to Color(0xFFFB923C),
            0.225f to Color(0xFFFDBA74),
            1.0f to Color(0xFFFDBA74)
        )
    )

    val backgroundProfileGradient = Brush.verticalGradient(
        colorStops = arrayOf(
            0.0f to Color(0xFFF97316),
            0.5f to Color(0xFFFDBA74),
            1.0f to Color(0xFFFDBA74)
        )
    )

    val unselectedPrimaryGradient = Brush.verticalGradient(
        colorStops = arrayOf(
            0.0f to Color(0xFFC3C3C6),
            1.0f to Color(0xFFC3C3C6)
        )
    )

    val onPrimaryGradient = Brush.horizontalGradient(
        colorStops = arrayOf(
            0.0f to Color(0xFFFF7121),
            1.0f to Color(0xFFFEAA43)
        )
    )
}