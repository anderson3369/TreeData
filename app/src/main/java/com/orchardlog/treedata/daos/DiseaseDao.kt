package com.orchardlog.treedata.daos

import androidx.room.*
import com.orchardlog.treedata.entities.Disease

@Dao
interface DiseaseDao {
    @Insert
    fun insert(disease: Disease)

    @Update
    fun update(disease: Disease)

    @Delete
    fun delete(disease: Disease)

    @Query("SELECT * FROM Disease")
    fun getDiseases(): List<Disease?>
}