package com.orchardlog.treedata.shared.repositories

import com.orchardlog.treedata.shared.daos.FarmWithOrchardsDao
import com.orchardlog.treedata.shared.daos.FarmWithOrchardsWithOrchardActivitiesDao
import com.orchardlog.treedata.shared.daos.OrchardActivityDao
import com.orchardlog.treedata.shared.daos.OrchardDao
import com.orchardlog.treedata.shared.daos.OrchardWithOrchardActivitiesDao
import com.orchardlog.treedata.shared.model.Orchard
import com.orchardlog.treedata.shared.model.OrchardActivity
import kotlin.time.Instant
import com.orchardlog.treedata.shared.model.OrchardWithFarm
import com.orchardlog.treedata.shared.TemporalUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

class OrchardRepository(
    private val orchardDao: OrchardDao,
    private val orchardActivityDao: OrchardActivityDao,
    private val farmWithOrchardsDao: FarmWithOrchardsDao,
    private val orchardWithOrchardActivitiesDao: OrchardWithOrchardActivitiesDao,
    private val farmWithOrchardsWithOrchardActivitiesDao: FarmWithOrchardsWithOrchardActivitiesDao
) {

    suspend fun createOrchard(orchard: Orchard): Long {
        val now = TemporalUtils.now()
        val orchardToInsert = orchard.copy(
            validFrom = now,
            validTo = TemporalUtils.INFINITY
        )
        return orchardDao.insert(orchardToInsert)
    }

    suspend fun updateOrchard(orchard: Orchard) {
        val now = TemporalUtils.now()
        // 1. Close the current version
        val currentVersion = orchard.copy(validTo = now)
        orchardDao.update(currentVersion)

        // 2. Insert new version
        val newVersion = orchard.copy(
            id = 0L, // New primary key
            validFrom = now,
            validTo = TemporalUtils.INFINITY
        )
        orchardDao.insert(newVersion)
    }

    suspend fun deleteOrchard(orchard: Orchard) {
        orchardDao.delete(orchard)
    }

    fun getAllOrchards(): Flow<List<Orchard>> = orchardDao.getOrchards(TemporalUtils.now())

    fun getOrchardsWithFarm(): Flow<List<OrchardWithFarm>> = orchardDao.getOrchardsWithFarm(TemporalUtils.now())

    suspend fun getOrchardCurrent(persistentId: String): Orchard? {
        return orchardDao.getOrchardByPersistentId(persistentId, TemporalUtils.now())
    }

    suspend fun createOrchardActivity(orchardActivity: OrchardActivity): Long {
        return orchardActivityDao.insert(orchardActivity)
    }

    suspend fun updateOrchardActivity(orchardActivity: OrchardActivity) {
        orchardActivityDao.update(orchardActivity)
    }

    suspend fun deleteOrchardActivity(orchardActivity: OrchardActivity) {
        orchardActivityDao.delete(orchardActivity)
    }

    fun getOrchardActivity(): Flow<List<OrchardActivity>> = orchardActivityDao.getOrchardActivities()

    fun getFarmWithOrchardsMap(): Flow<Map<Long, String>> = farmWithOrchardsDao.getFarmWithOrchardsMap()

    fun getOrchardWithOrchardActivities(orchardId: Long, startDate: Instant, endDate: Instant) =
        orchardWithOrchardActivitiesDao.getOrchardWithOrchardActivities(orchardId, startDate, endDate)

    //fun getFarmWithOrchardsWithOrchardActivities(orchardId: Long, startDate: LocalDate, endDate: LocalDate) =
        //farmWithOrchardsWithOrchardActivitiesDao.getFarmWithOrchardsWithOrchardActivities(orchardId, startDate, endDate)
}
