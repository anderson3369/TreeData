package com.orchardmanager.treedata.repositories

import com.orchardmanager.treedata.daos.PesticideApplicationDao
import com.orchardmanager.treedata.daos.PesticideDao
import com.orchardmanager.treedata.entities.Pesticide
import com.orchardmanager.treedata.entities.PesticideApplication
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PesticideRepository @Inject constructor(private val pesticideDao: PesticideDao,
    private val pesticideApplicationDao: PesticideApplicationDao) {

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
}