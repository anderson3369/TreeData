package com.orchardlog.treedata.shared.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.orchardlog.treedata.shared.model.OrchardAndIrrigationSystem

@Dao
interface OrchardAndIrrigationSystemDao {
    @Transaction
    @Query("SELECT * FROM Orchard")
    suspend fun getOrchardAndIrrigationSystem(): List<OrchardAndIrrigationSystem>

    @Transaction
    @Query("SELECT * FROM Orchard WHERE id = :orchardId")
    suspend fun getOrchardAndIrrigationSystem(orchardId: Long): OrchardAndIrrigationSystem?
}
