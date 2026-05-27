package com.orchardlog.treedata.shared.model

import androidx.room.*
import com.orchardlog.treedata.shared.database.DateConverter
import com.orchardlog.treedata.shared.database.EnumConverter
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

@Entity
@TypeConverters(DateConverter::class, EnumConverter::class)
data class Tree (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val orchardId: Long,
    val rootstockId: Long,
    val varietyId: Long,
    val plantedDate: String,
    @ColumnInfo("treeRanking", defaultValue = "Good")
    val treeRanking: TreeRanking,
    val notes: String,
    val latitude: Double,
    val longitude: Double,
    val persistentId: String, // UUID to track same tree across versions
    val validFrom: Instant,
    val validTo: Instant? = null
)
