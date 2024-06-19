package com.orchardlog.treedata.data

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor

class DateConverter {
    private val df: DateTimeFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm")
    private val daet: DateTimeFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy")
    private val dft: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    @TypeConverter
    fun toOffsetDateTime(date: String?): LocalDateTime? {
        return df.parse(date) { temporal: TemporalAccessor? ->
            LocalDateTime.from(temporal)
        }
    }

    @TypeConverter
    fun toOffsetDate(date: String?): LocalDate? {
        val format = DateTimeFormatter.ofPattern("MM-dd-yyyy")
        return format.parse(date) { temporal: TemporalAccessor? ->
            LocalDate.from(temporal)
        }
    }

    @TypeConverter
    fun fromTime(time: LocalTime): String? {
        return time.format(dft)
    }

    @TypeConverter
    fun fromOffsetDateTime(date: LocalDateTime): String? {
        return date.format(df)
    }

    @TypeConverter
    fun fromOffsetDate(date: LocalDate): String? {
        return date.format(daet)
    }

}