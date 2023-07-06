package com.orchardmanager.treedata.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RoomWarnings
import androidx.room.Transaction
import com.orchardmanager.treedata.entities.IrrigationSystemWithIrrigation
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface IrrigationSystemWithIrrigationsDao {
    @Transaction
    @Query("SELECT * FROM IrrigationSystem IrrSys " +
            "JOIN Irrigation I ON IrrSys.id = I.irrigationSystemId " +
            "WHERE IrrSys.orchardId = :orchardId AND I.startTime BETWEEN :startTime AND :endTime")
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    fun getIrrigationSystemWithIrrigation(orchardId: Long, startTime: LocalDate, endTime: LocalDate):
            Flow<IrrigationSystemWithIrrigation>
}