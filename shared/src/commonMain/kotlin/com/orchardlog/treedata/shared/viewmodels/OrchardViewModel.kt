package com.orchardlog.treedata.shared.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orchardlog.treedata.shared.model.Orchard
import com.orchardlog.treedata.shared.model.OrchardActivity
import com.orchardlog.treedata.shared.model.OrchardWithFarm
import com.orchardlog.treedata.shared.model.OrchardWithOrchardActivity
import com.orchardlog.treedata.shared.repositories.OrchardRepository
import com.orchardlog.treedata.shared.sync.FirestoreSync
import com.orchardlog.treedata.shared.TemporalUtils
import kotlinx.coroutines.flow.first
import kotlin.time.Instant
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import com.orchardlog.treedata.shared.CommonStateFlow
import com.orchardlog.treedata.shared.asCommonStateFlow

class OrchardViewModel(private val orchardRepository: OrchardRepository) : ViewModel() {

    private val _allOrchards: StateFlow<List<Orchard>?> = orchardRepository.getAllOrchards()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    val allOrchards: CommonStateFlow<List<Orchard>?> = _allOrchards.asCommonStateFlow()

    private val _orchardsWithFarm: StateFlow<List<OrchardWithFarm>> = orchardRepository.getOrchardsWithFarm()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    val orchardsWithFarm: CommonStateFlow<List<OrchardWithFarm>> = _orchardsWithFarm.asCommonStateFlow()


    private val _farmWithOrchardsMap: StateFlow<Map<Long, String>?> = orchardRepository.getFarmWithOrchardsMap()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    val farmWithOrchardsMap: CommonStateFlow<Map<Long, String>?> = _farmWithOrchardsMap.asCommonStateFlow()

    private val _orchardActivities: StateFlow<List<OrchardActivity>> = orchardRepository.getOrchardActivity()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    val orchardActivities: CommonStateFlow<List<OrchardActivity>> = _orchardActivities.asCommonStateFlow()

    fun addOrchard(orchard: Orchard) {
        viewModelScope.launch {
            val toSave = if (orchard.persistentId.isBlank()) {
                orchard.copy(persistentId = TemporalUtils.randomUUID())
            } else {
                orchard
            }
            val newId = orchardRepository.createOrchard(toSave)
            val finalOrchard = toSave.copy(id = newId)
            FirestoreSync.pushOrchard(finalOrchard, FirestoreSync.currentFarmSiteId)
        }
    }

    fun updateOrchard(orchard: Orchard) {
        viewModelScope.launch {
            orchardRepository.updateOrchard(orchard)
            FirestoreSync.pushOrchard(orchard, FirestoreSync.currentFarmSiteId)
        }
    }

    fun deleteOrchard(orchard: Orchard) {
        viewModelScope.launch {
            orchardRepository.deleteOrchard(orchard)
            FirestoreSync.deleteOrchard(orchard, FirestoreSync.currentFarmSiteId)
        }
    }

    fun addOrchardActivity(orchardActivity: OrchardActivity) {
        viewModelScope.launch {
            // 1. Ensure we have a unique cloud ID
            val activityToSave = if (orchardActivity.firestoreId.isBlank()) {
                orchardActivity.copy(firestoreId = TemporalUtils.randomUUID())
            } else {
                orchardActivity
            }
            
            // 2. Save to local SQL DB
            val newId = orchardRepository.createOrchardActivity(activityToSave)
            val finalActivity = activityToSave.copy(id = newId)
            
            // 3. Resolve the Farm's siteId from the Orchard ID (Robust SQL lookup)
            val orchards = _allOrchards.value ?: orchardRepository.getAllOrchards().first()
            val orchard = orchards.find { it.id == orchardActivity.orchardId }
            val farmMap = orchardRepository.getFarmWithOrchardsMap().first()
            val siteId = orchard?.let { farmMap[it.farmId] } ?: FirestoreSync.currentFarmSiteId
            
            // 4. Push to Cloud
            if (siteId.isNotBlank()) {
                FirestoreSync.pushOrchardActivity(finalActivity, siteId)
            }
        }
    }

    fun updateOrchardActivity(orchardActivity: OrchardActivity) {
        viewModelScope.launch {
            // Ensure even old records get a UUID when updated
            val activityToSave = if (orchardActivity.firestoreId.isBlank()) {
                orchardActivity.copy(firestoreId = TemporalUtils.randomUUID())
            } else {
                orchardActivity
            }
            
            orchardRepository.updateOrchardActivity(activityToSave)
            
            val orchards = _allOrchards.value ?: orchardRepository.getAllOrchards().first()
            val orchard = orchards.find { it.id == orchardActivity.orchardId }
            val farmMap = orchardRepository.getFarmWithOrchardsMap().first()
            val siteId = orchard?.let { farmMap[it.farmId] } ?: FirestoreSync.currentFarmSiteId

            if (siteId.isNotBlank()) {
                FirestoreSync.pushOrchardActivity(activityToSave, siteId)
            }
        }
    }

    fun deleteOrchardActivity(orchardActivity: OrchardActivity) {
        viewModelScope.launch {
            orchardRepository.deleteOrchardActivity(orchardActivity)
            FirestoreSync.deleteOrchardActivity(orchardActivity, FirestoreSync.currentFarmSiteId)
        }
    }

    fun getOrchardWithOrchardActivities(orchardId: Long, startDate: Instant, endDate: Instant): CommonStateFlow<List<OrchardWithOrchardActivity>> =
        orchardRepository.getOrchardWithOrchardActivities(orchardId, startDate, endDate)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            ).asCommonStateFlow()

}
