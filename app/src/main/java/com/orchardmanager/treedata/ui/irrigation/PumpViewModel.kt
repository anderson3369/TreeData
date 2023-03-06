package com.orchardmanager.treedata.ui.irrigation

import android.widget.Toast
import androidx.lifecycle.*
import com.orchardmanager.treedata.entities.Pump
import com.orchardmanager.treedata.repositories.PumpRepository
import com.orchardmanager.treedata.ui.SAVE_FAILED
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PumpViewModel @Inject constructor(private val pumpRepository: PumpRepository)  : ViewModel() {

    fun addPump(pump: Pump) = liveData<Long> {
        try {
            val id = pumpRepository.createPump(pump)
            emit(id)
        } catch(e: Exception) {
            e.printStackTrace()
            emit(SAVE_FAILED)
        }
    }

    fun updatePump(pump: Pump) {
        viewModelScope.launch(Dispatchers.IO) {
            pumpRepository.updatePump(pump)
        }
    }

    fun deletePump(pump: Pump) {
        viewModelScope.launch(Dispatchers.IO) {
            pumpRepository.deletePump(pump)
        }
    }

    fun getPumps(): LiveData<MutableList<Pump>> {
        return pumpRepository.getPumps().asLiveData()
    }

    fun getPumpsMap(): LiveData<Map<Long, Pump>> {
        return pumpRepository.getPumpsMap().asLiveData()
    }
}