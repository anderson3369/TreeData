package com.orchardlog.treedata.shared.model

import androidx.room.Embedded
import androidx.room.Relation

data class PumpsWithIrrigationSystem(
    @Embedded
    val pump: Pump,
    @Relation(
        parentColumn = "id",
        entityColumn = "pumpId"
    )
    val irrigationSystems: List<IrrigationSystem>
)
