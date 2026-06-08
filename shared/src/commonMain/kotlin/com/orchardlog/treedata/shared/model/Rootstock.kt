package com.orchardlog.treedata.shared.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.orchardlog.treedata.shared.database.EnumConverter

@Entity
@TypeConverters(EnumConverter::class)
data class Rootstock(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val cultivar: String,
    val rootstockType: RootstockType,
    val firestoreId: String = ""

) {
    override fun toString(): String {
        return name
    }
}
