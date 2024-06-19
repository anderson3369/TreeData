package com.orchardlog.treedata.entities

enum class OrchardUnit(val unit: String) {
    ACRE("Acre"),
    SQUAREFEET("Square Feet"),
    HECTARE("Hectare");

    override fun toString(): String {
        return unit
    }

    companion object {
        fun from(search: String): OrchardUnit = requireNotNull(
            entries.find { it.unit == search }) { "No OrchardUnit with vale $search" }
    }

}