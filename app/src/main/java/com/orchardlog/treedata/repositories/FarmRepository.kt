package com.orchardlog.treedata.repositories

import com.orchardlog.treedata.daos.FarmDao
import com.orchardlog.treedata.daos.FarmerDao
import com.orchardlog.treedata.daos.FarmerWithFarmDao
import com.orchardlog.treedata.entities.Farm
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FarmRepository @Inject constructor(private val farmDao: FarmDao,
                                         private val farmerDao: FarmerDao,
                                         private val farmerWithFarmDao: FarmerWithFarmDao
){

    fun getFarmerId() = farmerDao.getFarmerId()

    fun getFarms(farmerId:Long) = farmDao.getFarmById(farmerId)

    fun getFarms() = farmDao.getFarms()

    suspend fun createFarm(farm: Farm): Long {
        return farmDao.insert(farm)
    }

    fun updateFarm(farm: Farm) {
        farmDao.update(farm)
    }

    fun deleteFarm(farm: Farm) {
        farmDao.delete(farm)
    }


}