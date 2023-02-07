package com.orchardmanager.treedata.data

import androidx.room.TypeConverter
import com.orchardmanager.treedata.entities.RootstockType

class EnumConverter {

    @TypeConverter
    fun toRootstockType(value: String) = enumValueOf<RootstockType>(value)

    @TypeConverter
    fun fromRootstocktype(value: RootstockType) = value.name
}