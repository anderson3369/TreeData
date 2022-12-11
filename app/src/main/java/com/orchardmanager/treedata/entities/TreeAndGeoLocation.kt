package com.orchardmanager.treedata.entities

import androidx.room.Embedded
import androidx.room.Relation

data class TreeAndGeoLocation(
    @Embedded
    val tree: Tree,
    @Relation(
        parentColumn = "id",
        entityColumn = "geoLocationId"
    )
    val geoLocation: GeoLocation
)
