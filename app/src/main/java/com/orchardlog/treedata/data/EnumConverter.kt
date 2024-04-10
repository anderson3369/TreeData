package com.orchardlog.treedata.data

import androidx.room.TypeConverter
import com.orchardlog.treedata.entities.*

class EnumConverter {

    @TypeConverter
    fun toRootstockType(value: String) = RootstockType.from(value)

    @TypeConverter
    fun fromRootstockType(value: RootstockType) = value.type

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

    @TypeConverter
    fun toSignalWord(value: String) = SignalWord.from(value)

    @TypeConverter
    fun fromSignalWord(value: SignalWord) = value.value

    @TypeConverter
    fun toREI(value: String) = REIUnit.from(value)

    @TypeConverter
    fun fromREI(value: REIUnit) = value.value

    @TypeConverter
    fun toApplicationMethod(value: String) = ApplicationMethod.from(value)

    @TypeConverter
    fun fromApplicationMethod(value: ApplicationMethod) = value.method

    @TypeConverter
    fun toTreeRanking(value: String) = TreeRanking.from(value)

    @TypeConverter
    fun fromTreeRanking(value: TreeRanking) = value.ranking
}