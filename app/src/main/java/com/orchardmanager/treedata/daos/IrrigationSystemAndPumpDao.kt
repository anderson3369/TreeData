package com.orchardmanager.treedata.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.orchardmanager.treedata.entities.IrrigationSystem

@Dao
interface IrrigationSystemAndPumpDao {
    @Transaction
    @Query("SELECT * FROM IrrigationSystem")
    //Todo query needs improvement
    fun getIrrigationSystem(): IrrigationSystem
}