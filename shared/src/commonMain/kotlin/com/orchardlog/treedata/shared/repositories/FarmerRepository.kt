package com.orchardlog.treedata.shared.repositories

import com.orchardlog.treedata.shared.daos.FarmerDao
import com.orchardlog.treedata.shared.model.Farmer
import kotlinx.coroutines.flow.Flow

class FarmerRepository(private val farmerDao: FarmerDao) {

    fun getFarmers(): Flow<List<Farmer>> = farmerDao.getFarmers()

    suspend fun createFarmer(farmer: Farmer): Long {
        return farmerDao.insert(farmer)
    }

    suspend fun updateFarmer(farmer: Farmer) {
        farmerDao.update(farmer)
    }

    suspend fun deleteFarmer(farmer: Farmer) {
        farmerDao.delete(farmer)
    }
}
