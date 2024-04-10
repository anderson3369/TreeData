package com.orchardlog.treedata.daos

import androidx.room.*
import com.orchardlog.treedata.entities.FertilizerApplication
import kotlinx.coroutines.flow.Flow

@Dao
interface FertilizerApplicationDao {
    @Insert
    suspend fun insert(fertilizerApplication: FertilizerApplication): Long

    @Update
    fun update(fertilizerApplication: FertilizerApplication)

    @Delete
    fun delete(fertilizerApplication: FertilizerApplication)

    @Query("SELECT * FROM FertilizerApplication")
    fun getFertilizerApplications(): Flow<MutableList<FertilizerApplication>>
}