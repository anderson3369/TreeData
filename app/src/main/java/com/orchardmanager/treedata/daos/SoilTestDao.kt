package com.orchardmanager.treedata.daos

import androidx.room.*
import com.orchardmanager.treedata.entities.SoilTest

@Dao
interface SoilTestDao {
    @Insert
    fun insert(soilTest: SoilTest)

    @Update
    fun update(soilTest: SoilTest)

    @Delete
    fun delete(soilTest: SoilTest)

    @Query("SELECT * FROM SoilTest")
    fun getSoilTests(): List<SoilTest>
}