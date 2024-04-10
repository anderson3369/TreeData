package com.orchardlog.treedata.entities

import androidx.room.Embedded
import androidx.room.Relation


data class FertilizerApplicationWithFertilizers(
    @Embedded
    val fertilizerApplication: FertilizerApplication,
    @Relation(
        parentColumn = "fertilizerId",
        entityColumn = "id"
    )
    val fertilizers: MutableList<Fertilizer>
)
