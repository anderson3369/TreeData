package com.orchardmanager.treedata.repositories

import com.orchardmanager.treedata.daos.FarmerDao
import com.orchardmanager.treedata.daos.FarmerWithFarmDao
import com.orchardmanager.treedata.entities.Farmer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FarmerRepository @Inject constructor(private val farmerDao: FarmerDao) {

    //fun getFarmers() = farmerDao.getFarmers()

    fun getFarmers() = farmerDao.getFarmers()

    suspend fun createFarmer(farmer: Farmer): Long {
        return farmerDao.insert(farmer)
    }

    fun updateFarmer(farmer: Farmer) {
        farmerDao.update(farmer)
    }

    fun getFarmerId() = farmerDao.getFarmerId()



    companion object {
        @Volatile private var instance: FarmerRepository? =  null

        fun getInstance(farmerDao: FarmerDao) {
            instance ?: synchronized(this) {
                instance ?: FarmerRepository(farmerDao).also { instance = it }
            }
        }
    }
}