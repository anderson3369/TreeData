package com.orchardlog.treedata.shared.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.orchardlog.treedata.shared.model.OrchardWithTrees
import kotlinx.coroutines.flow.Flow

@Dao
interface OrchardWithTreesDao {

    @Transaction
    @Query("SELECT * FROM Orchard")
    fun getAllOrchardWithTrees(): Flow<List<OrchardWithTrees>>

    @Transaction
    @Query("SELECT * FROM Orchard WHERE id = :orchardId")
    fun getOrchardWithTrees(orchardId: Long): Flow<List<OrchardWithTrees>>
}
