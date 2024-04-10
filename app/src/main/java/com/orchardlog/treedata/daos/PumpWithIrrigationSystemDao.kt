package com.orchardlog.treedata.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RoomWarnings
import androidx.room.Transaction
import com.orchardlog.treedata.entities.PumpsWithIrrigationSystem
import kotlinx.coroutines.flow.Flow

@Dao
interface PumpWithIrrigationSystemDao {
    @Transaction
    @Query("SELECT * FROM IrrigationSystem IrrSys " +
            "JOIN Pump P ON IrrSys.pumpId=P.id " +
            "WHERE IrrSys.orchardId = :orchardId ")
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    fun getPumpWithIrrigationSystem(orchardId: Long): Flow<PumpsWithIrrigationSystem>
}