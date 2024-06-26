package com.orchardlog.treedata.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.orchardlog.treedata.entities.OrchardWithTrees
import kotlinx.coroutines.flow.Flow

@Dao
interface OrchardWithTreesDao {

    @Transaction
    @Query("SELECT * FROM Orchard")
    fun getAllOrchardWithTrees(): Flow<MutableList<OrchardWithTrees>>

    @Transaction
    @Query("SELECT * FROM Orchard WHERE id = :id")
    fun getOrchardWithTrees(id: Long?): Flow<MutableList<OrchardWithTrees>>
}