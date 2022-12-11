package com.orchardmanager.treedata.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.orchardmanager.treedata.entities.FarmerWithFarm

@Dao
interface FarmerWithFarmDao {
    @Transaction
    @Query("SELECT * FROM Farmer")
    fun getFarmerWithFarm(): List<FarmerWithFarm>
}