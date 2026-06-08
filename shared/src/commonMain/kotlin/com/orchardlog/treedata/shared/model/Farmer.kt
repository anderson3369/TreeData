package com.orchardlog.treedata.shared.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/*
Technically you could have multiple Farmers
managing multiple locations, but for now
we don't
 */
@Serializable
@Entity
data class Farmer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @SerialName("name")
    val name: String,
    @SerialName("address")
    val address: String,
    @SerialName("city")
    val city: String,
    @SerialName("state")
    val state: String,
    @SerialName("zip")
    val zip: String,
    @SerialName("phone")
    val phone: String,
    @SerialName("email")
    val email: String,
    val persistentId: String = ""
)
