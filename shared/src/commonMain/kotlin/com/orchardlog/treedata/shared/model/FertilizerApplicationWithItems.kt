package com.orchardlog.treedata.shared.model

import androidx.room.Embedded
import androidx.room.Relation

data class FertilizerApplicationWithItems(
    @Embedded
    val application: FertilizerApplication,
    @Relation(
        parentColumn = "id",
        entityColumn = "fertilizerApplicationId"
    )
    val items: List<FertilizerApplicationItem>
)
