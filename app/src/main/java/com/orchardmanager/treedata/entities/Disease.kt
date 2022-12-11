package com.orchardmanager.treedata.entities

import androidx.room.*


@Entity
data class Disease(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val treeId: Long,
    val name: String,
    val scientificName: String,
    val description: String,
    val diseaseType: DiseaseType
)
