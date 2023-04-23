package com.orchardmanager.treedata.ui.pesticide

import androidx.lifecycle.*
import com.orchardmanager.treedata.entities.Pesticide
import com.orchardmanager.treedata.entities.PesticideApplication
import com.orchardmanager.treedata.repositories.PesticideRepository
import com.orchardmanager.treedata.ui.SAVE_FAILED
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PesticideViewModel @Inject constructor(private val pesticideRepository: PesticideRepository) : ViewModel() {

    fun addPesticide(pesticide: Pesticide) = liveData<Long> {
        try {
            val id = pesticideRepository.createPesticide(pesticide)
            emit(id)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(SAVE_FAILED)
        }
    }

    fun addPesticideApplication(pesticideApplication: PesticideApplication) = liveData<Long> {
        try {
            val id = pesticideRepository.createPesticideApplication(pesticideApplication)
            emit(id)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(SAVE_FAILED)
        }
    }

    fun updatePesticide(pesticide: Pesticide) {
        viewModelScope.launch(Dispatchers.IO) {
            pesticideRepository.updatePesticide(pesticide)
        }
    }

    fun updatePesticideApplication(pesticideApplication: PesticideApplication) {
        viewModelScope.launch(Dispatchers.IO) {
            pesticideRepository.updatePesticideApplication(pesticideApplication)
        }
    }

    fun deletePesticide(pesticide: Pesticide) {
        viewModelScope.launch(Dispatchers.IO) {
            pesticideRepository.deletePesticide(pesticide)
        }
    }

    fun deletePesticideApplication(pesticideApplication: PesticideApplication) {
        viewModelScope.launch(Dispatchers.IO) {
            pesticideRepository.deletePesticideApplication(pesticideApplication)
        }
    }

    fun getPesticides(): LiveData<MutableList<Pesticide>> {
        return pesticideRepository.getPesticides().asLiveData()
    }

    fun getPesticideApplications(): LiveData<MutableList<PesticideApplication>> {
        return pesticideRepository.getPesticideApplications().asLiveData()
    }
}