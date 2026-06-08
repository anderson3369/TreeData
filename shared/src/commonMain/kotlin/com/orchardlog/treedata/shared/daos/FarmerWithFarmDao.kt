package com.orchardlog.treedata.shared.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.orchardlog.treedata.shared.model.FarmerWithFarm
import kotlinx.coroutines.flow.Flow

@Dao
interface FarmerWithFarmDao {
    @Transaction
    @Query("SELECT * FROM Farmer")
    fun getFarmerWithFarm(): Flow<List<FarmerWithFarm>>
}
