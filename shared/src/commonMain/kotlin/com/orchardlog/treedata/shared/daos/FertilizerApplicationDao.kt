package com.orchardlog.treedata.shared.daos

import androidx.room.*
import com.orchardlog.treedata.shared.model.FertilizerApplication
import com.orchardlog.treedata.shared.model.FertilizerApplicationItem
import com.orchardlog.treedata.shared.model.FertilizerApplicationWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface FertilizerApplicationDao {
    @Insert
    suspend fun insert(fertilizerApplication: FertilizerApplication): Long

    @Update
    suspend fun update(fertilizerApplication: FertilizerApplication)

    @Delete
    suspend fun delete(fertilizerApplication: FertilizerApplication)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<FertilizerApplicationItem>)

    @Query("DELETE FROM FertilizerApplicationItem WHERE fertilizerApplicationId = :applicationId")
    suspend fun deleteItemsByApplicationId(applicationId: Long)

    @Transaction
    suspend fun saveWithItems(application: FertilizerApplication, items: List<FertilizerApplicationItem>): Long {
        val id = insert(application)
        insertItems(items.map { it.copy(fertilizerApplicationId = id) })
        return id
    }

    @Transaction
    suspend fun updateWithItems(application: FertilizerApplication, items: List<FertilizerApplicationItem>) {
        update(application)
        deleteItemsByApplicationId(application.id)
        insertItems(items.map { it.copy(fertilizerApplicationId = application.id) })
    }

    @Transaction
    suspend fun deleteWithItems(application: FertilizerApplication) {
        deleteItemsByApplicationId(application.id)
        delete(application)
    }

    @Query("SELECT * FROM FertilizerApplication")
    fun getFertilizerApplications(): Flow<List<FertilizerApplication>>

    @Transaction
    @Query("SELECT * FROM FertilizerApplication")
    fun getFertilizerApplicationsWithItems(): Flow<List<FertilizerApplicationWithItems>>
}
