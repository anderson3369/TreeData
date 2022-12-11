package com.orchardmanager.treedata.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["fertilizerType", "percent"], unique = true)])
data class Fertilizer(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String,
    val fertilizerType: FertilizerType,
    val percent: Double
)
