package com.orchardlog.treedata.shared.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.orchardlog.treedata.shared.database.DateConverter
import com.orchardlog.treedata.shared.database.DateFormatter
import kotlinx.datetime.Instant

@Entity
@TypeConverters(DateConverter::class)
data class SoilMoisture(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val orchardId: Long = 0L,
    val date: Instant,
    val centibar: Int,
    val percent: Int,
    val firestoreId: String = ""
) {
    fun description(): String {
        return "Moisture: $percent% ($centibar cb)"
    }

    override fun toString(): String {
        return DateFormatter.formatDateTime(date)
    }
}
