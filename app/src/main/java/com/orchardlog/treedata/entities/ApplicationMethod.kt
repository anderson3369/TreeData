package com.orchardlog.treedata.entities

enum class ApplicationMethod(val method: String) {
    GROUNDBOOM("Ground Boom"),
    AIRBLAST("Airblast"),
    CHEMIGATION("Chemigation"),
    AIR("Air"),
    FOGGER("Fogger"),
    HAND("Hand");

    override fun toString(): String {
        return method
    }

    companion object {
        fun from(search: String): ApplicationMethod =  requireNotNull(entries.find { it.method == search }) { "No Application Method with value $search" }
    }
}