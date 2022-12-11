package com.orchardmanager.treedata.ui.farmer

import android.util.Log
import androidx.lifecycle.*
import com.orchardmanager.treedata.entities.Farmer
import com.orchardmanager.treedata.repositories.FarmerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject


@HiltViewModel
class FarmerViewModel @Inject constructor(val farmerRepository: FarmerRepository): ViewModel() {

    fun add(farmer:Farmer) = liveData {
        emit("inserting...")
        try {
            val id = farmerRepository.createFarmer(farmer)
            Log.i("FarmerViewModel", "the ID " + id.toString())
        } catch (e: Exception) {
            e.printStackTrace()
            emit(e.message)
        }
    }

    fun update(farmer:Farmer) {
        viewModelScope.launch(Dispatchers.IO) {
            farmerRepository.updateFarmer(farmer)
        }
    }

    fun get():LiveData<List<Farmer>> {
        return farmerRepository.getFarmers().asLiveData()
    }

}