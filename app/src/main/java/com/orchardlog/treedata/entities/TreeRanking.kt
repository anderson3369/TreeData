package com.orchardlog.treedata.entities

enum class TreeRanking(val ranking: String) {
    EXCELLENT("Excellent"),
    GOOD("Good"),
    MODERATE("Moderate"),
    POOR("Poor"),
    DYING("Dying or Dead");

    override fun toString(): String {
        return ranking
    }

    companion object {
        fun from(search: String): TreeRanking =  requireNotNull(TreeRanking.values().find { it.ranking == search }) { "No Tree Ranking with value $search" }
    }
}