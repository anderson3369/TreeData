package com.orchardlog.treedata.entities

enum class IrrigationUnit(val unit: String) {
    GALLONS("Gallons"),
    ACREFEET("Acre Feet");

    override fun toString(): String {
        return unit
    }

    companion object {
        fun from(search: String): IrrigationUnit =  requireNotNull(IrrigationUnit.values().find { it.unit == search }) { "No IrrigationUnit with value $search" }
    }
}