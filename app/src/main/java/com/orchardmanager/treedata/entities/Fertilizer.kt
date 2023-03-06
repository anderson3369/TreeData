package com.orchardmanager.treedata.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity
data class Fertilizer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val nitrogen: Double,
    val phosphorous: Double,
    val potassium: Double,
    val sulfur: Double,
    val calcium: Double,
    val magnesium: Double,
    val boron: Double,
    val zinc: Double,
    val iron: Double,
    val manganese: Double,
    val molybdenum: Double,
    val chloride: Double,
    val copper: Double,
    val nickel: Double
)
