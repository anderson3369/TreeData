package com.orchardmanager.treedata.ui.farm

import android.util.Log
import androidx.lifecycle.*
import com.orchardmanager.treedata.entities.Farm
import com.orchardmanager.treedata.entities.FarmerWithFarm
import com.orchardmanager.treedata.repositories.FarmRepository
import com.orchardmanager.treedata.repositories.FarmerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FarmViewModel @Inject constructor(val farmRepository: FarmRepository) : ViewModel() {

    fun getFarmerId():LiveData<Long> {
        return farmRepository.getFarmerId().asLiveData()
    }

    fun add(farm:Farm) = liveData<Long> {
        try {
            val id = farmRepository.createFarm(farm)
            emit(id)
        }catch (e:Exception) {
            e.printStackTrace()
            emit(-1000L)
        }
    }

    fun update(farm: Farm) {
        viewModelScope.launch(Dispatchers.IO) {
            farmRepository.updateFarm(farm)
        }
    }

    fun delete(farm: Farm) {
        viewModelScope.launch(Dispatchers.IO) {
            farmRepository.deleteFarm(farm)
        }
    }

    fun get(farmerId:Long): LiveData<MutableList<Farm>> {
        return farmRepository.getFarms(farmerId).asLiveData()
    }

    fun getFarmerWithFarm(): LiveData<MutableList<FarmerWithFarm>> {
        return farmRepository.getFarmerWithFarms().asLiveData()
    }
}