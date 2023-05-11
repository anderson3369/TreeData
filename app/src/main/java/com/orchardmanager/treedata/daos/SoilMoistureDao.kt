package com.orchardmanager.treedata.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.orchardmanager.treedata.entities.SoilMoisture
import kotlinx.coroutines.flow.Flow

@Dao
interface SoilMoistureDao {

    @Insert
    suspend fun insert(soilMoisture: SoilMoisture): Long

    @Update
    fun update(soilMoisture: SoilMoisture)

    @Delete
    fun delete(soilMoisture: SoilMoisture)

    @Query("SELECT * FROM SoilMoisture")
    fun getSoilMoisture(): Flow<MutableList<SoilMoisture>>
}