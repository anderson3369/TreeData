package com.orchardlog.treedata.shared.database

import androidx.room.TypeConverter
import kotlinx.datetime.Instant

/**
 * Room TypeConverter for Instant <-> Long (epoch milliseconds).
 * Storing as Long ensures SQL BETWEEN queries work correctly with numeric comparison.
 * Uses kotlinx.datetime.Instant (typealias to kotlin.time.Instant) for KSP compatibility.
 */
object DateConverter {

    @TypeConverter
    fun toInstant(millis: Long): Instant = Instant.fromEpochMilliseconds(millis)

    @TypeConverter
    fun fromInstant(date: Instant): Long = date.toEpochMilliseconds()
}
