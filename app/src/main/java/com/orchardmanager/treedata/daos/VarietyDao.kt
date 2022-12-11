package com.orchardmanager.treedata.daos

import androidx.room.*
import com.orchardmanager.treedata.entities.Variety

@Dao
interface VarietyDao {
    @Insert
    fun insert(variety: Variety)

    @Update
    fun update(variety: Variety)

    @Delete
    fun delete(variety: Variety)

    @Query("SELECT * FROM Variety")
    fun getVarieties(): List<Variety>
}