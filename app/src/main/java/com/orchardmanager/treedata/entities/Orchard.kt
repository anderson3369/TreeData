package com.orchardmanager.treedata.entities

import androidx.room.*
import com.orchardmanager.treedata.data.DateConverter
import java.time.LocalDateTime

@Entity(foreignKeys = [
    ForeignKey(entity = Farm::class, parentColumns = ["id"], childColumns = ["farmId"])],
    indices = [Index("farmId")]
)
@TypeConverters(DateConverter::class)
data class Orchard(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val farmId: Long,
    val crop: String,
    val plantingDate: LocalDateTime
)
