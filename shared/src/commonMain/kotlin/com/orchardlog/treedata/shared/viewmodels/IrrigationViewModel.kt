package com.orchardlog.treedata.shared.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orchardlog.treedata.shared.model.Irrigation
import com.orchardlog.treedata.shared.model.IrrigationSystem
import com.orchardlog.treedata.shared.model.PumpsWithIrrigationSystem
import com.orchardlog.treedata.shared.model.SoilMoisture
import com.orchardlog.treedata.shared.repositories.IrrigationRepository
import com.orchardlog.treedata.shared.sync.FirestoreSync
import com.orchardlog.treedata.shared.TemporalUtils
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.orchardlog.treedata.shared.CommonStateFlow
import com.orchardlog.treedata.shared.asCommonStateFlow

class IrrigationViewModel(private val irrigationRepository: IrrigationRepository) : ViewModel() {

    private val _soilMoisture: StateFlow<List<SoilMoisture>> = irrigationRepository.getSoilMoisture()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    val soilMoisture: CommonStateFlow<List<SoilMoisture>> = _soilMoisture.asCommonStateFlow()

    private val _irrigationSystems: StateFlow<List<IrrigationSystem>> = irrigationRepository.getIrrigationSystems()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    val irrigationSystems: CommonStateFlow<List<IrrigationSystem>> = _irrigationSystems.asCommonStateFlow()

    private val _irrigations: StateFlow<List<Irrigation>> = irrigationRepository.getIrrigations()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    val irrigations: CommonStateFlow<List<Irrigation>> = _irrigations.asCommonStateFlow()

    fun getIrrigationsTotalHours(orchardId: Long, startDate: Instant, endDate: Instant): Flow<Long> {
        return irrigationRepository.getIrrigationSystemWithIrrigation(orchardId).map { systems ->
            var totalHours = 0L
            for (system in systems) {
                for (irrigation in system.irrigations) {
                    if (irrigation.startTime >= startDate && irrigation.startTime <= endDate) {
                        totalHours += (irrigation.stopTime - irrigation.startTime).inWholeHours
                    }
                }
            }
            totalHours
        }
    }

    fun getPumpWithIrrigationSystem(orchardId: Long): Flow<PumpsWithIrrigationSystem?> {
        return irrigationRepository.getPumpWithIrrigationSystem(orchardId).map { it.firstOrNull() }
    }

    fun addIrrigation(irrigation: Irrigation) {
        viewModelScope.launch {
            val toSave = if (irrigation.firestoreId.isBlank()) {
                irrigation.copy(firestoreId = TemporalUtils.randomUUID())
            } else {
                irrigation
            }
            val newId = irrigationRepository.createIrrigation(toSave)
            val finalIrrigation = toSave.copy(id = newId)
            FirestoreSync.pushIrrigation(finalIrrigation, FirestoreSync.currentFarmSiteId)
        }
    }

    fun updateIrrigation(irrigation: Irrigation) {
        viewModelScope.launch {
            val toSave = if (irrigation.firestoreId.isBlank()) {
                irrigation.copy(firestoreId = TemporalUtils.randomUUID())
            } else {
                irrigation
            }
            irrigationRepository.updateIrrigation(toSave)
            FirestoreSync.pushIrrigation(toSave, FirestoreSync.currentFarmSiteId)
        }
    }

    fun deleteIrrigation(irrigation: Irrigation) {
        viewModelScope.launch {
            irrigationRepository.deleteIrrigation(irrigation)
            FirestoreSync.deleteIrrigation(irrigation, FirestoreSync.currentFarmSiteId)
        }
    }

    fun addIrrigationSystem(irrigationSystem: IrrigationSystem) {
        viewModelScope.launch {
            val toSave = if (irrigationSystem.firestoreId.isBlank()) {
                irrigationSystem.copy(firestoreId = TemporalUtils.randomUUID())
            } else {
                irrigationSystem
            }
            val newId = irrigationRepository.createIrrigationSystem(toSave)
            val finalSystem = toSave.copy(id = newId)
            FirestoreSync.pushIrrigationSystem(finalSystem, FirestoreSync.currentFarmSiteId)
        }
    }

    fun updateIrrigationSystem(irrigationSystem: IrrigationSystem) {
        viewModelScope.launch {
            val toSave = if (irrigationSystem.firestoreId.isBlank()) {
                irrigationSystem.copy(firestoreId = TemporalUtils.randomUUID())
            } else {
                irrigationSystem
            }
            irrigationRepository.updateIrrigationSystem(toSave)
            FirestoreSync.pushIrrigationSystem(toSave, FirestoreSync.currentFarmSiteId)
        }
    }

    fun deleteIrrigationSystem(irrigationSystem: IrrigationSystem) {
        viewModelScope.launch {
            irrigationRepository.deleteIrrigationSystem(irrigationSystem)
            FirestoreSync.deleteIrrigationSystem(irrigationSystem, FirestoreSync.currentFarmSiteId)
        }
    }

    fun addSoilMoisture(soilMoisture: SoilMoisture) {
        viewModelScope.launch {
            val toSave = if (soilMoisture.firestoreId.isBlank()) {
                soilMoisture.copy(firestoreId = TemporalUtils.randomUUID())
            } else {
                soilMoisture
            }
            val newId = irrigationRepository.createSoilMoisture(toSave)
            val finalMoisture = toSave.copy(id = newId)
            FirestoreSync.pushSoilMoisture(finalMoisture, FirestoreSync.currentFarmSiteId)
        }
    }

    fun updateSoilMoisture(soilMoisture: SoilMoisture) {
        viewModelScope.launch {
            val toSave = if (soilMoisture.firestoreId.isBlank()) {
                soilMoisture.copy(firestoreId = TemporalUtils.randomUUID())
            } else {
                soilMoisture
            }
            irrigationRepository.updateSoilMoisture(toSave)
            FirestoreSync.pushSoilMoisture(toSave, FirestoreSync.currentFarmSiteId)
        }
    }

    fun deleteSoilMoisture(soilMoisture: SoilMoisture) {
        viewModelScope.launch {
            irrigationRepository.deleteSoilMoisture(soilMoisture)
            FirestoreSync.deleteSoilMoisture(soilMoisture, FirestoreSync.currentFarmSiteId)
        }
    }
}
