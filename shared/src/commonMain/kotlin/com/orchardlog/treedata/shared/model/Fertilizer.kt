package com.orchardlog.treedata.shared.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Entity
data class Fertilizer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @SerialName("name")
    val name: String,
    @SerialName("nitrogen")
    val nitrogen: Double,
    @SerialName("phosphorous")
    val phosphorous: Double,
    @SerialName("potassium")
    val potassium: Double,
    @SerialName("sulfur")
    val sulfur: Double,
    @SerialName("calcium")
    val calcium: Double,
    @SerialName("magnesium")
    val magnesium: Double,
    @SerialName("iron")
    val iron: Double,
    @SerialName("zinc")
    val zinc: Double,
    @SerialName("manganese")
    val manganese: Double,
    @SerialName("boron")
    val boron: Double,
    @SerialName("molybdenum")
    val molybdenum: Double,
    @SerialName("chloride")
    val chloride: Double,
    @SerialName("copper")
    val copper: Double,
    @SerialName("selenium")
    val selenium: Double,
    @SerialName("nickel")
    val nickel: Double,
    @SerialName("organicMatter")
    val organicMatter: Double,
    val firestoreId: String = ""
) {
    override fun toString(): String {
        return name
    }
}
