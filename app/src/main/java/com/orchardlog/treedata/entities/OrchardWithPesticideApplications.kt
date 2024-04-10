package com.orchardlog.treedata.entities

import androidx.room.Embedded
import androidx.room.Relation

data class OrchardWithPesticideApplications(
    @Embedded
    val orchard: Orchard,
    @Relation(
        parentColumn = "id",
        entityColumn = "pesticideApplicationId"
    )
    val pesticideApplications: List<PesticideApplication>
)
