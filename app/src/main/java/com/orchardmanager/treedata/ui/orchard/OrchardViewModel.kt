package com.orchardmanager.treedata.ui.orchard

import android.util.Log
import androidx.lifecycle.*
import com.orchardmanager.treedata.entities.FarmWithOrchards
import com.orchardmanager.treedata.entities.Orchard
import com.orchardmanager.treedata.repositories.OrchardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrchardViewModel @Inject constructor(
    private val orchardRepository: OrchardRepository): ViewModel() {

    fun add(orchard: Orchard) = liveData<Long> {
        try {
            val id = orchardRepository.createOrchard(orchard)
            emit(id)
        } catch(e: Exception) {
            e.printStackTrace()
            emit(-1000L)
        }
    }

    fun update(orchard: Orchard) {
        viewModelScope.launch(Dispatchers.IO) {
            orchardRepository.updateOrchard(orchard)
        }
    }

    fun delete(orchard: Orchard) {
        viewModelScope.launch(Dispatchers.IO) {
            orchardRepository.deleteOrchard(orchard)
        }
    }

    fun getOrchards(farmId: Long): LiveData<MutableList<Orchard>> {
        return orchardRepository.getOrchards(farmId).asLiveData()
    }

    fun getAllOrchards(): LiveData<MutableList<Orchard>> {
        return orchardRepository.getAllOrchards().asLiveData()
    }

    fun getFarmWithOrchards(): LiveData<MutableList<FarmWithOrchards>> {
        return orchardRepository.getFarmWithOrchards().asLiveData()
    }
}