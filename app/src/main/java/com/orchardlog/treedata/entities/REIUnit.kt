package com.orchardlog.treedata.entities

enum class REIUnit(val value: String) {
    HOUR("Hour"),
    DAY("Day");

    override fun toString(): String {
        return value
    }

    companion object {
        fun from(search: String): REIUnit =  requireNotNull(REIUnit.values().find { it.value == search }) { "No REI with value $search" }
    }
}