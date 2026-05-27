package com.orchardlog.treedata.shared.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orchardlog.treedata.shared.model.Pump
import com.orchardlog.treedata.shared.repositories.PumpRepository
import com.orchardlog.treedata.shared.sync.FirestoreSync
import com.orchardlog.treedata.shared.TemporalUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.orchardlog.treedata.shared.CommonStateFlow
import com.orchardlog.treedata.shared.asCommonStateFlow

class PumpViewModel(private val pumpRepository: PumpRepository) : ViewModel() {

    private val _pumps: StateFlow<List<Pump>> = pumpRepository.getPumps()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    val pumps: CommonStateFlow<List<Pump>> = _pumps.asCommonStateFlow()


    private val _pumpsMap: StateFlow<Map<Long, Pump>> = pumpRepository.getPumpsMap()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )
    val pumpsMap: CommonStateFlow<Map<Long, Pump>> = _pumpsMap.asCommonStateFlow()



    fun addPump(pump: Pump) {
        viewModelScope.launch {
            val toSave = if (pump.firestoreId.isBlank()) {
                pump.copy(firestoreId = TemporalUtils.randomUUID())
            } else {
                pump
            }
            val newId = pumpRepository.createPump(toSave)
            val finalPump = toSave.copy(id = newId)
            FirestoreSync.pushPump(finalPump, FirestoreSync.currentFarmSiteId)
        }
    }

    fun updatePump(pump: Pump) {
        viewModelScope.launch {
            val toSave = if (pump.firestoreId.isBlank()) {
                pump.copy(firestoreId = TemporalUtils.randomUUID())
            } else {
                pump
            }
            pumpRepository.updatePump(toSave)
            FirestoreSync.pushPump(toSave, FirestoreSync.currentFarmSiteId)
        }
    }

    fun deletePump(pump: Pump) {
        viewModelScope.launch {
            pumpRepository.deletePump(pump)
            FirestoreSync.deletePump(pump, FirestoreSync.currentFarmSiteId)
        }
    }
}
