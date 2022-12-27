package com.orchardmanager.treedata.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.orchardmanager.treedata.entities.FarmerWithFarm
import kotlinx.coroutines.flow.Flow

@Dao
interface FarmerWithFarmDao {
    @Transaction
    @Query("SELECT * FROM Farmer")
    fun getFarmerWithFarm(): Flow<MutableList<FarmerWithFarm>>
}