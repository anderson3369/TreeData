package com.orchardmanager.treedata.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.orchardmanager.treedata.data.EnumConverter

@Entity
@TypeConverters(EnumConverter::class)
data class Rootstock(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val cultivar: String,
    val rootstockType: RootstockType

) {
    override fun toString(): String {
        return name
    }
}
