package com.orchardlog.treedata.entities

enum class SignalWord(val value: String) {
    DANGER("Danger"),
    WARNING("Warning"),
    CAUTION("Caution");

    override fun toString(): String {
        return value
    }

    companion object {
        fun from(search: String): SignalWord =  requireNotNull(entries.find { it.value == search }) { "No Signal Word with value $search" }
    }
}