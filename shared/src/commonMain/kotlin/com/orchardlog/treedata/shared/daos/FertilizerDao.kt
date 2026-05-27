package com.orchardlog.treedata.shared.daos

import androidx.room.*
import com.orchardlog.treedata.shared.model.Fertilizer
import kotlinx.coroutines.flow.Flow

@Dao
interface FertilizerDao {
    @Insert
    suspend fun insert(fertilizer: Fertilizer): Long

    @Update
    suspend fun update(fertilizer: Fertilizer)

    @Delete
    suspend fun delete(fertilizer: Fertilizer)

    @Query("SELECT * FROM Fertilizer")
    fun getFertilizers(): Flow<List<Fertilizer>>
}
