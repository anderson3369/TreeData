package com.orchardmanager.treedata.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.orchardmanager.treedata.entities.TreeAndVariety

@Dao
interface TreeAndVarietyDao {
    @Transaction
    @Query("SELECT * FROM Tree")
    fun getTreeAndVariety(): TreeAndVariety

    @Transaction
    @Query("SELECT * FROM Tree WHERE id = :id")
    fun getTreeAndVariety(id: Long): TreeAndVariety
}