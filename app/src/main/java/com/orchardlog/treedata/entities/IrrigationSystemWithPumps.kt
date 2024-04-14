package com.orchardlog.treedata.entities

import androidx.room.Embedded
import androidx.room.Relation

data class PumpsWithIrrigationSystem(
    @Embedded
    val pump: Pump,
    @Relation(
        parentColumn = "id",
        entityColumn = "pumpId"
    )
    val pumps: MutableList<IrrigationSystem>
)
