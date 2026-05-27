package com.orchardlog.treedata.shared.repositories

import com.orchardlog.treedata.shared.daos.FertilizerApplicationDao
import com.orchardlog.treedata.shared.daos.FertilizerApplicationItemDao
import com.orchardlog.treedata.shared.daos.FertilizerApplicationWithFertilizersDao
import kotlin.time.Instant
import com.orchardlog.treedata.shared.daos.FertilizerDao
import com.orchardlog.treedata.shared.model.Fertilizer
import com.orchardlog.treedata.shared.model.FertilizerApplication
import com.orchardlog.treedata.shared.model.FertilizerApplicationItem


class FertilizerRepository (
    private val fertilizerDao: FertilizerDao,
    private val fertilizerApplicationDao: FertilizerApplicationDao,
    private val fertilizerApplicationItemDao: FertilizerApplicationItemDao,
    private val fertilizerApplicationWithFertilizersDao: FertilizerApplicationWithFertilizersDao
    ) {

    suspend fun createFertilizer(fertilizer: Fertilizer): Long {
        return fertilizerDao.insert(fertilizer)
    }

    suspend fun updateFertilizer(fertilizer: Fertilizer) {
        fertilizerDao.update(fertilizer)
    }

    suspend fun deleteFertilizer(fertilizer: Fertilizer) {
        fertilizerDao.delete(fertilizer)
    }

    fun getFertilizers() = fertilizerDao.getFertilizers()

    suspend fun createFertilizerApplication(fertilizerApplication: FertilizerApplication): Long {
        return fertilizerApplicationDao.insert(fertilizerApplication)
    }

    suspend fun updateFertilizerApplication(fertilizerApplication: FertilizerApplication) {
        fertilizerApplicationDao.update(fertilizerApplication)
    }

    suspend fun deleteFertilizerApplication(fertilizerApplication: FertilizerApplication) {
        fertilizerApplicationDao.delete(fertilizerApplication)
    }

    fun getFertilizerApplicationItemsByDate(startDate: Instant, endDate: Instant) =
        fertilizerApplicationItemDao.getFertilizerApplicationItemsByDate(startDate, endDate)

    fun getAllFertilizerApplicationItemsJoined() = fertilizerApplicationItemDao.getAllFertilizerApplicationItemsJoined()

    fun getFertilizerApplications() = fertilizerApplicationDao.getFertilizerApplications()

    fun getFertilizerApplicationsWithItems() = fertilizerApplicationDao.getFertilizerApplicationsWithItems()

    fun getFertilizerApplicationWithFertilizers(orchardId: Long, startDate: Instant, endDate: Instant) =
        fertilizerApplicationWithFertilizersDao.getFertilizerApplicationWithFertilizers(orchardId,startDate,endDate)

    suspend fun saveFertilizerApplicationWithItems(application: FertilizerApplication, items: List<FertilizerApplicationItem>): Long {
        return fertilizerApplicationDao.saveWithItems(application, items)
    }

    suspend fun updateFertilizerApplicationWithItems(application: FertilizerApplication, items: List<FertilizerApplicationItem>) {
        fertilizerApplicationDao.updateWithItems(application, items)
    }

    suspend fun deleteFertilizerApplicationWithItems(application: FertilizerApplication) {
        fertilizerApplicationDao.deleteWithItems(application)
    }
}
