package com.orchardmanager.treedata.entities

import androidx.room.Embedded
import androidx.room.Relation

data class OrchardAndSpacing(
    @Embedded
    val orchard: Orchard,
    @Relation(
        parentColumn = "id",
        entityColumn = "spacingId"
    )
    val spacing: Spacing
)
