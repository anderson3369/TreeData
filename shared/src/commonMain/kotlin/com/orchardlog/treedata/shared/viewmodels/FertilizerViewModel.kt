package com.orchardlog.treedata.shared.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orchardlog.treedata.shared.model.Fertilizer
import com.orchardlog.treedata.shared.model.FertilizerApplication
import com.orchardlog.treedata.shared.model.FertilizerApplicationItem
import com.orchardlog.treedata.shared.model.FertilizerApplicationWithFertilizers
import com.orchardlog.treedata.shared.model.FertilizerApplicationWithItems
import com.orchardlog.treedata.shared.repositories.FertilizerRepository
import com.orchardlog.treedata.shared.sync.FirestoreSync
import com.orchardlog.treedata.shared.TemporalUtils
import kotlin.time.Instant
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import com.orchardlog.treedata.shared.CommonStateFlow
import com.orchardlog.treedata.shared.asCommonStateFlow

class FertilizerViewModel(private val fertilizerRepository: FertilizerRepository) : ViewModel() {

    private val _fertilizers: StateFlow<List<Fertilizer>> = fertilizerRepository.getFertilizers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    val fertilizers: CommonStateFlow<List<Fertilizer>> = _fertilizers.asCommonStateFlow()

    private val _fertilizerApplications: StateFlow<List<FertilizerApplication>> = fertilizerRepository.getFertilizerApplications()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    val fertilizerApplications: CommonStateFlow<List<FertilizerApplication>> = _fertilizerApplications.asCommonStateFlow()

    private val _fertilizerApplicationsWithItems: StateFlow<List<FertilizerApplicationWithItems>> = fertilizerRepository.getFertilizerApplicationsWithItems()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    val fertilizerApplicationsWithItems: CommonStateFlow<List<FertilizerApplicationWithItems>> = _fertilizerApplicationsWithItems.asCommonStateFlow()


    fun addFertilizer(fertilizer: Fertilizer) {
        viewModelScope.launch {
            val toSave = if (fertilizer.firestoreId.isBlank()) {
                fertilizer.copy(firestoreId = TemporalUtils.randomUUID())
            } else {
                fertilizer
            }
            val newId = fertilizerRepository.createFertilizer(toSave)
            val finalFertilizer = toSave.copy(id = newId)
            FirestoreSync.pushFertilizer(finalFertilizer, FirestoreSync.currentFarmSiteId)
        }
    }

    fun updateFertilizer(fertilizer: Fertilizer) {
        viewModelScope.launch {
            val toSave = if (fertilizer.firestoreId.isBlank()) {
                fertilizer.copy(firestoreId = TemporalUtils.randomUUID())
            } else {
                fertilizer
            }
            fertilizerRepository.updateFertilizer(toSave)
            FirestoreSync.pushFertilizer(toSave, FirestoreSync.currentFarmSiteId)
        }
    }

    fun deleteFertilizer(fertilizer: Fertilizer) {
        viewModelScope.launch {
            fertilizerRepository.deleteFertilizer(fertilizer)
            FirestoreSync.deleteFertilizer(fertilizer, FirestoreSync.currentFarmSiteId)
        }
    }

    fun addFertilizerApplication(fertilizerApplication: FertilizerApplication) {
        viewModelScope.launch {
            val toSave = if (fertilizerApplication.firestoreId.isBlank()) {
                fertilizerApplication.copy(firestoreId = TemporalUtils.randomUUID())
            } else {
                fertilizerApplication
            }
            val newId = fertilizerRepository.createFertilizerApplication(toSave)
            val finalApp = toSave.copy(id = newId)
            FirestoreSync.pushFertilizerApplication(finalApp, FirestoreSync.currentFarmSiteId)
        }
    }

    fun updateFertilizerApplication(fertilizerApplication: FertilizerApplication) {
        viewModelScope.launch {
            val toSave = if (fertilizerApplication.firestoreId.isBlank()) {
                fertilizerApplication.copy(firestoreId = TemporalUtils.randomUUID())
            } else {
                fertilizerApplication
            }
            fertilizerRepository.updateFertilizerApplication(toSave)
            FirestoreSync.pushFertilizerApplication(toSave, FirestoreSync.currentFarmSiteId)
        }
    }

    fun deleteFertilizerApplication(fertilizerApplication: FertilizerApplication) {
        viewModelScope.launch {
            fertilizerRepository.deleteFertilizerApplication(fertilizerApplication)
            FirestoreSync.deleteFertilizerApplication(fertilizerApplication, FirestoreSync.currentFarmSiteId)
        }
    }

    fun saveFertilizerApplicationWithItems(application: FertilizerApplication, items: List<FertilizerApplicationItem>) {
        viewModelScope.launch {
            val toSave = if (application.firestoreId.isBlank()) {
                application.copy(firestoreId = TemporalUtils.randomUUID())
            } else {
                application
            }
            fertilizerRepository.saveFertilizerApplicationWithItems(toSave, items)
            FirestoreSync.pushFertilizerApplication(toSave, FirestoreSync.currentFarmSiteId)
        }
    }

    fun updateFertilizerApplicationWithItems(application: FertilizerApplication, items: List<FertilizerApplicationItem>) {
        viewModelScope.launch {
            val toSave = if (application.firestoreId.isBlank()) {
                application.copy(firestoreId = TemporalUtils.randomUUID())
            } else {
                application
            }
            fertilizerRepository.updateFertilizerApplicationWithItems(toSave, items)
            FirestoreSync.pushFertilizerApplication(toSave, FirestoreSync.currentFarmSiteId)
        }
    }

    fun deleteFertilizerApplicationWithItems(application: FertilizerApplication) {
        viewModelScope.launch {
            fertilizerRepository.deleteFertilizerApplicationWithItems(application)
            FirestoreSync.deleteFertilizerApplication(application, FirestoreSync.currentFarmSiteId)
        }
    }

    fun getFertilizerApplicationsWithFertilizers(orchardId: Long, startDate: Instant, endDate: Instant): CommonStateFlow<List<FertilizerApplicationWithFertilizers>> {
        return fertilizerRepository.getFertilizerApplicationWithFertilizers(orchardId, startDate, endDate)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            ).asCommonStateFlow()
    }
}
