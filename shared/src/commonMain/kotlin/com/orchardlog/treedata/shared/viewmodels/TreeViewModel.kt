package com.orchardlog.treedata.shared.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orchardlog.treedata.shared.model.Rootstock
import com.orchardlog.treedata.shared.model.Tree
import com.orchardlog.treedata.shared.model.Variety
import com.orchardlog.treedata.shared.repositories.TreeRepository
import com.orchardlog.treedata.shared.sync.FirestoreSync
import com.orchardlog.treedata.shared.TemporalUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.orchardlog.treedata.shared.CommonStateFlow
import com.orchardlog.treedata.shared.asCommonStateFlow

class TreeViewModel(private val treeRepository: TreeRepository) : ViewModel() {

    private val _allTrees: StateFlow<List<Tree>?> = treeRepository.getAllTrees()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    val allTrees: CommonStateFlow<List<Tree>?> = _allTrees.asCommonStateFlow()


    private val _rootstocks: StateFlow<List<Rootstock>?> = treeRepository.getAllRootstocks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    val rootstocks: CommonStateFlow<List<Rootstock>?> = _rootstocks.asCommonStateFlow()


    private val _varieties: StateFlow<List<Variety>?> = treeRepository.getAllVarieties()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    val varieties: CommonStateFlow<List<Variety>?> = _varieties.asCommonStateFlow()



    fun addTree(tree: Tree) {
        viewModelScope.launch {
            val toSave = if (tree.persistentId.isBlank()) {
                tree.copy(persistentId = TemporalUtils.randomUUID())
            } else {
                tree
            }
            val newId = treeRepository.createTree(toSave)
            val finalTree = toSave.copy(id = newId)
            FirestoreSync.pushTree(finalTree, FirestoreSync.currentFarmSiteId)
        }
    }

    fun updateTree(tree: Tree) {
        viewModelScope.launch {
            val toSave = if (tree.persistentId.isBlank()) {
                tree.copy(persistentId = TemporalUtils.randomUUID())
            } else {
                tree
            }
            treeRepository.updateTree(toSave)
            FirestoreSync.pushTree(toSave, FirestoreSync.currentFarmSiteId)
        }
    }

    fun deleteTree(tree: Tree) {
        viewModelScope.launch {
            treeRepository.deleteTree(tree)
            FirestoreSync.deleteTree(tree, FirestoreSync.currentFarmSiteId)
        }
    }

    fun addRootstock(rootstock: Rootstock) {
        viewModelScope.launch {
            val toSave = if (rootstock.firestoreId.isBlank()) {
                rootstock.copy(firestoreId = TemporalUtils.randomUUID())
            } else {
                rootstock
            }
            val newId = treeRepository.createRootstock(toSave)
            val finalRootstock = toSave.copy(id = newId)
            FirestoreSync.pushRootstock(finalRootstock, FirestoreSync.currentFarmSiteId)
        }
    }

    fun addVariety(variety: Variety) {
        viewModelScope.launch {
            val toSave = if (variety.firestoreId.isBlank()) {
                variety.copy(firestoreId = TemporalUtils.randomUUID())
            } else {
                variety
            }
            val newId = treeRepository.createVariety(toSave)
            val finalVariety = toSave.copy(id = newId)
            FirestoreSync.pushVariety(finalVariety, FirestoreSync.currentFarmSiteId)
        }
    }

    fun updateVariety(variety: Variety) {
        viewModelScope.launch {
            val toSave = if (variety.firestoreId.isBlank()) {
                variety.copy(firestoreId = TemporalUtils.randomUUID())
            } else {
                variety
            }
            treeRepository.updateVariety(toSave)
            FirestoreSync.pushVariety(toSave, FirestoreSync.currentFarmSiteId)
        }
    }

    fun deleteVariety(variety: Variety) {
        viewModelScope.launch {
            treeRepository.deleteVariety(variety)
            FirestoreSync.deleteVariety(variety, FirestoreSync.currentFarmSiteId)
        }
    }

    fun updateRootstock(rootstock: Rootstock) {
        viewModelScope.launch {
            val toSave = if (rootstock.firestoreId.isBlank()) {
                rootstock.copy(firestoreId = TemporalUtils.randomUUID())
            } else {
                rootstock
            }
            treeRepository.updateRootstock(toSave)
            FirestoreSync.pushRootstock(toSave, FirestoreSync.currentFarmSiteId)
        }
    }

    fun deleteRootstock(rootstock: Rootstock) {
        viewModelScope.launch {
            treeRepository.deleteRootstock(rootstock)
            FirestoreSync.deleteRootstock(rootstock, FirestoreSync.currentFarmSiteId)
        }
    }
}
