package com.orchardmanager.treedata.repositories

import com.orchardmanager.treedata.daos.FarmWithOrchardsDao
import com.orchardmanager.treedata.daos.OrchardDao
import com.orchardmanager.treedata.entities.Orchard
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrchardRepository @Inject constructor(
    private val orchardDao: OrchardDao,
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

    fun getFarmWithOrchards() = farmWithOrchardsDao.getFarmWithOrchards()

    fun getFarmWithOrchardsMap() = farmWithOrchardsDao.getFarmWithOrchardsMap()
}