package com.orchardlog.treedata.shared.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.orchardlog.treedata.shared.database.EnumConverter

@TypeConverters(EnumConverter::class)
@Entity
data class Pesticide(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val productName: String,
    val eparegno: String,
    val signalWord: SignalWord,
    val rei: Int,
    val reiUnit: REIUnit,
    val firestoreId: String = ""
) {
    override fun toString(): String {
        return productName
    }
}
