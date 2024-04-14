package com.orchardlog.treedata.entities

import androidx.room.Embedded
import androidx.room.Relation

class PesticideApplicationWithPesticides (
    @Embedded
    val pesticideApplication: PesticideApplication,
    @Relation(
        parentColumn = "pesticideId",
        entityColumn = "id"
    )
    val pesticides: MutableList<Pesticide>
)



