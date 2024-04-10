package com.orchardlog.treedata.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RoomWarnings
import androidx.room.Transaction
import com.orchardlog.treedata.entities.OrchardWithOrchardActivity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface OrchardWithOrchardActivitiesDao {
    @Transaction
    @Query("SELECT * FROM Orchard O JOIN OrchardActivity OA ON O.id = OA.orchardId " +
            "WHERE OA.orchardId = :orchardId AND OA.activityStart BETWEEN :startDate AND :enddDate")
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    fun getOrchardWithOrchardActivities(orchardId: Long, startDate: LocalDate, enddDate: LocalDate):
            Flow<MutableList<OrchardWithOrchardActivity>>
}