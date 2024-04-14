package com.orchardlog.treedata.entities

enum class WeightOrMeasureUnit(val type: String) {
    POUNDS("Pounds"),
    TONS("Tons"),
    OUNCES("Ounces"),
    GRAMS("Grams"),
    GALLONS("Gallons"),
    QUARTS("Quarts"),
    PINTS("Pints"),
    FLUIDOUNCES("Fluid Ounces");

    override fun toString(): String {
        return type
    }

    companion object {
        fun from(search: String): WeightOrMeasureUnit =  requireNotNull(WeightOrMeasureUnit.values().find { it.type == search }) { "No WeightOrMeasureUnit with value $search" }
    }
}

