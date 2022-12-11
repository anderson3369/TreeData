package com.orchardmanager.treedata.entities

import androidx.room.Embedded
import androidx.room.Relation

data class OrchardAndIrrigationSystem(
    @Embedded
    val orchard: Orchard?,
    @Relation(
        parentColumn = "id",
        entityColumn = "orchardId"
    )
    val irrigationSystem: IrrigationSystem
)
