package com.orchardlog.treedata.utils

import com.orchardlog.treedata.data.DateConverter
import com.orchardlog.treedata.entities.Irrigation

class SortIrrigations: Comparator<Irrigation> {
    override fun compare(o1: Irrigation?, o2: Irrigation?): Int {
        val d1 = DateConverter().toOffsetDateTime(o1.toString())
        val d2 = DateConverter().toOffsetDateTime(o2.toString())
        return d2!!.compareTo(d1)
    }
}