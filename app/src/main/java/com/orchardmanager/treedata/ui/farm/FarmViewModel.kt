package com.orchardmanager.treedata.ui.farm

import android.util.Log
import androidx.lifecycle.*
import com.orchardmanager.treedata.entities.Farm
import com.orchardmanager.treedata.entities.FarmerWithFarm
import com.orchardmanager.treedata.repositories.FarmRepository
import com.orchardmanager.treedata.repositories.FarmerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FarmViewModel @Inject constructor(val farmRepository: FarmRepository) : ViewModel() {

    fun getFarmerId():LiveData<Long> {
        return farmRepository.getFarmerId().asLiveData()
    }

    fun add(farm:Farm) = liveData {
        emit("adding a Farm")
        try {
            val id = farmRepository.createFarm(farm)
            Log.i("FarmViewModel", "the farm ID..." + id.toString())
        }catch (e:Exception) {
            e.printStackTrace()
            emit(e.message)
        }
    }

    fun update(farm: Farm) {
        viewModelScope.launch {
            farmRepository.updateFarm(farm)
        }
    }

    fun get(farmerId:Long): LiveData<MutableList<Farm>> {
        return farmRepository.getFarms(farmerId).asLiveData()
    }

    fun getFarmerWithFarm(): LiveData<MutableList<FarmerWithFarm>> {
        return farmRepository.getFarmerWithFarms().asLiveData()
    }
}