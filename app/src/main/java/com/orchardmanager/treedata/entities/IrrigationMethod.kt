package com.orchardmanager.treedata.entities

enum class IrrigationMethod(val method: String) {
    SPRINKLER("Sprinkler"),
    MICROSPRINKLER("Micro Sprinkler"),
    DRIP("Drip"),
    FLOOD("Flood");

    override fun toString(): String {
        return method
    }

    companion object {
        fun from(search: String): IrrigationMethod =  requireNotNull(values().find { it.method == search }) { "No IrrigationMethod with value $search" }
    }
}