package com.orchardmanager.treedata.daos

import androidx.room.*
import com.orchardmanager.treedata.entities.PesticideApplication

@Dao
interface PesticideApplicationDao {
    @Insert
    fun insert(pesticideApplication: PesticideApplication)

    @Update
    fun update(pesticideApplication: PesticideApplication)

    @Delete
    fun delete(pesticideApplication: PesticideApplication)

    @Query("SELECT * FROM PesticideApplication")
    fun getPesticideApplications(): List<PesticideApplication>
}