package com.appcitas.app.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object Extensions {

    fun String.toFormattedDate(): String {
        return try {
            val inputFormat = SimpleDateFormat(Constants.DATETIME_FORMAT, Locale.US)
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
            val date = inputFormat.parse(this)
            date?.let { outputFormat.format(it) } ?: this
        } catch (e: Exception) {
            this
        }
    }

    fun String.toFormattedTime(): String {
        return try {
            val inputFormat = SimpleDateFormat(Constants.DATETIME_FORMAT, Locale.US)
            val outputFormat = SimpleDateFormat("HH:mm", Locale.US)
            val date = inputFormat.parse(this)
            date?.let { outputFormat.format(it) } ?: this
        } catch (e: Exception) {
            this
        }
    }

    fun String.timeAgo(): String {
        return try {
            val inputFormat = SimpleDateFormat(Constants.DATETIME_FORMAT, Locale.US)
            val date = inputFormat.parse(this) ?: return this
            val now = Date()
            val diff = now.time - date.time

            val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
            val hours = TimeUnit.MILLISECONDS.toHours(diff)
            val days = TimeUnit.MILLISECONDS.toDays(diff)

            when {
                minutes < 1 -> "Ahora"
                minutes < 60 -> "${minutes}m"
                hours < 24 -> "${hours}h"
                days < 7 -> "${days}d"
                else -> this.toFormattedDate()
            }
        } catch (e: Exception) {
            this
        }
    }

    fun Double.formatDistance(): String {
        return when {
            this < 1.0 -> "${(this * 1000).toInt()}m"
            this < 100 -> "${String.format("%.1f", this)}km"
            else -> "${this.toInt()}km"
        }
    }

    fun Int.toChatUnreadBadge(): String {
        return when {
            this > 99 -> "99+"
            this > 0 -> this.toString()
            else -> ""
        }
    }
}
