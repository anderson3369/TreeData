package com.orchardlog.treedata.shared.model

enum class SoilUnit(val units: String) {
    PPM("ppm"),
    MEQ("milliequivalents");

    companion object {
        fun from(search: String): SoilUnit = requireNotNull(
            entries.find { it.units == search }) { "No SoilUnit with value $search" }
    }
}