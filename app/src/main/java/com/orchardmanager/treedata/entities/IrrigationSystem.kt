package com.orchardmanager.treedata.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class IrrigationSystem(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val orchardId: Long,
    val irrigationMethod: IrrigationMethod,
    val irrigationRadius: Double,
    val irrigationRadiusLinearUnit: LinearUnit,
    val dripSpacing: Double,
    val dripSpacingLinearUnit: LinearUnit,
    val emitterFlowRate: Double,
    val irrigationUnit: IrrigationUnit
)
