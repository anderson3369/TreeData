package com.orchardlog.treedata.shared.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.orchardlog.treedata.shared.database.DateConverter
import com.orchardlog.treedata.shared.database.DateFormatter
import com.orchardlog.treedata.shared.database.EnumConverter
import kotlinx.datetime.Instant


@Entity
@TypeConverters(DateConverter::class, EnumConverter::class)
data class PesticideApplication(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val orchardId: Long,
    val applicationStart: Instant,
    val applicationStop: Instant,
    val dilution: Int,
    val dilutionUnit: WeightOrMeasureUnit,
    val areaTreated: Double,
    val areaTreatedUnit: OrchardUnit,
    val applicationMethod: ApplicationMethod,
    val firestoreId: String = ""
) {
    override fun toString(): String {
        return "Application $id"
    }

    fun description(): String {
        return DateFormatter.formatDateTime(applicationStop)
    }
}
