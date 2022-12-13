package com.orchardmanager.treedata.ui.orchard

import android.util.Log
import androidx.lifecycle.*
import com.orchardmanager.treedata.entities.Orchard
import com.orchardmanager.treedata.repositories.OrchardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrchardViewModel @Inject constructor(
    private val orchardRepository: OrchardRepository): ViewModel() {

    fun add(orchard: Orchard) = liveData {
        emit("inserting orchard...")
        try {
            val id = orchardRepository.createOrchard(orchard)
            Log.i("OrchardViewModel", "orchard id..." + id.toString())
        } catch(e: Exception) {
            e.printStackTrace()
            emit(e.message)
        }
    }

    fun update(orchard: Orchard) {
        viewModelScope.launch {
            orchardRepository.updateOrchard(orchard)
        }
    }

    fun getOrchards(farmId: Long): LiveData<List<Orchard>> {
        return orchardRepository.getOrchards(farmId).asLiveData()
    }
}