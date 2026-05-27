package com.orchardlog.treedata.shared.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.orchardlog.treedata.shared.model.PumpsWithIrrigationSystem
import kotlinx.coroutines.flow.Flow

@Dao
interface PumpWithIrrigationSystemDao {
    @Transaction
    @Query("SELECT * FROM Pump WHERE id IN (SELECT pumpId FROM IrrigationSystem WHERE orchardId = :orchardId)")
    fun getPumpWithIrrigationSystem(orchardId: Long): Flow<List<PumpsWithIrrigationSystem>>
}
