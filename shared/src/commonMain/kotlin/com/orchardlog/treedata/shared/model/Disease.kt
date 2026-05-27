package com.orchardlog.treedata.shared.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Entity
data class Disease(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val treeId: Long,
    @SerialName("name")
    val name: String,
    @SerialName("scientific_name")
    val scientificName: String,
    @SerialName("description")
    val description: String,
    val diseaseType: DiseaseType,
    val firestoreId: String = ""
)
