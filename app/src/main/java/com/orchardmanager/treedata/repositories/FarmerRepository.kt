package com.orchardmanager.treedata.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.room.Dao
import com.orchardmanager.treedata.daos.FarmerDao
import com.orchardmanager.treedata.databinding.FragmentFarmerBinding
import com.orchardmanager.treedata.entities.Farmer
import dagger.Provides
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FarmerRepository @Inject constructor(val farmerDao: FarmerDao) {


    //fun getFarmers() = farmerDao.getFarmers();

    fun getFarmers() = farmerDao.getFarmers();

    suspend fun createFarmer(farmer: Farmer):Long {
        return farmerDao.insert(farmer)
    }

    suspend fun updateFarmer(farmer: Farmer) {
        farmerDao.update(farmer)
    }
    companion object {
        @Volatile private var instance: FarmerRepository? =  null

        fun getInstance(farmerDao: FarmerDao) {
            instance ?: synchronized(this) {
                instance ?: FarmerRepository(farmerDao).also { instance = it }
            }
        }
    }
}