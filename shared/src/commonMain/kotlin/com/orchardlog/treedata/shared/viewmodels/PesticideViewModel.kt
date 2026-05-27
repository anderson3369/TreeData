package com.orchardlog.treedata.shared.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orchardlog.treedata.shared.model.Pesticide
import com.orchardlog.treedata.shared.model.PesticideApplication
import com.orchardlog.treedata.shared.model.PesticideApplicationItem
import com.orchardlog.treedata.shared.model.PesticideApplicationWithPesticides
import com.orchardlog.treedata.shared.model.PesticideApplicationWithItems
import com.orchardlog.treedata.shared.repositories.PesticideRepository
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

class PesticideViewModel(private val pesticideRepository: PesticideRepository) : ViewModel() {

    private val _pesticides: StateFlow<List<Pesticide>> = pesticideRepository.getPesticides()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    val pesticides: CommonStateFlow<List<Pesticide>> = _pesticides.asCommonStateFlow()


    private val _pesticideApplications: StateFlow<List<PesticideApplication>> = pesticideRepository.getPesticideApplications()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    val pesticideApplications: CommonStateFlow<List<PesticideApplication>> = _pesticideApplications.asCommonStateFlow()

    private val _pesticideApplicationsWithItems: StateFlow<List<PesticideApplicationWithItems>> = pesticideRepository.getPesticideApplicationsWithItems()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    val pesticideApplicationsWithItems: CommonStateFlow<List<PesticideApplicationWithItems>> = _pesticideApplicationsWithItems.asCommonStateFlow()


    fun addPesticide(pesticide: Pesticide) {
        viewModelScope.launch {
            val toSave = if (pesticide.firestoreId.isBlank()) {
                pesticide.copy(firestoreId = TemporalUtils.randomUUID())
            } else {
                pesticide
            }
            val newId = pesticideRepository.createPesticide(toSave)
            val finalPesticide = toSave.copy(id = newId)
            FirestoreSync.pushPesticide(finalPesticide, FirestoreSync.currentFarmSiteId)
        }
    }

    fun addPesticideApplication(pesticideApplication: PesticideApplication) {
        viewModelScope.launch {
            val toSave = if (pesticideApplication.firestoreId.isBlank()) {
                pesticideApplication.copy(firestoreId = TemporalUtils.randomUUID())
            } else {
                pesticideApplication
            }
            val newId = pesticideRepository.createPesticideApplication(toSave)
            val finalApp = toSave.copy(id = newId)
            FirestoreSync.pushPesticideApplication(finalApp, FirestoreSync.currentFarmSiteId)
        }
    }

    fun addPesticideApplicationItem(pesticideApplicationItems: List<PesticideApplicationItem>) {
        viewModelScope.launch {
            pesticideRepository.createPesticideApplicationItem(pesticideApplicationItems)
        }
    }

    fun updatePesticide(pesticide: Pesticide) {
        viewModelScope.launch {
            val toSave = if (pesticide.firestoreId.isBlank()) {
                pesticide.copy(firestoreId = TemporalUtils.randomUUID())
            } else {
                pesticide
            }
            pesticideRepository.updatePesticide(toSave)
            FirestoreSync.pushPesticide(toSave, FirestoreSync.currentFarmSiteId)
        }
    }

    fun updatePesticideApplication(pesticideApplication: PesticideApplication) {
        viewModelScope.launch {
            val toSave = if (pesticideApplication.firestoreId.isBlank()) {
                pesticideApplication.copy(firestoreId = TemporalUtils.randomUUID())
            } else {
                pesticideApplication
            }
            pesticideRepository.updatePesticideApplication(toSave)
            FirestoreSync.pushPesticideApplication(toSave, FirestoreSync.currentFarmSiteId)
        }
    }

    fun deletePesticide(pesticide: Pesticide) {
        viewModelScope.launch {
            pesticideRepository.deletePesticide(pesticide)
            FirestoreSync.deletePesticide(pesticide, FirestoreSync.currentFarmSiteId)
        }
    }

    fun deletePesticideApplication(pesticideApplication: PesticideApplication) {
        viewModelScope.launch {
            pesticideRepository.deletePesticideApplication(pesticideApplication)
            FirestoreSync.deletePesticideApplication(pesticideApplication, FirestoreSync.currentFarmSiteId)
        }
    }

    fun savePesticideApplicationWithItems(application: PesticideApplication, items: List<PesticideApplicationItem>) {
        viewModelScope.launch {
            val toSave = if (application.firestoreId.isBlank()) {
                application.copy(firestoreId = TemporalUtils.randomUUID())
            } else {
                application
            }
            pesticideRepository.savePesticideApplicationWithItems(toSave, items)
            FirestoreSync.pushPesticideApplication(toSave, FirestoreSync.currentFarmSiteId)
        }
    }

    fun updatePesticideApplicationWithItems(application: PesticideApplication, items: List<PesticideApplicationItem>) {
        viewModelScope.launch {
            val toSave = if (application.firestoreId.isBlank()) {
                application.copy(firestoreId = TemporalUtils.randomUUID())
            } else {
                application
            }
            pesticideRepository.updatePesticideApplicationWithItems(toSave, items)
            FirestoreSync.pushPesticideApplication(toSave, FirestoreSync.currentFarmSiteId)
        }
    }

    fun deletePesticideApplicationWithItems(application: PesticideApplication) {
        viewModelScope.launch {
            pesticideRepository.deletePesticideApplicationWithItems(application)
            FirestoreSync.deletePesticideApplication(application, FirestoreSync.currentFarmSiteId)
        }
    }

    fun getPesticideApplicationWithPesticides(orchardId: Long, startDate: Instant, endDate: Instant): CommonStateFlow<List<PesticideApplicationWithPesticides>> {
        return pesticideRepository.getPesticideApplicationWithPesticides(orchardId, startDate, endDate)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            ).asCommonStateFlow()
    }
}
