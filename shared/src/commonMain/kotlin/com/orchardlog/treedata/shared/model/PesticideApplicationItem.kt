package com.orchardlog.treedata.shared.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    primaryKeys = ["pesticideApplicationId", "pesticideId"],
    foreignKeys = [
        ForeignKey(
            entity = PesticideApplication::class,
            parentColumns = ["id"],
            childColumns = ["pesticideApplicationId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Pesticide::class,
            parentColumns = ["id"],
            childColumns = ["pesticideId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("pesticideId")]
)
data class PesticideApplicationItem(
    val pesticideApplicationId: Long,
    val pesticideId: Long,
    val applied: Double,
    val appliedUnit: WeightOrMeasureUnit
)
