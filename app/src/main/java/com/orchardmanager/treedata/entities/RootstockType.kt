package com.orchardmanager.treedata.entities

enum class RootstockType(val type: String) {
    BAREROOT("Bareroot"),
    POTTED("Potted");

    override fun toString(): String {
        return type
    }

    companion object {
        fun from(search: String): RootstockType = requireNotNull(values().find { it.type == search }) { "No RootstockType with value $search" }
    }
}