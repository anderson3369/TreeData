package com.orchardlog.treedata.shared.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class PesticideApplicationWithPesticides (
    @Embedded
    val pesticideApplication: PesticideApplication,
    @Relation(
        entity = Pesticide::class,
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = PesticideApplicationItem::class,
            parentColumn = "pesticideApplicationId",
            entityColumn = "pesticideId"
        )
    )
    val pesticides: List<Pesticide>,

    @Relation(
        parentColumn = "id",
        entityColumn = "pesticideApplicationId"
    )
    val items: List<PesticideApplicationItem>
)
