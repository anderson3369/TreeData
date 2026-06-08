package com.orchardlog.treedata.shared.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.orchardlog.treedata.shared.model.OrchardWithSoilTest

@Dao
interface OrchardWithSoilTestDao {
    @Transaction
    @Query("SELECT * FROM Orchard")
    suspend fun getOrchardWithSoilTest(): List<OrchardWithSoilTest>
}
