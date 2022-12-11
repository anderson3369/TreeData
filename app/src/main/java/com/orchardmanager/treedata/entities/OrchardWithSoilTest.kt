package com.orchardmanager.treedata.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Relation

@Entity
data class OrchardWithSoilTest(
    @Embedded
    val orchard: Orchard,
    @Relation(
        parentColumn = "id",
        entityColumn = "soilTestId"
    )
    val soilTests: List<SoilTest>
)
