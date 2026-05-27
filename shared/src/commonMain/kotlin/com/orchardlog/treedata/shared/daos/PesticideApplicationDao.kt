package com.orchardlog.treedata.shared.daos

import androidx.room.*
import com.orchardlog.treedata.shared.model.PesticideApplication
import com.orchardlog.treedata.shared.model.PesticideApplicationItem
import com.orchardlog.treedata.shared.model.PesticideApplicationWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface PesticideApplicationDao {
    @Insert
    suspend fun insert(pesticideApplication: PesticideApplication): Long

    @Update
    suspend fun update(pesticideApplication: PesticideApplication)

    @Delete
    suspend fun delete(pesticideApplication: PesticideApplication)

    @Query("DELETE FROM PesticideApplicationItem WHERE pesticideApplicationId = :applicationId")
    suspend fun deleteItemsByApplicationId(applicationId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<PesticideApplicationItem>)

    @Transaction
    suspend fun saveWithItems(application: PesticideApplication, items: List<PesticideApplicationItem>): Long {
        val id = insert(application)
        insertItems(items.map { it.copy(pesticideApplicationId = id) })
        return id
    }

    @Transaction
    suspend fun updateWithItems(application: PesticideApplication, items: List<PesticideApplicationItem>) {
        update(application)
        deleteItemsByApplicationId(application.id)
        insertItems(items.map { it.copy(pesticideApplicationId = application.id) })
    }

    @Transaction
    suspend fun deleteWithItems(application: PesticideApplication) {
        deleteItemsByApplicationId(application.id)
        delete(application)
    }

    @Query("SELECT * FROM PesticideApplication")
    fun getPesticideApplications(): Flow<List<PesticideApplication>>

    @Transaction
    @Query("SELECT * FROM PesticideApplication")
    fun getPesticideApplicationsWithItems(): Flow<List<PesticideApplicationWithItems>>
}
