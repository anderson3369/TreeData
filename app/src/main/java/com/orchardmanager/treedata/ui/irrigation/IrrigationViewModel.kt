package com.orchardmanager.treedata.ui.irrigation

import androidx.lifecycle.*
import com.orchardmanager.treedata.entities.Irrigation
import com.orchardmanager.treedata.entities.IrrigationSystem
import com.orchardmanager.treedata.entities.IrrigationSystemWithIrrigation
import com.orchardmanager.treedata.entities.IrrigationWithWithPump
import com.orchardmanager.treedata.entities.PumpsWithIrrigationSystem
import com.orchardmanager.treedata.entities.SoilMoisture
import com.orchardmanager.treedata.repositories.IrrigationRepository
import com.orchardmanager.treedata.ui.SAVE_FAILED
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import javax.inject.Inject


@HiltViewModel
class IrrigationViewModel @Inject constructor(private val irrigationRepository: IrrigationRepository) : ViewModel() {

    fun addIrrigation(irrigation: Irrigation) = liveData<Long> {
        try {
            val id = irrigationRepository.createIrrigation(irrigation)
            emit(id)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(SAVE_FAILED)
        }
    }

    fun updateIrrigation(irrigation: Irrigation) {
        viewModelScope.launch(Dispatchers.IO) {
            irrigationRepository.update(irrigation)
        }
    }

    fun deleteIrrigation(irrigation: Irrigation) {
        viewModelScope.launch(Dispatchers.IO) {
            irrigationRepository.delete(irrigation)
        }
    }

    fun addSoilMoisture(soilMoisture: SoilMoisture) = liveData<Long> {
        try {
            val id = irrigationRepository.createSoilMoisture(soilMoisture)
            emit(id)
        } catch(e: Exception) {
            e.printStackTrace()
            emit(SAVE_FAILED)
        }
    }

    fun updateSoilMoisture(soilMoisture: SoilMoisture) {
        viewModelScope.launch(Dispatchers.IO) {
            irrigationRepository.updateSoilMoisture(soilMoisture)
        }
    }

    fun deleteSoilMoisture(soilMoisture: SoilMoisture) {
        viewModelScope.launch(Dispatchers.IO) {
            irrigationRepository.deleteSoilMoisture(soilMoisture)
        }
    }

    fun getSoilMoisture(): LiveData<MutableList<SoilMoisture>> {
        return irrigationRepository.getSoilMoisture().asLiveData()
    }

    fun getIrrigations(): LiveData<MutableList<Irrigation>> {
        return irrigationRepository.getIrrigations().asLiveData()
    }

    fun getIrrgationsBetween(startDate: LocalDate, endDate: LocalDate): LiveData<MutableList<Irrigation>> {
        return irrigationRepository.getIrrigationsBetween(startDate, endDate).asLiveData()
    }

    fun getIrrigationsTotalHours(orchardId: Long, startDate: LocalDate, endDate: LocalDate): LiveData<Long> {
        val irrigations: LiveData<IrrigationSystemWithIrrigation> = getIrrigationSystemWithIrrigations(orchardId, startDate, endDate)

        val totalHours: LiveData<Long> = irrigations.map {
            irrigationSystemWithIrrigations ->
            var hours = 0L
            if(irrigationSystemWithIrrigations != null) {
                val list = irrigationSystemWithIrrigations.irrigations
                for(irrigation in list) {
                    val duration = Duration.between(irrigation.startTime, irrigation.stopTime)
                    hours = duration.toHours() + hours
                }
            }
            hours
        }
        return totalHours
    }

    fun addIrrigationSystem(irrigationSystem: IrrigationSystem) = liveData<Long> {
        try {
            val id = irrigationRepository.createIrrigationSystem(irrigationSystem)
            emit(id)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(SAVE_FAILED)
        }
    }

    fun updateIrrigationSystem(irrigationSystem: IrrigationSystem) {
        viewModelScope.launch(Dispatchers.IO) {
            irrigationRepository.update(irrigationSystem)
        }
    }

    fun deleteIrrigationSystem(irrigationSystem: IrrigationSystem) {
        viewModelScope.launch(Dispatchers.IO) {
            irrigationRepository.delete(irrigationSystem)
        }
    }

    fun getIrrigationSystem(): LiveData<MutableList<IrrigationSystem>> {
        return irrigationRepository.getIrrigationSystems().asLiveData()
    }

    fun getIrrigationSystemWithIrrigations(orchardId: Long, startDate: LocalDate, endDate: LocalDate): LiveData<IrrigationSystemWithIrrigation> {
        return irrigationRepository.getIrrigationSystemWithIrrigation(orchardId, startDate, endDate).asLiveData()
    }

    fun getPumpWithIrrigationSystem(orchardId: Long): LiveData<PumpsWithIrrigationSystem> {
        return irrigationRepository.getPumpWithIrrigationSystem(orchardId).asLiveData()
    }
}