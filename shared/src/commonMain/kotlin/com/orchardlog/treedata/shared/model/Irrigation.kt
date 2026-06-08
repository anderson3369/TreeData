package com.orchardlog.treedata.shared.model

import androidx.room.*
import com.orchardlog.treedata.shared.database.DateConverter
import com.orchardlog.treedata.shared.database.DateFormatter
import kotlinx.datetime.Instant

@Entity(foreignKeys = [
    ForeignKey(entity = IrrigationSystem::class, parentColumns = ["id"], childColumns = ["irrigationSystemId"])],
    indices = [Index("irrigationSystemId")]
)
@TypeConverters(DateConverter::class)
data class Irrigation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val irrigationSystemId: Long,
    val startTime: Instant,
    val stopTime: Instant,
    val firestoreId: String = ""
) {
    override fun toString(): String {
        return DateFormatter.formatDateTime(startTime)
    }
}
