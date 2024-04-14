package com.orchardlog.treedata.repositories

import com.orchardlog.treedata.daos.FarmWithOrchardsDao
import com.orchardlog.treedata.daos.FarmWithOrchardsWithOrchardActivitiesDao
import com.orchardlog.treedata.daos.OrchardActivityDao
import com.orchardlog.treedata.daos.OrchardDao
import com.orchardlog.treedata.daos.OrchardWithOrchardActivitiesDao
import com.orchardlog.treedata.entities.Orchard
import com.orchardlog.treedata.entities.OrchardActivity
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrchardRepository @Inject constructor(
    private val orchardDao: OrchardDao,
    private val orchardActivityDao: OrchardActivityDao,
    private val farmWithOrchardsDao: FarmWithOrchardsDao,
    private val orchardWithOrchardActivitiesDao: OrchardWithOrchardActivitiesDao,
    private val farmWithOrchardsWithOrchardActivitiesDao: FarmWithOrchardsWithOrchardActivitiesDao
    ) {

    suspend fun createOrchard(orchard: Orchard): Long {
        return orchardDao.insert(orchard)
    }

    fun updateOrchard(orchard: Orchard) {
        orchardDao.update(orchard)
    }

    fun deleteOrchard(orchard: Orchard) {
        orchardDao.delete(orchard)
    }

    fun getOrchards(farmId: Long) = orchardDao.getOrchards(farmId)

    fun getAllOrchards() = orchardDao.getAllrchards()

    suspend fun createOrchardActivity(orchardActivity: OrchardActivity): Long {
        return orchardActivityDao.insert(orchardActivity)
    }

    fun updateOrchardActivity(orchardActivity: OrchardActivity) {
        orchardActivityDao.update(orchardActivity)
    }

    fun deleteOrchardActivity(orchardActivity: OrchardActivity) {
        orchardActivityDao.delete(orchardActivity)
    }

    fun getOrchardActivity() = orchardActivityDao.getOrchardActivities()

    fun getFarmWithOrchards() = farmWithOrchardsDao.getFarmWithOrchards()

    fun getFarmWithOrchardsMap() = farmWithOrchardsDao.getFarmWithOrchardsMap()

    fun getOrchardWithOrchardActivities(orchardId: Long, startDate: LocalDate, endDate: LocalDate) =
        orchardWithOrchardActivitiesDao.getOrchardWithOrchardActivities(orchardId, startDate, endDate)

    fun getFarmWithOrchardsWithOrchardActivities(orchardId: Long, startDate: LocalDate, endDate: LocalDate) =
        farmWithOrchardsWithOrchardActivitiesDao.getFarmWithOrchardsWithOrchardActivities(orchardId, startDate, endDate)
}