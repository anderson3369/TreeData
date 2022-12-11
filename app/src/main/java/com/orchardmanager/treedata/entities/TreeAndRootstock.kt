package com.orchardmanager.treedata.entities

import androidx.room.Embedded
import androidx.room.Relation

data class TreeAndRootstock(
    @Embedded
    val tree: Tree,
    @Relation(
        parentColumn = "id",
        entityColumn = "rootstockId"
    )
    val rootstock: Rootstock
)
