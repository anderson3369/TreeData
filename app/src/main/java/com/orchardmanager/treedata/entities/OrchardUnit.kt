package com.orchardmanager.treedata.entities

enum class OrchardUnit(val unit: String) {
    ACRE("Acre"),
    HECTARE("Hectare");

    override fun toString(): String {
        return unit
    }

    companion object {
        fun from(search: String): OrchardUnit = requireNotNull(
            OrchardUnit.values().find { it.unit == search }) { "No OrchardUnit with vale $search" }
    }

}