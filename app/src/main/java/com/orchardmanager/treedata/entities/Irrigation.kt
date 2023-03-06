package com.orchardmanager.treedata.entities

import androidx.room.*
import com.orchardmanager.treedata.data.DateConverter
import java.time.LocalDateTime

@Entity(foreignKeys = [
    ForeignKey(entity = IrrigationSystem::class, parentColumns = ["id"], childColumns = ["irrigationSystemId"])],
    indices = [Index("irrigationSystemId")]
)
@TypeConverters(DateConverter::class)
data class Irrigation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val irrigationSystemId: Long,
    val startTime: LocalDateTime,
    val stopTime: LocalDateTime
) {
    override fun toString(): String {
        return DateConverter().fromOffsetDateTime(startTime)!!
    }
}
