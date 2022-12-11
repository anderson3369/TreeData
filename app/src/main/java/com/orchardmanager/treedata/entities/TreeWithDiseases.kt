package com.orchardmanager.treedata.entities

import androidx.room.Embedded
import androidx.room.Relation

data class TreeWithDiseases(
    @Embedded
    val tree: Tree,
    @Relation(
        parentColumn = "id",
        entityColumn = "diseaseId"
    )
    val diseases: List<Disease>
)
