package com.orchardlog.treedata.shared.repositories

import com.orchardlog.treedata.shared.daos.PumpDao
import com.orchardlog.treedata.shared.model.Pump
import kotlinx.coroutines.flow.Flow

class PumpRepository (private val pumpDao: PumpDao) {

    suspend fun createPump(pump: Pump): Long {
        return pumpDao.insert(pump)
    }

    suspend fun updatePump(pump: Pump) {
        pumpDao.update(pump)
    }

    suspend fun deletePump(pump: Pump) {
        pumpDao.delete(pump)
    }

    fun getPumps(): Flow<List<Pump>> = pumpDao.getPumps()

    fun getPumpsMap(): Flow<Map<Long, Pump>> = pumpDao.getPumpMap()
}
