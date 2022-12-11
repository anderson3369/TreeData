package com.orchardmanager.treedata.entities

import androidx.room.Embedded
import androidx.room.Relation

data class FarmWithOrchards(
    @Embedded
    val farm: Farm,
    @Relation(
        parentColumn = "id",
        entityColumn = "orchardId"
    )
    val orchards: List<Orchard>
)
