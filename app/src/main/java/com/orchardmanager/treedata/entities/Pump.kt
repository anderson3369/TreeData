package com.orchardmanager.treedata.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(foreignKeys = [
    ForeignKey(entity = IrrigationSystem::class, parentColumns = ["id"], childColumns = ["irrigationSystemId"])],
    indices = [Index("irrigationSystemId")]
)
data class Pump(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val irrigationSystemId: Long,
    val horsepower: Double,
    val phase: Int,
    val flowRate: Double,
    val irrigationUnit: IrrigationUnit
)
