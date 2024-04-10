package com.orchardlog.treedata.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.orchardlog.treedata.data.EnumConverter

@Entity
@TypeConverters(EnumConverter::class)
data class Pump(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val type: String,
    val horsepower: Double,
    val phase: Int,
    val flowRate: Double,
    val flowRateUnit: FlowRateUnit

) {
    override fun toString(): String {
        return type
    }
}
