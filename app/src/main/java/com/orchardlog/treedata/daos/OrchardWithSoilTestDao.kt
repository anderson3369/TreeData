package com.orchardlog.treedata.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.orchardlog.treedata.entities.OrchardWithSoilTest

@Dao
interface OrchardWithSoilTestDao {
    @Transaction
    @Query("SELECT * FROM Orchard")
    fun getOrchardWithSoilTest(): List<OrchardWithSoilTest>
}