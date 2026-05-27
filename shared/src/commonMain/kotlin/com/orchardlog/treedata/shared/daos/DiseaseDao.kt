package com.orchardlog.treedata.shared.daos

import androidx.room.*
import com.orchardlog.treedata.shared.model.Disease

@Dao
interface DiseaseDao {
    @Insert
    suspend fun insert(disease: Disease)

    @Update
    suspend fun update(disease: Disease)

    @Delete
    suspend fun delete(disease: Disease)

    @Query("SELECT * FROM Disease")
    suspend fun getDiseases(): List<Disease>
}
