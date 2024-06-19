package com.orchardlog.treedata.ui.pesticide

import androidx.lifecycle.*
import com.orchardlog.treedata.entities.Pesticide
import com.orchardlog.treedata.entities.PesticideApplication
import com.orchardlog.treedata.entities.PesticideApplicationWithPesticides
import com.orchardlog.treedata.repositories.PesticideRepository
import com.orchardlog.treedata.utils.SAVE_FAILED
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class PesticideViewModel @Inject constructor(private val pesticideRepository: PesticideRepository) : ViewModel() {

    fun addPesticide(pesticide: Pesticide) = liveData {
        try {
            val id = pesticideRepository.createPesticide(pesticide)
            emit(id)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(SAVE_FAILED)
        }
    }

    fun addPesticideApplication(pesticideApplication: PesticideApplication) = liveData {
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

    fun getPesticideApplicationWithPesticides(orchardId: Long, startDate: LocalDate, endDate: LocalDate):
            LiveData<MutableList<PesticideApplicationWithPesticides>> {
        return pesticideRepository.getPesticideApplicationWithPesticides(orchardId, startDate, endDate).asLiveData()
    }
}