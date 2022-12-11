package com.orchardmanager.treedata.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.orchardmanager.treedata.data.DateConverter
import java.time.LocalDateTime

@Entity
@TypeConverters(DateConverter::class)
data class PesticideApplication(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val orchardId: Long,
    val applicationDate: LocalDateTime,
    val productName: String,
    val eparegno: String,
    val applicationRate: Double,
    val applicationRateUnit: WeightOrMeasureUnit,
    val dilution: Double,
    val dilutionUnit: WeightOrMeasureUnit,
    val pesticideApplicationMethod: PesticideMethod

)

