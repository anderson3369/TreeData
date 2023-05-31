package com.orchardmanager.treedata.repositories

import com.orchardmanager.treedata.daos.FertilizerApplicationDao
import com.orchardmanager.treedata.daos.FertilizerDao
import com.orchardmanager.treedata.entities.Fertilizer
import com.orchardmanager.treedata.entities.FertilizerApplication
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FertilizerRepository @Inject constructor(private val fertilizerDao: FertilizerDao,
                                               private val fertilizerApplicationDao: FertilizerApplicationDao) {

    suspend fun createFertilizer(fertilizer: Fertilizer): Long {
        return fertilizerDao.insert(fertilizer)
    }

    fun updateFertilizer(fertilizer: Fertilizer) {
        fertilizerDao.update(fertilizer)
    }

    fun deleteFertilizer(fertilizer: Fertilizer) {
        fertilizerDao.delete(fertilizer)
    }

    fun getFertilizers() = fertilizerDao.getFertilizers()

    suspend fun createFertilizerApplication(fertilizerApplication: FertilizerApplication): Long {
        return fertilizerApplicationDao.insert(fertilizerApplication)
    }

    fun updateFertilizerApplication(fertilizerApplication: FertilizerApplication) {
        fertilizerApplicationDao.update(fertilizerApplication)
    }

    fun deleteFertilizerApplication(fertilizerApplication: FertilizerApplication) {
        fertilizerApplicationDao.delete(fertilizerApplication)
    }

    fun getFertilizerApplications() = fertilizerApplicationDao.getFertilizerApplications()
}