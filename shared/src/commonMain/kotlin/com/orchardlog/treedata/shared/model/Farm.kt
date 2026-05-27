package com.orchardlog.treedata.shared.model


import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Entity(foreignKeys = [
    ForeignKey(entity = Farmer::class, parentColumns = ["id"], childColumns = ["farmerId"])],
    indices = [Index("farmerId"), Index(value = ["siteId"], unique = true)])
data class Farm(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val farmerId: Long = 0L,
    @SerialName("name")
    val name: String,
    @SerialName("siteId")
    val siteId: String,
    val persistentId: String, // UUID to track same farm across versions
    @Contextual
    val validFrom: Instant,
    @Contextual
    val validTo: Instant? = null
) {
    override fun toString():String = "$name  -  $siteId"
}
