package com.orchardlog.treedata.shared.model

import androidx.room.*
import com.orchardlog.treedata.shared.database.DateConverter
import kotlinx.datetime.Instant

@Entity(foreignKeys = [
    ForeignKey(entity = Farm::class, parentColumns = ["id"], childColumns = ["farmId"])],
    indices = [Index("farmId")]
)
@TypeConverters(DateConverter::class)
data class Orchard(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val farmId: Long,
    val crop: String,
    val plantedDate: String,
    val rowWidth: Double,
    val rowWidthLinearUnit: LinearUnit,
    val distanceBetweenTrees: Double,
    val distanceBetweenTreesLinearUnit: LinearUnit,
    val sand: Double,
    val silt: Double,
    val clay: Double,
    val organicMatter: Double,
    val persistentId: String, // UUID to track same orchard across versions
    val validFrom: Instant,
    val validTo: Instant? = null
) {
    override fun toString():String = crop + "      " + plantedDate
}
