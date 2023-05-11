package com.orchardmanager.treedata.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.orchardmanager.treedata.data.DateConverter
import java.time.LocalDateTime

@Entity
@TypeConverters(DateConverter::class)
data class SoilMoisture(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val orchardId: Long = 0L,
    val date: LocalDateTime,
    val centibar: Int,
    val percent: Int
)
