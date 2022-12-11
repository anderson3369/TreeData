package com.orchardmanager.treedata.daos

import androidx.room.*
import com.orchardmanager.treedata.entities.Soil

@Dao
interface SoilDao {
    @Insert
    fun insert(soil: Soil)

    @Update
    fun update(soil: Soil)

    @Delete
    fun delete(soil: Soil)

    @Query("SELECT * FROM Soil")
    fun getSoils(): List<Soil>
}