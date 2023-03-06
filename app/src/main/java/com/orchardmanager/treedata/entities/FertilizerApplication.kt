package com.orchardmanager.treedata.entities

import androidx.room.*
import com.orchardmanager.treedata.data.DateConverter
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@TypeConverters(DateConverter::class)
data class FertilizerApplication(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val orchardId: Long,
    val applicationDate: LocalDate,
    val fertilizerId: Long,
    val applied: Double,
    val weightOrMeasureUnit: WeightOrMeasureUnit
)
