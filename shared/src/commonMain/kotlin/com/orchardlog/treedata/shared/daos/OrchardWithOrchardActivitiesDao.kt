package com.orchardlog.treedata.shared.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.orchardlog.treedata.shared.model.OrchardWithOrchardActivity
import kotlinx.datetime.Instant
import kotlinx.coroutines.flow.Flow

@Dao
interface OrchardWithOrchardActivitiesDao {
    @Transaction
    @Query("SELECT * FROM Orchard JOIN OrchardActivity ON Orchard.id = OrchardActivity.orchardId " +
            "WHERE Orchard.id = :orchardId AND OrchardActivity.activityStart BETWEEN :startDate AND :endDate")
    fun getOrchardWithOrchardActivities(orchardId: Long, startDate: Instant, endDate: Instant): Flow<List<OrchardWithOrchardActivity>>
}
