package com.orchardmanager.treedata.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Relation

@Entity
data class OrchardWithTrees(
    @Embedded
    val orchard: Orchard,
    @Relation(
        parentColumn = "id",
        entityColumn = "orchardId"
    )
    val trees: MutableList<Tree>
)
