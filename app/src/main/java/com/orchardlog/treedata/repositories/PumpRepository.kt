package com.orchardlog.treedata.repositories

import com.orchardlog.treedata.daos.PumpDao
import com.orchardlog.treedata.entities.Pump
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PumpRepository @Inject constructor(private val pumpDao: PumpDao) {

    suspend fun createPump(pump: Pump): Long {
        return pumpDao.insert(pump)
    }

    fun updatePump(pump: Pump) {
        pumpDao.update(pump)
    }

    fun deletePump(pump: Pump) {
        pumpDao.delete(pump)
    }

    fun getPumps() = pumpDao.getPumps()

    fun getPumpsMap() = pumpDao.getPumpMap()
}