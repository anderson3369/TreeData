package com.orchardmanager.treedata.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Relation


data class FertilizerApplicationWithFertilizers(
    @Embedded
    val fertilizerApplication: FertilizerApplication,
    @Relation(
        parentColumn = "id",
        entityColumn = "fertilizerId"
    )
    val fertilizers: List<Fertilizer>
)
