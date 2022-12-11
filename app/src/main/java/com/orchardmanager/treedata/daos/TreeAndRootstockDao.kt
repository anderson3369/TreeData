package com.orchardmanager.treedata.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.orchardmanager.treedata.entities.TreeAndRootstock

@Dao
interface TreeAndRootstockDao {
    @Transaction
    @Query("SELECT * FROM Tree")
    fun getTreeAndRootstock(): TreeAndRootstock

    @Transaction
    @Query("SELECT * FROM Tree WHERE id = :id")
    fun getTreeAndRootstock(id: Long): TreeAndRootstock
}