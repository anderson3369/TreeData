package com.orchardmanager.treedata.daos

import androidx.room.*
import com.orchardmanager.treedata.entities.Spacing

@Dao
interface SpacingDao {
    @Insert
    fun insert(spacing: Spacing)

    @Update
    fun update(spacing: Spacing)

    @Delete
    fun delete(spacing: Spacing)

    @Query("SELECT * FROM Spacing")
    fun getSpacing(): List<Spacing>
}