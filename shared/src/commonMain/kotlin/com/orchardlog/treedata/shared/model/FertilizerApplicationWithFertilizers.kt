package com.orchardlog.treedata.shared.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation


data class FertilizerApplicationWithFertilizers(
    @Embedded
    val fertilizerApplication: FertilizerApplication,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            FertilizerApplicationItem::class,
            parentColumn = "fertilizerApplicationId",
            entityColumn = "fertilizerId"
        )
    )
    val fertilizers: List<Fertilizer>
)
