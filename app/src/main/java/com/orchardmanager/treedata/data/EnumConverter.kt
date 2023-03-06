package com.orchardmanager.treedata.data

import androidx.room.TypeConverter
import com.orchardmanager.treedata.entities.*

class EnumConverter {

    @TypeConverter
    fun toRootstockType(value: String) = RootstockType.from(value)

    @TypeConverter
    fun fromRootstocktype(value: RootstockType) = value.name

    @TypeConverter
    fun toIrrigationMethod(value: String) = IrrigationMethod.from(value)

    @TypeConverter
    fun fromIrrigationMethod(value: IrrigationMethod) = value.method

    @TypeConverter
    fun toFlowRateUnit(value: String) = FlowRateUnit.from(value)

    @TypeConverter
    fun fromFlowRateUnit(value: FlowRateUnit) = value.unit

    @TypeConverter
    fun toLinearUnit(value: String) = LinearUnit.from(value)

    @TypeConverter
    fun fromLinearUnit(value: LinearUnit) = value.unit

    @TypeConverter
    fun toWeightOrMeasureUnit(value: String) = WeightOrMeasureUnit.from(value)

    @TypeConverter
    fun fromWeightOrMeasureUnit(value: WeightOrMeasureUnit) = value.type
}