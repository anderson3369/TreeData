package com.orchardmanager.treedata.entities

import androidx.room.Embedded
import androidx.room.Relation


data class IrrigationWithWithPump(
    @Embedded
    val irrigation: IrrigationSystem,
    @Relation(
        parentColumn = "id",
        entityColumn = "irrigationSystemId"
    )
    val irrigationSystem: Irrigation,
    @Relation(
        parentColumn = "pumpId",
        entityColumn = "id"
    )
    val pump: Pump

)
