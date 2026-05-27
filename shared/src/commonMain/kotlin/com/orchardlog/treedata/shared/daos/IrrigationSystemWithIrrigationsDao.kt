package com.orchardlog.treedata.shared.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.orchardlog.treedata.shared.model.IrrigationSystemWithIrrigation
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface IrrigationSystemWithIrrigationsDao {
    @Transaction
    @Query("SELECT * FROM IrrigationSystem WHERE orchardId = :orchardId")
    fun getIrrigationSystemWithIrrigation(orchardId: Long): Flow<List<IrrigationSystemWithIrrigation>>
}
