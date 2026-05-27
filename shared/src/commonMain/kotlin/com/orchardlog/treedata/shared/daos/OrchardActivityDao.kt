package com.orchardlog.treedata.shared.daos

import androidx.room.*
import com.orchardlog.treedata.shared.model.OrchardActivity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrchardActivityDao {
    @Insert
    suspend fun insert(orchardActivity: OrchardActivity): Long

    @Update
    suspend fun update(orchardActivity: OrchardActivity)

    @Delete
    suspend fun delete(orchardActivity: OrchardActivity)

    @Query("SELECT * FROM OrchardActivity")
    fun getOrchardActivities(): Flow<List<OrchardActivity>>
}
