package com.orchardlog.treedata.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.orchardlog.treedata.data.DateConverter
import java.time.LocalDateTime

@Entity
@TypeConverters(DateConverter::class)
data class OrchardActivity (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val orchardId: Long,
    val activity: String,
    val notes: String,
    val activityStart: LocalDateTime,
    val activityStop: LocalDateTime
) {
    override fun toString(): String {
        return activity + "  " + hours() + "   hours"
    }

    private fun hours(): Long {
        return java.time.Duration.between(activityStart, activityStop).toHours()
    }
}
