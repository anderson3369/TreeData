package com.orchardmanager.treedata.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(foreignKeys = [
    ForeignKey(entity = Farmer::class, parentColumns = ["id"], childColumns = ["farmerId"])],
    indices = [Index("farmerId")])
data class Farm(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val farmerId: Long = 0L,
    val name: String,
    val siteId: String
) {
    override fun toString():String = name + "  -  " + siteId
}
