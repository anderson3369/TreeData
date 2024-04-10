package com.orchardlog.treedata.entities

import androidx.room.Embedded
import androidx.room.Relation


class OrchardWithOrchardActivity(
    @Embedded
    val orchard: Orchard,
    @Relation(
        parentColumn = "id",
        entityColumn = "orchardId"
    )
    val orchardActivities: MutableList<OrchardActivity>
)