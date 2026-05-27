package com.orchardlog.treedata.shared.model

import androidx.room.Embedded
import androidx.room.Relation

data class PesticideApplicationWithItems(
    @Embedded
    val application: PesticideApplication,
    @Relation(
        parentColumn = "id",
        entityColumn = "pesticideApplicationId"
    )
    val items: List<PesticideApplicationItem>
)
