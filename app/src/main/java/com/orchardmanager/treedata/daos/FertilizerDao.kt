package com.orchardmanager.treedata.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import com.orchardmanager.treedata.entities.Fertilizer
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