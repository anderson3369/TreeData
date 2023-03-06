package com.orchardmanager.treedata.entities

import androidx.room.Embedded
import androidx.room.Relation

data class IrrigationSystemWithIrrigation(
    @Embedded
    val irrigationSystem: IrrigationSystem,
    @Relation(
        parentColumn = "id",
        entityColumn = "irrigationSystemId"
    )
    val irrigations: MutableList<Irrigation>
)
