package com.orchardmanager.treedata.entities

import androidx.room.*
import com.orchardmanager.treedata.data.DateConverter
import java.time.LocalDate
import java.time.LocalDateTime

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
    val plantedDate: LocalDate,
    val rowWidth: Double,
    val rowWidthLinearUnit: LinearUnit,
    val distanceBetweenTrees: Double,
    val distanceBetweenTreesLinearUnit: LinearUnit,
    val sand: Double,
    val silt: Double,
    val clay: Double,
    val organicMatter: Double

) {
    override fun toString():String = crop + "      " + plantedDate
}
