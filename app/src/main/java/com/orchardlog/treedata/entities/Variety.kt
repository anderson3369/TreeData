package com.orchardlog.treedata.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Variety(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val cultivar: String
) {
    override fun toString(): String {
        return name
    }
}
