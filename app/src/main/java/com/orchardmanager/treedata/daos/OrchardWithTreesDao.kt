package com.orchardmanager.treedata.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.orchardmanager.treedata.entities.OrchardWithTrees

@Dao
interface OrchardWithTreesDao {
    @Transaction
    @Query("SELECT * FROM Orchard")
    fun getOrchardWithTrees(): List<OrchardWithTrees>

    @Transaction
    @Query("SELECT * FROM Orchard WHERE id = :id")
    fun getOrchardWithTrees(id: Long?): List<OrchardWithTrees>
}