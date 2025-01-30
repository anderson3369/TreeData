package com.orchardlog.treedata.ui.farm

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.orchardlog.treedata.entities.Farm
import com.orchardlog.treedata.repositories.FarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FarmViewModel @Inject constructor(private val farmRepository: FarmRepository) : ViewModel() {

    companion object {
        const val TAG = "FarmViewModel"
    }

    fun getFarmerId():LiveData<Long> {
        return farmRepository.getFarmerId().asLiveData()
    }

    fun add(farm:Farm) = liveData {
        try {
            val id = farmRepository.createFarm(farm)
            emit(id)
        }catch (e:Exception) {
            Log.i(TAG, e.message, e.cause)
            emit(-1000L)
        }
    }

    fun update(farm: Farm) {
        try {
            viewModelScope.launch(Dispatchers.IO) {
                farmRepository.updateFarm(farm)
            }
        } catch (e:Exception) {
            Log.i(TAG, e.message, e.cause)
            viewModelScope.cancel(e.message.toString(), e.cause)
        }

    }

    fun delete(farm: Farm) {
        try {
            viewModelScope.launch(Dispatchers.IO) {
                farmRepository.deleteFarm(farm)
            }
        } catch (e:Exception) {
            Log.i(TAG, e.message, e.cause)
            viewModelScope.cancel(e.message.toString(), e.cause)
        }

    }

    fun get(farmerId:Long): LiveData<MutableList<Farm>> {
        return farmRepository.getFarms(farmerId).asLiveData()
    }

    fun getFarms(): LiveData<MutableList<Farm>> {
        return farmRepository.getFarms().asLiveData()
    }

}