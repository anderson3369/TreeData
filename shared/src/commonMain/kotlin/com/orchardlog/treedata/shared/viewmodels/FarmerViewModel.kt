package com.orchardlog.treedata.shared.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orchardlog.treedata.shared.model.Farmer
import com.orchardlog.treedata.shared.repositories.FarmerRepository
import com.orchardlog.treedata.shared.sync.FirestoreSync
import com.orchardlog.treedata.shared.TemporalUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.orchardlog.treedata.shared.CommonStateFlow
import com.orchardlog.treedata.shared.asCommonStateFlow

class FarmerViewModel(private val farmerRepository: FarmerRepository) : ViewModel() {

    private val _farmers: StateFlow<List<Farmer>?> = farmerRepository.getFarmers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val farmers: CommonStateFlow<List<Farmer>?> = _farmers.asCommonStateFlow()

    fun addFarmer(farmer: Farmer) {
        viewModelScope.launch {
            val toSave = if (farmer.persistentId.isBlank()) {
                farmer.copy(persistentId = TemporalUtils.randomUUID())
            } else {
                farmer
            }
            val newId = farmerRepository.createFarmer(toSave)
            val finalFarmer = toSave.copy(id = newId)
            FirestoreSync.pushFarmer(finalFarmer, FirestoreSync.currentFarmSiteId)
        }
    }

    fun updateFarmer(farmer: Farmer) {
        viewModelScope.launch {
            val toSave = if (farmer.persistentId.isBlank()) {
                farmer.copy(persistentId = TemporalUtils.randomUUID())
            } else {
                farmer
            }
            farmerRepository.updateFarmer(toSave)
            FirestoreSync.pushFarmer(toSave, FirestoreSync.currentFarmSiteId)
        }
    }

    fun deleteFarmer(farmer: Farmer) {
        viewModelScope.launch {
            farmerRepository.deleteFarmer(farmer)
            FirestoreSync.deleteFarmer(farmer, FirestoreSync.currentFarmSiteId)
        }
    }
}
