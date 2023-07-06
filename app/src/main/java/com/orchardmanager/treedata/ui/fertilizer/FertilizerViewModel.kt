package com.orchardmanager.treedata.ui.fertilizer

import androidx.lifecycle.*
import com.orchardmanager.treedata.entities.Fertilizer
import com.orchardmanager.treedata.entities.FertilizerApplication
import com.orchardmanager.treedata.entities.FertilizerApplicationWithFertilizers
import com.orchardmanager.treedata.repositories.FertilizerRepository
import com.orchardmanager.treedata.ui.SAVE_FAILED
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class FertilizerViewModel @Inject constructor(private val fertilizerRepository: FertilizerRepository) : ViewModel() {

    fun addFertilizer(fertilizer: Fertilizer) = liveData<Long> {
        try {
            val id = fertilizerRepository.createFertilizer(fertilizer)
            emit(id)
        } catch(e: Exception) {
            e.printStackTrace()
            emit(SAVE_FAILED)
        }
    }

    fun updateFertilizer(fertilizer: Fertilizer) {
        viewModelScope.launch(Dispatchers.IO) {
            fertilizerRepository.updateFertilizer(fertilizer)
        }
    }

    fun deleteFertilizer(fertilizer: Fertilizer) {
        viewModelScope.launch(Dispatchers.IO) {
            fertilizerRepository.deleteFertilizer(fertilizer)
        }

    }

    fun getFertilizers(): LiveData<MutableList<Fertilizer>> {
        return fertilizerRepository.getFertilizers().asLiveData()
    }

    fun addFertilizerApplication(fertilizerApplication: FertilizerApplication) = liveData<Long> {
        try {
            val id = fertilizerRepository.createFertilizerApplication(fertilizerApplication)
            emit(id)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(SAVE_FAILED)
        }
    }

    fun updateFertilizerApplication(fertilizerApplication: FertilizerApplication) {
        viewModelScope.launch(Dispatchers.IO) {
            fertilizerRepository.updateFertilizerApplication(fertilizerApplication)
        }
    }

    fun deleteFertilizerApplication(fertilizerApplication: FertilizerApplication) {
        viewModelScope.launch(Dispatchers.IO) {
            fertilizerRepository.deleteFertilizerApplication(fertilizerApplication)
        }
    }

    fun getFertilizerApplications(): LiveData<MutableList<FertilizerApplication>> {
        return fertilizerRepository.getFertilizerApplications().asLiveData()
    }

    fun getFertilizerApplicationsWithFertilizers(orchardId: Long, startDate: LocalDate, endDate: LocalDate): LiveData<MutableList<FertilizerApplicationWithFertilizers>> {
        return fertilizerRepository.getFertilizerApplicationWithFertilizers(orchardId,startDate,endDate).asLiveData()
    }
}