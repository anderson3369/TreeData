package com.orchardmanager.treedata.entities

import androidx.room.Embedded
import androidx.room.Relation

data class FarmerWithFarm(
    @Embedded
    val farmer: Farmer,
    @Relation(
        parentColumn = "id",
        entityColumn = "farmerId"
    )
    val farms: List<Farm>

)
