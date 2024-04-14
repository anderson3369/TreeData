package com.orchardlog.treedata.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.orchardlog.treedata.entities.OrchardWithFertilizerApplications

@Dao
interface OrchardWithFertilizerApplicationsDao {
    @Transaction
    @Query("SELECT * FROM Orchard")
    fun getOrchardWithFertilizerApplications(): List<OrchardWithFertilizerApplications>
}