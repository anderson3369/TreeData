package com.orchardmanager.treedata.entities

import androidx.room.*
import com.orchardmanager.treedata.data.DateConverter
import java.time.LocalDate

@Entity
@TypeConverters(DateConverter::class)
data class Tree (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val orchardId: Long,
    val rootstockId: Long,
    val varietyId: Long,
    val plantedDate: LocalDate,
    val notes: String,
    val latitude: Double,
    val longitude: Double
)
