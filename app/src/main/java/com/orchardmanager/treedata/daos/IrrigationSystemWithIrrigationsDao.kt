package com.orchardmanager.treedata.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.orchardmanager.treedata.entities.IrrigationSystemWithIrrigation
import kotlinx.coroutines.flow.Flow

@Dao
interface IrrigationSystemWithIrrigationsDao {
    @Transaction
    @Query("SELECT * FROM IrrigationSystem")
    fun getIrrigationSystemWithIrrigation(): Flow<MutableList<IrrigationSystemWithIrrigation>>
}