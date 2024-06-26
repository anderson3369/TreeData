package com.orchardlog.treedata.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.orchardlog.treedata.entities.OrchardAndIrrigationSystem

@Dao
interface OrcahardAndIrrigationSystemDao {
    @Transaction
    @Query("SELECT * FROM Orchard")
    fun getOrchardAndIrrigationSystem(): OrchardAndIrrigationSystem

    @Transaction
    @Query("SELECT * FROM Orchard WHERE id = :id")
    fun getOrchardAndIrrigationSystem(id: Long): OrchardAndIrrigationSystem
}