package com.orchardmanager.treedata.daos

import androidx.room.*
import com.orchardmanager.treedata.entities.Irrigation

@Dao
interface IrrigationDao {
    @Insert
    fun insert(irrigation: Irrigation)

    @Update
    fun update(irrigation: Irrigation)

    @Delete
    fun delete(irrigation: Irrigation)

    @Query("SELECT * FROM Irrigation")
    fun getIrrigations(): List<Irrigation>
}