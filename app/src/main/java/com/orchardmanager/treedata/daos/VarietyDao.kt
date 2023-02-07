package com.orchardmanager.treedata.daos

import androidx.room.*
import com.orchardmanager.treedata.entities.Variety
import kotlinx.coroutines.flow.Flow

@Dao
interface VarietyDao {
    @Insert
    suspend fun insert(variety: Variety): Long

    @Update
    fun update(variety: Variety)

    @Delete
    fun delete(variety: Variety)

    @Query("SELECT * FROM Variety")
    fun getVarieties(): Flow<MutableList<Variety>>
}