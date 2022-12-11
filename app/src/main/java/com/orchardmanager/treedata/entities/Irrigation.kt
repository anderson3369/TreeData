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
    @ColumnInfo(name="rowid")
    val id: Long,
    val irrigationSystemId: Long,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime
)
