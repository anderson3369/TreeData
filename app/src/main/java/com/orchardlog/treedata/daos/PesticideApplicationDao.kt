package com.orchardlog.treedata.daos

import androidx.room.*
import com.orchardlog.treedata.entities.PesticideApplication
import kotlinx.coroutines.flow.Flow

@Dao
interface PesticideApplicationDao {
    @Insert
    suspend fun insert(pesticideApplication: PesticideApplication): Long

    @Update
    fun update(pesticideApplication: PesticideApplication)

    @Delete
    fun delete(pesticideApplication: PesticideApplication)

    @Query("SELECT * FROM PesticideApplication")
    fun getPesticideApplications(): Flow<MutableList<PesticideApplication>>
}