package com.orchardlog.treedata.entities

import androidx.room.*
import com.orchardlog.treedata.data.DateConverter
import com.orchardlog.treedata.data.EnumConverter
import java.time.LocalDate

@Entity
@TypeConverters(DateConverter::class, EnumConverter::class)
data class Tree (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val orchardId: Long,
    val rootstockId: Long,
    val varietyId: Long,
    val plantedDate: LocalDate,
    @ColumnInfo("treeRanking", defaultValue = "Good")
    val treeRanking: TreeRanking,
    val notes: String,
    val latitude: Double,
    val longitude: Double
)
