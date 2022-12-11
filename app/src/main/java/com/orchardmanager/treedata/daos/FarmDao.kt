package com.orchardmanager.treedata.daos

import androidx.room.*
import com.orchardmanager.treedata.entities.Farm
import kotlinx.coroutines.flow.Flow

@Dao
interface FarmDao {
    @Insert
    suspend fun insert(farm: Farm):Long

    @Update
    fun update(farm: Farm)

    @Delete
    fun delete(farm: Farm)

    @Query("SELECT * FROM Farm WHERE farmerId = :farmerId")
    fun getOrchardLocations(farmerId:Long): Flow<MutableList<Farm>>
}