package com.orchardmanager.treedata.data

import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor

class DateConverter {
    val df = DateTimeFormatter.ISO_OFFSET_DATE_TIME

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
    fun fromOffsetDateTime(date: LocalDateTime): String? {
        return date.format(df)
    }

}