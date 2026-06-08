package com.orchardlog.treedata.shared.daos

import androidx.room.*
import com.orchardlog.treedata.shared.model.Farmer
import kotlinx.coroutines.flow.Flow

@Dao
interface FarmerDao {
    @Insert
    suspend fun insert(farmer: Farmer): Long

    @Update
    suspend fun update(farmer: Farmer)

    @Delete
    suspend fun delete(farmer: Farmer)

    @Query("SELECT * FROM Farmer")
    fun getFarmers(): Flow<List<Farmer>>

    // Convenience method for now
    @Query("SELECT id FROM Farmer LIMIT 1")
    fun getFarmerId(): Flow<Long>
}
