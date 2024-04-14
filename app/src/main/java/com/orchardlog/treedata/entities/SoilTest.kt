package com.orchardlog.treedata.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.orchardlog.treedata.data.DateConverter
import java.time.LocalDate

@Entity
@TypeConverters(DateConverter::class)
data class SoilTest(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val orchardId: Long,
    val testDate: LocalDate,
    val nitrate: Double,
    val nitrateUnit: SoilUnit,
    val phosphorousBray: Double,
    val phosphorousBrayUnit: SoilUnit,
    val phosphorousNaHCO3: Double,
    val phosphorousNaHCO3Unit: SoilUnit,
    val potassium: Double,
    val potassiumUnit: SoilUnit,
    val calcium: Double,
    val calciumUnit: SoilUnit,
    val magnesium: Double,
    val magnesiumUnit: SoilUnit,
    val sodium: Double,
    val sodiumUnit: SoilUnit,
    val sulfur: Double,
    val sulfurUnit: SoilUnit,
    val zinc: Double,
    val zincUnit: SoilUnit,
    val manganese: Double,
    val manganeseUnit: SoilUnit,
    val iron: Double,
    val ironUnit: SoilUnit,
    val copper: Double,
    val copperUnit: SoilUnit,
    val boron: Double,
    val boronUnit: SoilUnit,
    var chloride: Double,
    val chlorideUnit: SoilUnit,
    val potassiumCation: Double,
    val magnesiumCation: Double,
    val calciumCation: Double,
    val sodiumCation: Double

)
