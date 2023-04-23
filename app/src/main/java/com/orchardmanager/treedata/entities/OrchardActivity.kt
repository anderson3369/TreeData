package com.orchardmanager.treedata.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.orchardmanager.treedata.data.DateConverter
import java.time.LocalDateTime

@Entity
@TypeConverters(DateConverter::class)
data class OrchardActivity (
    @PrimaryKey
    val id: Long = 0L,
    val activity: String,
    val notes: String,
    val activityStart: LocalDateTime,
    val activityStop: LocalDateTime
) {
    override fun toString(): String {
        return activity
    }
}
