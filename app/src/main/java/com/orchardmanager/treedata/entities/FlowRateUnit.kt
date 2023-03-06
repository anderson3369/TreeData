package com.orchardmanager.treedata.entities

enum class FlowRateUnit(val unit: String) {
    GALLONSPERHOUR("Gallons per Hour"),
    GALLONSPERMINUTE("Gallons per Minute");

    override fun toString(): String {
        return unit
    }

    companion object {
        fun from(search: String): FlowRateUnit =  requireNotNull(values().find { it.unit == search }) { "No FlowRateUnit with value $search" }
    }
}