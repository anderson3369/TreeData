package com.orchardlog.treedata.shared.repositories

import com.orchardlog.treedata.shared.daos.PesticideApplicationDao
import com.orchardlog.treedata.shared.daos.PesticideApplicationWithPesticidesDao
import kotlin.time.Instant
import com.orchardlog.treedata.shared.daos.PesticideDao
import com.orchardlog.treedata.shared.model.Pesticide
import com.orchardlog.treedata.shared.model.PesticideApplication
import com.orchardlog.treedata.shared.model.PesticideApplicationItem
import com.orchardlog.treedata.shared.model.PesticideApplicationWithItems

class PesticideRepository (
    private val pesticideDao: PesticideDao,
    private val pesticideApplicationDao: PesticideApplicationDao,
    private val pesticideApplicationWithPesticidesDao: PesticideApplicationWithPesticidesDao
    ) {

    suspend fun createPesticide(pesticide: Pesticide): Long {
        return pesticideDao.insert(pesticide)
    }

    suspend fun updatePesticide(pesticide: Pesticide) {
        pesticideDao.update(pesticide)
    }

    suspend fun deletePesticide(pesticide: Pesticide) {
        pesticideDao.delete(pesticide)
    }

    fun getPesticides() = pesticideDao.getPesticides()

    suspend fun createPesticideApplication(pesticideApplication: PesticideApplication): Long {
        return pesticideApplicationDao.insert(pesticideApplication)
    }

    suspend fun createPesticideApplicationItem(pesticideApplicationItems: List<PesticideApplicationItem>) {
        pesticideApplicationDao.insertItems(pesticideApplicationItems)
    }

    suspend fun updatePesticideApplication(pesticideApplication: PesticideApplication) {
        pesticideApplicationDao.update(pesticideApplication)
    }

    suspend fun deletePesticideApplication(pesticideApplication: PesticideApplication) {
        pesticideApplicationDao.delete(pesticideApplication)
    }

    fun getPesticideApplications() = pesticideApplicationDao.getPesticideApplications()

    fun getPesticideApplicationsWithItems() = pesticideApplicationDao.getPesticideApplicationsWithItems()

    fun getPesticideApplicationWithPesticides(orchardId: Long, startDate: Instant, endDate: Instant) =
        pesticideApplicationWithPesticidesDao.getPesticideApplicationWithPesticides(orchardId, startDate, endDate)

    suspend fun savePesticideApplicationWithItem(application: PesticideApplication, items: List<PesticideApplicationItem>): Long {
        val id = pesticideApplicationDao.insert(application)
        pesticideApplicationDao.insertItems(items)
        return id
    }

    suspend fun updatePesticideApplicationWithItems(application: PesticideApplication, items: List<PesticideApplicationItem>) {
        pesticideApplicationDao.updateWithItems(application, items)
    }

    suspend fun savePesticideApplicationWithItems(application: PesticideApplication, items: List<PesticideApplicationItem>): Long {
        return pesticideApplicationDao.saveWithItems(application, items)
    }

    suspend fun deletePesticideApplicationWithItems(application: PesticideApplication) {
        pesticideApplicationDao.deleteWithItems(application)
    }
}
