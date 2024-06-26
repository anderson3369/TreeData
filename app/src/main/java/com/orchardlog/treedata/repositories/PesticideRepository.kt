package com.orchardlog.treedata.repositories

import com.orchardlog.treedata.daos.PesticideApplicationDao
import com.orchardlog.treedata.daos.PesticideApplicationWithPesticidesDao
import com.orchardlog.treedata.daos.PesticideDao
import com.orchardlog.treedata.entities.Pesticide
import com.orchardlog.treedata.entities.PesticideApplication
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PesticideRepository @Inject constructor(
    private val pesticideDao: PesticideDao,
    private val pesticideApplicationDao: PesticideApplicationDao,
    private val pesticideApplicationWithPesticidesDao: PesticideApplicationWithPesticidesDao
    ) {

    suspend fun createPesticide(pesticide: Pesticide): Long {
        return pesticideDao.insert(pesticide)
    }

    fun updatePesticide(pesticide: Pesticide) {
        pesticideDao.update(pesticide)
    }

    fun deletePesticide(pesticide: Pesticide) {
        pesticideDao.delete(pesticide)
    }

    fun getPesticides() = pesticideDao.getPesticides()

    suspend fun createPesticideApplication(pesticideApplication: PesticideApplication): Long {
        return pesticideApplicationDao.insert(pesticideApplication)
    }

    fun updatePesticideApplication(pesticideApplication: PesticideApplication) {
        pesticideApplicationDao.update(pesticideApplication)
    }

    fun deletePesticideApplication(pesticideApplication: PesticideApplication) {
        pesticideApplicationDao.delete(pesticideApplication)
    }

    fun getPesticideApplications() = pesticideApplicationDao.getPesticideApplications()

    fun getPesticideApplicationWithPesticides(orchardId: Long, startDate: LocalDate, endDate: LocalDate) =
        pesticideApplicationWithPesticidesDao.getPesticideApplicationWithPesticides(orchardId, startDate, endDate)
}