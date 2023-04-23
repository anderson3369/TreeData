package com.orchardmanager.treedata.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.orchardmanager.treedata.data.DateConverter
import com.orchardmanager.treedata.data.EnumConverter
import java.time.LocalDateTime

@Entity
@TypeConverters(DateConverter::class, EnumConverter::class)
data class PesticideApplication(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val orchardId: Long,
    val pesticideId: Long,
    val applicationStart: LocalDateTime,
    val applicationStop: LocalDateTime,
    val applied: Double,
    val appliedUnit: WeightOrMeasureUnit,
    val dilution: Int,
    val dilutionUnit: WeightOrMeasureUnit,
    val areaTreated: Double,
    val areaTreatedUnit: OrchardUnit,
    val applicationMethod: ApplicationMethod

) {
    override fun toString(): String {
        return DateConverter().fromOffsetDateTime(applicationStop)!!
    }
}

