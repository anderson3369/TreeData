package com.orchardmanager.treedata.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface IrrigationSystemWithIrrigations {
    @Transaction
    @Query("SELECT * FROM IrrigationSystem")
    fun getIrrigationSystemWithIrrigations(): List<IrrigationSystemWithIrrigations?>?
}