package com.orchardmanager.treedata.entities

enum class RootstockType(val type: String) {
    BAREROOT("Bareroot"),
    POTTED("Potted");

    override fun toString(): String {
        return type
    }
}