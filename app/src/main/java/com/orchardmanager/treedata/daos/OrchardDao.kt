package com.orchardmanager.treedata.daos

import androidx.room.*
import com.orchardmanager.treedata.entities.Orchard

@Dao
interface OrchardDao {
    @Insert
    fun insert(orchard: Orchard)

    @Update
    fun update(orchard: Orchard)

    @Delete
    fun delete(orchard: Orchard)

    @Query("SELECT * FROM Orchard")
    fun getOrchards(): List<Orchard>
}