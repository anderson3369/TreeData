package com.orchardmanager.treedata.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Variety(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val treeId: Long,
    val name: String,
    val cultivar: String
)
