package com.orchardlog.treedata.shared.daos

import androidx.room.*
import com.orchardlog.treedata.shared.model.SoilMoisture
import kotlinx.coroutines.flow.Flow

@Dao
interface SoilMoistureDao {

    @Insert
    suspend fun insert(soilMoisture: SoilMoisture): Long

    @Update
    suspend fun update(soilMoisture: SoilMoisture)

    @Delete
    suspend fun delete(soilMoisture: SoilMoisture)

    @Query("SELECT * FROM SoilMoisture")
    fun getSoilMoisture(): Flow<List<SoilMoisture>>
}
