package com.orchardlog.treedata.ui.trees

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.orchardlog.treedata.entities.Rootstock
import com.orchardlog.treedata.entities.Tree
import com.orchardlog.treedata.entities.Variety
import com.orchardlog.treedata.repositories.TreeRepository
import com.orchardlog.treedata.ui.SAVE_FAILED
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TreeViewModel @Inject constructor(private val treeRepository: TreeRepository): ViewModel() {

    fun add(tree: Tree) = liveData<Long> {
        try {
            val id = treeRepository.createTree(tree)
            emit(id)
        } catch(e: Exception) {
            e.printStackTrace()
            emit(SAVE_FAILED)
        }
    }

    fun update(tree: Tree) {
        viewModelScope.launch(Dispatchers.IO) {
            treeRepository.updateTree(tree)
        }
    }

    fun delete(tree: Tree) {
        viewModelScope.launch(Dispatchers.IO) {
            treeRepository.deleteTree(tree)
        }
    }

    fun getAllTrees(): LiveData<MutableList<Tree>> {
        return treeRepository.getAllTrees().asLiveData()
    }

    fun getTree(id: Long): LiveData<Tree> {
        return treeRepository.getTree(id).asLiveData()
    }

    fun add(rootstock: Rootstock) = liveData<Long> {
        try {
            val id = treeRepository.createRootstock(rootstock)
            emit(id)
        } catch(e: Exception) {
            e.printStackTrace()
            emit(-1000L)
        }
    }

    fun update(rootstock: Rootstock) {
        viewModelScope.launch(Dispatchers.IO) {
            treeRepository.updateRootstock(rootstock)
        }
    }

    fun delete(rootstock: Rootstock) {
        viewModelScope.launch(Dispatchers.IO) {
            treeRepository.deleteRootstock(rootstock)
        }
    }

    fun getAllRootstocks(): LiveData<MutableList<Rootstock>> {
        return treeRepository.getAllRootstocks().asLiveData()
    }

    fun getRootstocksMap(): LiveData<Map<Long, String>> {
        return treeRepository.getRootstocksMap().asLiveData()
    }

    fun add(variety: Variety) = liveData<Long> {
        try {
            val id = treeRepository.createVariety(variety)
            emit(id)
        } catch(e: Exception) {
            e.printStackTrace()
            emit(-1000L)
        }
    }

    fun update(variety: Variety) {
        viewModelScope.launch(Dispatchers.IO) {
            treeRepository.updatevVariety(variety)
        }
    }

    fun delete(variety: Variety) {
        viewModelScope.launch(Dispatchers.IO) {
            treeRepository.deleteVariety(variety)
        }
    }

    fun getAllVarieties(): LiveData<MutableList<Variety>> {
        return treeRepository.getAllVarieties().asLiveData()
    }

    fun getVarietiesMap(): LiveData<Map<Long, String>> {
        return treeRepository.getVarietiesMap().asLiveData()
    }
}