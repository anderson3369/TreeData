package com.orchardmanager.treedata.daos

import androidx.room.*
import com.orchardmanager.treedata.entities.IrrigationSystem

@Dao
interface IrrigationSystemDao {
    @Insert
    fun insert(irrigationSystem: IrrigationSystem)

    @Update
    fun update(irrigationSystem: IrrigationSystem)

    @Delete
    fun delete(irrigationSystem: IrrigationSystem)

    @Query("SELECT * FROM IrrigationSystem")
    fun getIrrigationSystems(): List<IrrigationSystem>
}