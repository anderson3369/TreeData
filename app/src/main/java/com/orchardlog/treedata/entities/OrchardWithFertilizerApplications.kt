package com.orchardlog.treedata.entities

import androidx.room.Embedded
import androidx.room.Relation

data class OrchardWithFertilizerApplications(
    @Embedded
    val orchard: Orchard,
    @Relation(
        parentColumn = "id",
        entityColumn = "fertilizerApplicationId"
    )
    val fertilizerApplications: List<FertilizerApplication>
)
