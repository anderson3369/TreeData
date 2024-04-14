package com.orchardlog.treedata.utils

import com.orchardlog.treedata.data.DateConverter
import com.orchardlog.treedata.entities.OrchardActivity

class SortOrchardActivity: Comparator<OrchardActivity> {
    override fun compare(o1: OrchardActivity?, o2: OrchardActivity?): Int {
        val d1 = DateConverter().toOffsetDateTime(o1!!.activityStart.toString())
        val d2 = DateConverter().toOffsetDateTime(o2!!.activityStop.toString())
        return d2!!.compareTo(d1)
    }
}