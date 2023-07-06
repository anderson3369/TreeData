package com.orchardmanager.treedata.utils

import com.orchardmanager.treedata.data.DateConverter
import com.orchardmanager.treedata.entities.FertilizerApplication
import com.orchardmanager.treedata.entities.PesticideApplication

class SortPesticideApplications: Comparator<PesticideApplication> {
    override fun compare(o1: PesticideApplication?, o2: PesticideApplication?): Int {
        val d1 = DateConverter().toOffsetDateTime(o1.toString())
        val d2 = DateConverter().toOffsetDateTime(o2.toString())
        return d2!!.compareTo(d1)
    }
}