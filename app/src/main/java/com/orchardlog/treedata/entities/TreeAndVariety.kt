package com.orchardlog.treedata.entities

import androidx.room.Embedded
import androidx.room.Relation

data class TreeAndVariety(
    @Embedded
    val tree: Tree,
    @Relation(
        parentColumn = "id",
        entityColumn = "varietyId"
    )
    val variety: Variety
)
