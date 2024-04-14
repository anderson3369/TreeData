package com.orchardlog.treedata.utils

import com.orchardlog.treedata.data.DateConverter
import com.orchardlog.treedata.entities.FertilizerApplication

class SortFertilizerApplications: Comparator<FertilizerApplication> {
    override fun compare(o1: FertilizerApplication?, o2: FertilizerApplication?): Int {
        val d1 = DateConverter().toOffsetDateTime(o1.toString())
        val d2 = DateConverter().toOffsetDateTime(o2.toString())
        return d2!!.compareTo(d1)
    }
}