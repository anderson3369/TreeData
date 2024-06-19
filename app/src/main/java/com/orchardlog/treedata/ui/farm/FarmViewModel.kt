package com.orchardlog.treedata.ui.farm

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.orchardlog.treedata.entities.Farm
import com.orchardlog.treedata.repositories.FarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FarmViewModel @Inject constructor(private val farmRepository: FarmRepository) : ViewModel() {

    fun getFarmerId():LiveData<Long> {
        return farmRepository.getFarmerId().asLiveData()
    }

    fun add(farm:Farm) = liveData {
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

    fun getFarms(): LiveData<MutableList<Farm>> {
        return farmRepository.getFarms().asLiveData()
    }

}