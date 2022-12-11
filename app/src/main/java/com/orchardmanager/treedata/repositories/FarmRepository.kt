package com.orchardmanager.treedata.repositories

import com.orchardmanager.treedata.daos.FarmDao
import com.orchardmanager.treedata.daos.FarmerDao
import com.orchardmanager.treedata.entities.Farm
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FarmRepository @Inject constructor(private val farmDao: FarmDao,
                                         private val farmerDao: FarmerDao){

    fun getFarmerId() = farmerDao.getFarmerId()

    fun getFarms(farmerId:Long) = farmDao.getOrchardLocations(farmerId)

    suspend fun createFarm(farm: Farm):Long {
        return farmDao.insert(farm)
    }

    suspend fun updateFarm(farm: Farm) {
        farmDao.update(farm)
    }

    companion object{
        @Volatile private var instance:FarmRepository? = null

        fun getInstance(farmDao: FarmDao,farmerDao: FarmerDao) {
            instance ?: synchronized(this) {
                instance ?: FarmRepository(farmDao,farmerDao).also { instance=it }
            }
        }
    }
}