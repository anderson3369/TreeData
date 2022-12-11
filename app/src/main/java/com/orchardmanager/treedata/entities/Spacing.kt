package com.orchardmanager.treedata.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Spacing(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val orchardId: Long,
    val rowWidth: Double,
    val rowWidthLinearUnit: LinearUnit,
    val spaceBetweenTrees: Double,
    val spaceBetweenTreesLinearUnit: LinearUnit
)
