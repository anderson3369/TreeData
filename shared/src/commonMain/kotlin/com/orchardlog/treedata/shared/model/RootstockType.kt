package com.orchardlog.treedata.shared.model

enum class RootstockType(val type: String) {
    BAREROOT("Bareroot"),
    POTTED("Potted");

    override fun toString(): String {
        return type
    }

    companion object {
        fun from(search: String): RootstockType = requireNotNull(entries.find { it.type == search }) { "No RootstockType with value $search" }
    }
}