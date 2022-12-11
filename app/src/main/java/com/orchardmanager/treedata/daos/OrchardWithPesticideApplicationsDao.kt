package com.orchardmanager.treedata.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.orchardmanager.treedata.entities.OrchardWithPesticideApplications

@Dao
interface OrchardWithPesticideApplicationsDao {
    @Transaction
    @Query("SELECT * FROM Orchard")
    fun getOrchardWithPesticideApplications(): List<OrchardWithPesticideApplications>
}