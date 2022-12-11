package com.orchardmanager.treedata.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.orchardmanager.treedata.entities.OrchardAndSoil

@Dao
interface OrchardAndSoilDao {
    @Transaction
    @Query("SELECT * FROM Orchard")
    fun getOrchardAndSoil(): OrchardAndSoil
}