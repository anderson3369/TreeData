package com.orchardlog.treedata.utils

import com.orchardlog.treedata.data.DateConverter

class SortDates : Comparator<String> {
    override fun compare(o1: String?, o2: String?): Int {
        val d1 = DateConverter().toOffsetDateTime(o1)
        val d2 = DateConverter().toOffsetDateTime(o2)
        return d2!!.compareTo(d1)
    }
}