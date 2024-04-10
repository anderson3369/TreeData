package com.orchardlog.treedata.utils

import com.orchardlog.treedata.data.DateConverter
import com.orchardlog.treedata.entities.PesticideApplication

class SortPesticideApplications: Comparator<PesticideApplication> {
    override fun compare(o1: PesticideApplication?, o2: PesticideApplication?): Int {
        val d1 = DateConverter().toOffsetDateTime(o1.toString())
        val d2 = DateConverter().toOffsetDateTime(o2.toString())
        return d2!!.compareTo(d1)
    }
}