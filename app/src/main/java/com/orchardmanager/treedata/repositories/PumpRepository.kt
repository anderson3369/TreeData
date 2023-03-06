package com.orchardmanager.treedata.repositories

import com.orchardmanager.treedata.daos.PumpDao
import com.orchardmanager.treedata.entities.Pump
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PumpRepository @Inject constructor(val pumpDao: PumpDao) {

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