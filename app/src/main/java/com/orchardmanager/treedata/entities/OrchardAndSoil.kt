package com.orchardmanager.treedata.entities

import androidx.room.Embedded
import androidx.room.Relation

data class OrchardAndSoil(
    @Embedded
    val orchard: Orchard,
    @Relation(
        parentColumn = "id",
        entityColumn = "soilId"
    )
    val soil: Soil
)
