package com.orchardlog.treedata.shared.daos

import androidx.room.*
import com.orchardlog.treedata.shared.model.IrrigationSystem
import kotlinx.coroutines.flow.Flow

@Dao
interface IrrigationSystemDao {
    @Insert
    suspend fun insert(irrigationSystem: IrrigationSystem): Long

    @Update
    suspend fun update(irrigationSystem: IrrigationSystem)

    @Delete
    suspend fun delete(irrigationSystem: IrrigationSystem)

    @Query("SELECT * FROM IrrigationSystem")
    fun getIrrigationSystems(): Flow<List<IrrigationSystem>>
}
