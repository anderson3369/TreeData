package com.orchardmanager.treedata.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
/*
Technically you could have multiple Farmers
managing multiple locations, but for now
we don't
 */
@Entity
data class Farmer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val address: String,
    val city: String,
    val state: String,
    val zip: String,
    val phone: String,
    val email: String
)
