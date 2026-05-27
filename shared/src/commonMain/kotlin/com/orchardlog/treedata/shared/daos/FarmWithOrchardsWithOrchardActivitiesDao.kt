package com.orchardlog.treedata.shared.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.orchardlog.treedata.shared.model.FarmWithOrchardsWithOrchardActivities
import kotlinx.coroutines.flow.Flow

@Dao
interface FarmWithOrchardsWithOrchardActivitiesDao {
    @Transaction
    @Query("SELECT * FROM Farm WHERE id IN (SELECT farmId FROM Orchard WHERE id = :orchardId)")
    fun getFarmWithOrchardsWithOrchardActivities(orchardId: Long): Flow<List<FarmWithOrchardsWithOrchardActivities>>
}
