package com.orchardmanager.treedata.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.orchardmanager.treedata.entities.Farm

@Dao
interface FarmWithOrchardsDao {
    @Transaction
    @Query("SELECT * FROM Farm")
    fun getOrchardLocationWithOrchards(): List<Farm>

}