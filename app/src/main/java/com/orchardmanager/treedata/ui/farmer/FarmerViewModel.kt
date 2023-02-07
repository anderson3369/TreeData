package com.orchardmanager.treedata.ui.farmer

import android.util.Log
import androidx.lifecycle.*
import com.orchardmanager.treedata.entities.Farmer
import com.orchardmanager.treedata.repositories.FarmerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class FarmerViewModel @Inject constructor(val farmerRepository: FarmerRepository): ViewModel() {

    fun add(farmer:Farmer) = liveData<Long> {
        try {
            val id = farmerRepository.createFarmer(farmer)
            emit(id)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(-1000L)
        }
    }

    fun update(farmer:Farmer) {
        viewModelScope.launch(Dispatchers.IO) {
            farmerRepository.updateFarmer(farmer)
        }
    }

    fun get(): LiveData<List<Farmer>> {
        return farmerRepository.getFarmers().asLiveData()
    }

    fun getFarmerId(): LiveData<Long> {
        return farmerRepository.getFarmerId().asLiveData()
    }

}