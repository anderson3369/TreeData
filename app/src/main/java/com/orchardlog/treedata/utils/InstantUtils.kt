package com.orchardlog.treedata.utils

import java.util.Calendar
import java.util.Locale
import kotlin.time.Instant

/**
 * Utility functions for converting between DatePicker strings (MM-DD-YYYY) and Instant.
 */
object InstantUtils {

    /** Parse "MM-DD-YYYY" string to Instant at start of day. */
    fun parseStartOfDay(dateStr: String): Instant? {
        val parts = dateStr.split("-")
        if (parts.size != 3) return null
        val month = parts[0].toIntOrNull() ?: return null
        val day = parts[1].toIntOrNull() ?: return null
        val year = parts[2].toIntOrNull() ?: return null
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, day, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return Instant.fromEpochMilliseconds(cal.timeInMillis)
    }

    /** Parse "MM-DD-YYYY" string to Instant at end of day (23:59:59.999). */
    fun parseEndOfDay(dateStr: String): Instant? {
        val parts = dateStr.split("-")
        if (parts.size != 3) return null
        val month = parts[0].toIntOrNull() ?: return null
        val day = parts[1].toIntOrNull() ?: return null
        val year = parts[2].toIntOrNull() ?: return null
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, day, 23, 59, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return Instant.fromEpochMilliseconds(cal.timeInMillis)
    }

    /** Format Instant as "MM-DD-YYYY" for display. */
    fun formatAsDate(instant: Instant): String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = instant.toEpochMilliseconds()
        return String.format(
            Locale.US, "%02d-%02d-%d",
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH),
            cal.get(Calendar.YEAR)
        )
    }

    /** Get start of year as Instant. */
    fun startOfYear(year: Int): Instant {
        val cal = Calendar.getInstance()
        cal.set(year, 0, 1, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return Instant.fromEpochMilliseconds(cal.timeInMillis)
    }

    /** Get end of year as Instant (Dec 31, 23:59:59.999). */
    fun endOfYear(year: Int): Instant {
        val cal = Calendar.getInstance()
        cal.set(year, 11, 31, 23, 59, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return Instant.fromEpochMilliseconds(cal.timeInMillis)
    }
}
