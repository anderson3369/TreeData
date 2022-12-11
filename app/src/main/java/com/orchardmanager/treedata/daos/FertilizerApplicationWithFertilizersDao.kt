package com.orchardmanager.treedata.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.orchardmanager.treedata.entities.FertilizerApplication

@Dao
interface FertilizerApplicationWithFertilizersDao {
    @Transaction
    @Query("SELECT * FROM FertilizerApplication")
    fun getFertilizerApplicationWithFertilizers(): List<FertilizerApplication>
}