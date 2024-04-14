package com.orchardlog.treedata.entities

import androidx.room.Embedded
import androidx.room.Relation

data class FarmWithOrchards(
    @Embedded
    val farm: Farm,
    @Relation(
        parentColumn = "id",
        entityColumn = "farmId"
    )
    val orchards: MutableList<Orchard>
)


