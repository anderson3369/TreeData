package com.orchardmanager.treedata.daos

import androidx.room.*
import com.orchardmanager.treedata.entities.FertilizerApplication

@Dao
interface FertilizerApplicationDao {
    @Insert
    fun insert(fertilizerApplication: FertilizerApplication)

    @Update
    fun update(fertilizerApplication: FertilizerApplication)

    @Delete
    fun delete(fertilizerApplication: FertilizerApplication)

    @Query("SELECT * FROM FertilizerApplication")
    fun getFertilizerApplications(): List<FertilizerApplication>
}