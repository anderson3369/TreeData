package com.orchardlog.treedata.repositories

import com.orchardlog.treedata.daos.FertilizerApplicationDao
import com.orchardlog.treedata.daos.FertilizerApplicationWithFertilizersDao
import com.orchardlog.treedata.daos.FertilizerDao
import com.orchardlog.treedata.entities.Fertilizer
import com.orchardlog.treedata.entities.FertilizerApplication
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FertilizerRepository @Inject constructor(
    private val fertilizerDao: FertilizerDao,
    private val fertilizerApplicationDao: FertilizerApplicationDao,
    private val fertilizerApplicationWithFertilizersDao: FertilizerApplicationWithFertilizersDao
    ) {

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

    fun getFertilizerApplicationWithFertilizers(orchardId: Long, startDate: LocalDate, endDate: LocalDate) =
        fertilizerApplicationWithFertilizersDao.getFertilizerApplicationWithFertilizers(orchardId,startDate,endDate)
}