package com.orchardlog.treedata.shared.database

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.Instant

/**
 * Display-only date formatting utility.
 * Use this for UI display — NOT for database storage (DateConverter handles that).
 */
object DateFormatter {

    private val dateTimeFormat = LocalDateTime.Format {
        monthNumber()
        char('/')
        this@Format.day(padding = Padding.ZERO)
        char('/')
        year()
        char(' ')
        hour()
        char(':')
        minute()
    }

    private val dateOnlyFormat = LocalDateTime.Format {
        monthNumber()
        char('/')
        this@Format.day(padding = Padding.ZERO)
        char('/')
        year()
    }

    fun formatDateTime(instant: Instant): String {
        val ldt = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return ldt.format(dateTimeFormat)
    }

    fun formatDate(instant: Instant): String {
        val ldt = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return ldt.format(dateOnlyFormat)
    }
}
