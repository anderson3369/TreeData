package com.orchardmanager.treedata.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.orchardmanager.treedata.entities.PumpsWithIrrigationSystem
import kotlinx.coroutines.flow.Flow

@Dao
interface PumpWithIrrigationSystemDao {
    @Transaction
    @Query("SELECT * FROM Pump")
    fun getPumpWithIrrigationSystem(): Flow<MutableList<PumpsWithIrrigationSystem>>
}