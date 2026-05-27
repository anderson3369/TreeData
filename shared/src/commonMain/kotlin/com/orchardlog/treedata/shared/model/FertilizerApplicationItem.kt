package com.orchardlog.treedata.shared.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    primaryKeys = ["fertilizerApplicationId", "fertilizerId"],
    foreignKeys = [
        ForeignKey(
            entity = FertilizerApplication::class,
            parentColumns = ["id"],
            childColumns = ["fertilizerApplicationId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Fertilizer::class,
            parentColumns = ["id"],
            childColumns = ["fertilizerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("fertilizerId")]
)
data class FertilizerApplicationItem(
    val fertilizerApplicationId: Long,
    val fertilizerId: Long,
    val applied: Double,
    val appliedUnit: WeightOrMeasureUnit
)
