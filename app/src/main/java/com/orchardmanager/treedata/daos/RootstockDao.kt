package com.orchardmanager.treedata.daos

import androidx.room.*
import com.orchardmanager.treedata.entities.Rootstock

@Dao
interface RootstockDao {
    @Insert
    fun insert(rootstock: Rootstock)

    @Update
    fun update(rootstock: Rootstock)

    @Delete
    fun delete(rootstock: Rootstock)

    @Query("SELECT * FROM Rootstock")
    fun getRootstocks(): List<Rootstock>
}