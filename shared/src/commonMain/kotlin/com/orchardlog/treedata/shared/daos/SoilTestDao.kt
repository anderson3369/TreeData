package com.orchardlog.treedata.shared.daos

import androidx.room.*
import com.orchardlog.treedata.shared.model.SoilTest

@Dao
interface SoilTestDao {
    @Insert
    suspend fun insert(soilTest: SoilTest)

    @Update
    suspend fun update(soilTest: SoilTest)

    @Delete
    suspend fun delete(soilTest: SoilTest)

    @Query("SELECT * FROM SoilTest")
    suspend fun getSoilTests(): List<SoilTest>
}
