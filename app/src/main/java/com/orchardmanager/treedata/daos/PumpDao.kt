package com.orchardmanager.treedata.daos

import androidx.room.*
import com.orchardmanager.treedata.entities.Pump

@Dao
interface PumpDao {
    @Insert
    fun insert(pump: Pump)

    @Update
    fun update(pump: Pump)

    @Delete
    fun delete(pump: Pump)

    @Query("SELECT * FROM Pump")
    fun getPumps(): List<Pump>
}