package com.orchardlog.treedata.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.orchardlog.treedata.entities.OrchardActivity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrchardActivityDao {
    @Insert
    suspend fun insert(orchardActivity: OrchardActivity): Long

    @Update
    fun update(orchardActivity: OrchardActivity)

    @Delete
    fun delete(orchardActivity: OrchardActivity)

    @Query("SELECT * From OrchardActivity")
    fun getOrchardActivities(): Flow<MutableList<OrchardActivity>>
}