package com.orchardlog.treedata.shared.repositories

import com.orchardlog.treedata.shared.daos.FarmDao
import com.orchardlog.treedata.shared.daos.FarmerDao
import com.orchardlog.treedata.shared.daos.FarmerWithFarmDao
import com.orchardlog.treedata.shared.model.Farm
import com.orchardlog.treedata.shared.TemporalUtils
import kotlinx.coroutines.flow.Flow

class FarmRepository(
    private val farmDao: FarmDao,
    private val farmerDao: FarmerDao,
    private val farmerWithFarmDao: FarmerWithFarmDao
) {

    fun getFarmerId(): Flow<Long> = farmerDao.getFarmerId()

    fun getFarms(farmerId: Long): Flow<List<Farm>> = farmDao.getFarmById(farmerId, TemporalUtils.now())

    fun getFarms(): Flow<List<Farm>> = farmDao.getFarms(TemporalUtils.now())

    suspend fun getFarmCurrent(persistentId: String): Farm? {
        return farmDao.getFarmByPersistentId(persistentId, TemporalUtils.now())
    }

    suspend fun createFarm(farm: Farm): Long {
        val now = TemporalUtils.now()
        val farmToInsert = farm.copy(
            validFrom = now,
            validTo = TemporalUtils.INFINITY
        )
        return farmDao.insert(farmToInsert)
    }

    suspend fun updateFarm(farm: Farm) {
        val now = TemporalUtils.now()
        // 1. Close the current version
        val currentVersion = farm.copy(validTo = now)
        farmDao.update(currentVersion)
        
        // 2. Insert new version
        val newVersion = farm.copy(
            id = 0L, // New primary key
            validFrom = now,
            validTo = TemporalUtils.INFINITY
        )
        farmDao.insert(newVersion)
    }

    suspend fun deleteFarm(farm: Farm) {
        farmDao.delete(farm)
    }
}
