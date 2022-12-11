package com.orchardmanager.treedata.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey
@Fts4
@Entity
data class GeoLocation(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name ="rowid")
    val id: Long,
    val treeId: Long,
    val latitude: Double,
    val longitude: Double
)
