package com.orchardmanager.treedata.entities

import androidx.room.*
import com.orchardmanager.treedata.data.DateConverter
import java.time.LocalDateTime
@Fts4
@Entity
@TypeConverters(DateConverter::class)
data class Tree(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="rowid")
    val id: Long,
    val orchardId: Long,
    val plantingDate: LocalDateTime
)
