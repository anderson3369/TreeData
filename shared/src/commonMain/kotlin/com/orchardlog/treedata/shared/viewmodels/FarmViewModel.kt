package com.orchardlog.treedata.shared.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orchardlog.treedata.shared.model.Farm
import com.orchardlog.treedata.shared.repositories.FarmRepository
import com.orchardlog.treedata.shared.sync.FirestoreSync
import com.orchardlog.treedata.shared.TemporalUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import com.orchardlog.treedata.shared.CommonStateFlow
import com.orchardlog.treedata.shared.asCommonStateFlow

class FarmViewModel(private val farmRepository: FarmRepository) : ViewModel() {

    private val _farms: StateFlow<List<Farm>?> = farmRepository.getFarms()
        .onEach { farms ->
            // Set the current farm siteId for Firestore sync
            farms?.firstOrNull()?.siteId?.let { siteId ->
                FirestoreSync.currentFarmSiteId = siteId
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    val farms: CommonStateFlow<List<Farm>?> = _farms.asCommonStateFlow()


    fun addFarm(farm: Farm) {
        viewModelScope.launch {
            val toSave = if (farm.persistentId.isBlank()) {
                farm.copy(persistentId = TemporalUtils.randomUUID())
            } else {
                farm
            }
            farmRepository.createFarm(toSave)
            FirestoreSync.pushFarm(toSave)
        }
    }

    fun updateFarm(farm: Farm) {
        viewModelScope.launch {
            val toSave = if (farm.persistentId.isBlank()) {
                farm.copy(persistentId = TemporalUtils.randomUUID())
            } else {
                farm
            }
            farmRepository.updateFarm(toSave)
            FirestoreSync.pushFarm(toSave)
        }
    }

    fun deleteFarm(farm: Farm) {
        viewModelScope.launch {
            farmRepository.deleteFarm(farm)
            FirestoreSync.deleteFarm(farm)
        }
    }
}
