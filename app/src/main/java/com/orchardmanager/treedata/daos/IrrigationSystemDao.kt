package com.orchardmanager.treedata.daos

import androidx.room.*
import com.orchardmanager.treedata.entities.IrrigationSystem
import kotlinx.coroutines.flow.Flow

@Dao
interface IrrigationSystemDao {
    @Insert
    suspend fun insert(irrigationSystem: IrrigationSystem): Long

    @Update
    fun update(irrigationSystem: IrrigationSystem)

    @Delete
    fun delete(irrigationSystem: IrrigationSystem)

    @Query("SELECT * FROM IrrigationSystem")
    fun getIrrigationSystems(): Flow<MutableList<IrrigationSystem>>
}