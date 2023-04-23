package com.orchardmanager.treedata.entities

import androidx.room.*
import com.orchardmanager.treedata.data.DateConverter
import com.orchardmanager.treedata.data.EnumConverter
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@TypeConverters(DateConverter::class, EnumConverter::class)
data class FertilizerApplication(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val orchardId: Long,
    val fertilizerId: Long,
    val applicationStart: LocalDateTime,
    val applicationStop: LocalDateTime,
    val applied: Double,
    val weightOrMeasureUnit: WeightOrMeasureUnit,
    val areaTreated: Double,
    val orchardUnit: OrchardUnit
) {
    override fun toString(): String {
        return DateConverter().fromOffsetDateTime(applicationStop)!!
    }
}
