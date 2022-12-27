package com.orchardmanager.treedata.data

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor

class DateConverter {
    val df = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    val daet = DateTimeFormatter.ISO_LOCAL_DATE

    @TypeConverter
    fun toOffsetDateTime(date: String?): LocalDateTime? {
        return df.parse(
            date
        ) { temporal: TemporalAccessor? ->
            LocalDateTime.from(
                temporal
            )
        }
    }

    @TypeConverter
    fun toOffsetDate(date: String?): LocalDate? {
        val format = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return format.parse(
            date
        ) { temporal: TemporalAccessor? ->
            LocalDate.from(
                temporal
            )
        }
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