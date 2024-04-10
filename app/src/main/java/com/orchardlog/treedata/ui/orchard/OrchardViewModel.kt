package com.orchardlog.treedata.ui.orchard

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.orchardlog.treedata.entities.FarmWithOrchards
import com.orchardlog.treedata.entities.Orchard
import com.orchardlog.treedata.entities.OrchardActivity
import com.orchardlog.treedata.entities.OrchardWithOrchardActivity
import com.orchardlog.treedata.repositories.OrchardRepository
import com.orchardlog.treedata.ui.SAVE_FAILED
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
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
            emit(SAVE_FAILED)
        }
    }

    fun addOrchardActivity(orchardActivity: OrchardActivity) = liveData<Long> {
        try {
            val id = orchardRepository.createOrchardActivity(orchardActivity)
            emit(id)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(SAVE_FAILED)
        }
    }

    fun update(orchard: Orchard) {
        viewModelScope.launch(Dispatchers.IO) {
            orchardRepository.updateOrchard(orchard)
        }
    }

    fun updateOrchardActivity(orchardActivity: OrchardActivity) {
        viewModelScope.launch(Dispatchers.IO) {
            orchardRepository.updateOrchardActivity(orchardActivity)
        }
    }

    fun delete(orchard: Orchard) {
        viewModelScope.launch(Dispatchers.IO) {
            orchardRepository.deleteOrchard(orchard)
        }
    }

    fun deleteOrchardActivity(orchardActivity: OrchardActivity) {
        viewModelScope.launch(Dispatchers.IO) {
            orchardRepository.deleteOrchardActivity(orchardActivity)
        }
    }

    fun getOrchardActivity(): LiveData<MutableList<OrchardActivity>> {
        return orchardRepository.getOrchardActivity().asLiveData()
    }


    fun getFarmWithOrchards(): LiveData<MutableList<FarmWithOrchards>> {
        return orchardRepository.getFarmWithOrchards().asLiveData()
    }

    fun getFarmWithOrchardsMap(): LiveData<Map<Long, String>> {
        return orchardRepository.getFarmWithOrchardsMap().asLiveData()
    }

    fun getOrchardWithOrchardActivities(orchardId: Long, startDate: LocalDate, endDate: LocalDate):
            LiveData<MutableList<OrchardWithOrchardActivity>> {
        return orchardRepository.getOrchardWithOrchardActivities(orchardId,startDate,endDate).asLiveData()
    }

    fun getAllOrchards(): LiveData<MutableList<Orchard>> {
        return orchardRepository.getAllOrchards().asLiveData()
    }

}