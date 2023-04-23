package com.orchardmanager.treedata.repositories

import com.orchardmanager.treedata.daos.FarmWithOrchardsDao
import com.orchardmanager.treedata.daos.OrchardActivityDao
import com.orchardmanager.treedata.daos.OrchardDao
import com.orchardmanager.treedata.entities.Orchard
import com.orchardmanager.treedata.entities.OrchardActivity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrchardRepository @Inject constructor(
    private val orchardDao: OrchardDao,
    private val orchardActivityDao: OrchardActivityDao,
    private val farmWithOrchardsDao: FarmWithOrchardsDao
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
}