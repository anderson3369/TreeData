package com.orchardmanager.treedata.entities

enum class LinearUnit(val unit: String) {
    FEET("Feet"),
    INCHES("Inches"),
    METERS("Meters");

    override fun toString(): String {
        return unit
    }

    companion object {
        fun from(search: String): LinearUnit =  requireNotNull(LinearUnit.values().find { it.unit == search }) { "No LinearUnit with value $search" }
    }
}