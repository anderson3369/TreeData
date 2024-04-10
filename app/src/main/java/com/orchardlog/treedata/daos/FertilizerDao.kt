package com.orchardlog.treedata.daos

import androidx.room.*
import com.orchardlog.treedata.entities.Fertilizer
import kotlinx.coroutines.flow.Flow

@Dao
interface FertilizerDao {
    @Insert
    suspend fun insert(fertilizer: Fertilizer): Long

    @Update
    fun update(fertilizer: Fertilizer)

    @Delete
    fun delete(fertilizer: Fertilizer)

    @Query("SELECT * FROM Fertilizer")
    fun getFertilizers(): Flow<MutableList<Fertilizer>>
}