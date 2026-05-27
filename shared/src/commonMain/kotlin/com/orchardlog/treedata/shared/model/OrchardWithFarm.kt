package com.orchardlog.treedata.shared.model

import androidx.room.Embedded
import androidx.room.Relation

data class OrchardWithFarm(
    @Embedded
    val orchard: Orchard,
    @Relation(
        parentColumn = "farmId",
        entityColumn = "id"
    )
    val farm: Farm
) {
    fun description(): String {
        return "${farm.siteId} - ${orchard.crop}"
    }

    override fun toString(): String = "${farm.siteId} and ${orchard.crop}"
}
