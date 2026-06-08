package com.orchardlog.treedata.shared.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.orchardlog.treedata.shared.database.EnumConverter

@Entity
@TypeConverters(EnumConverter::class)
data class IrrigationSystem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val orchardId: Long,
    val pumpId: Long,
    val name: String,
    val irrigationMethod: IrrigationMethod,
    val emitterFlowRate: Double,
    val emitterFlowUnit: FlowRateUnit,
    val emitterRadius: Double,
    val emitterRadiusLinearUnit: LinearUnit,
    val emitterSpacing: Double,
    val emitterSpacingLinearUnit: LinearUnit,
    val firestoreId: String = ""
) {
    override fun toString(): String {
        return name
    }
}
