package com.orchardlog.treedata.entities

import androidx.room.Embedded
import androidx.room.Relation

data class FarmWithOrchardsWithOrchardActivities(
    @Embedded
    val farm: Farm,
    @Relation(
        parentColumn = "id",
        entityColumn = "farmId"
    )
    val orchards: MutableList<Orchard>,
    @Relation(
        parentColumn = "id",
        entityColumn = "orchardId"
    )
    val orchardActivities: MutableList<OrchardActivity>
)
