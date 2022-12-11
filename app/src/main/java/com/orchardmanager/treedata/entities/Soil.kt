package com.orchardmanager.treedata.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Soil(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val orchardId: Long,
    val sand: Double,
    val silt: Double,
    val clay: Double,
    val organicMatter: Double
)
