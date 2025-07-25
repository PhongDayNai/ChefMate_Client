package com.watb.chefmate.helper

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.ui.text.toLowerCase
import com.watb.chefmate.data.IngredientItem
import com.watb.chefmate.data.Recipe
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
        isoFormat.timeZone = TimeZone.getTimeZone("Asia/Bangkok")
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

    fun parseTimeToDMY(time: String): String {
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        isoFormat.timeZone = TimeZone.getTimeZone("UTC")

        return try {
            val date = isoFormat.parse(time)
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            outputFormat.format(date ?: return time)
        } catch (e: Exception) {
            time
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

    fun String.parseIngredientName(): String {
        return this.trim()
            .lowercase()
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }

    fun consolidateIngredients(
        recipes: List<Recipe>? = null,
        manualIngredients: List<IngredientItem>? = null
    ): List<IngredientItem> {
        val consolidated = mutableListOf<IngredientItem>()

        recipes?.let { recipeList ->
            recipeList.forEach { recipe ->
                recipe.ingredients.forEach { recipeIngredient ->
                    val existingIngredient = consolidated.find {
                        it.ingredientName.lowercase(Locale.ROOT) == recipeIngredient.ingredientName.lowercase(Locale.ROOT)
                        && it.unit.trim().lowercase() == recipeIngredient.unit.trim().lowercase()
                    }
                    if (existingIngredient != null) {
                        val newWeight = existingIngredient.weight + recipeIngredient.weight
                        consolidated.remove(existingIngredient)
                        consolidated.add(existingIngredient.copy(weight = newWeight))
                    } else {
                        consolidated.add(recipeIngredient)
                    }
                }
            }
        }

        manualIngredients?.let { manualIngredientList ->
            manualIngredientList.forEach { manualIngredient ->
                val existingIngredient = consolidated.find {
                    it.ingredientName.lowercase(Locale.ROOT) == manualIngredient.ingredientName.lowercase(Locale.ROOT)
                    && it.unit.trim().lowercase() == manualIngredient.unit.trim().lowercase()
                }
                if (existingIngredient != null) {
                    val newWeight = existingIngredient.weight + manualIngredient.weight
                    consolidated.remove(existingIngredient)
                    consolidated.add(existingIngredient.copy(weight = newWeight))
                } else {
                    consolidated.add(manualIngredient)
                }
            }
        }

        return consolidated
    }

    private const val TAG = "CommonHelper"
}