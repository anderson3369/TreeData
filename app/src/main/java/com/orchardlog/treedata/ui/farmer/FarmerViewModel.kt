package com.orchardlog.treedata.ui.farmer

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.orchardlog.treedata.entities.Farmer
import com.orchardlog.treedata.repositories.FarmerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class FarmerViewModel @Inject constructor(private val farmerRepository: FarmerRepository): ViewModel() {

    fun add(farmer:Farmer) = liveData {
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


}