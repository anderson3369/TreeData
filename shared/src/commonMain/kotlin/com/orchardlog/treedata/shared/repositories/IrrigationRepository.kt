package com.orchardlog.treedata.shared.repositories

import com.orchardlog.treedata.shared.daos.*
import com.orchardlog.treedata.shared.model.Irrigation
import com.orchardlog.treedata.shared.model.IrrigationSystem
import kotlin.time.Instant
import com.orchardlog.treedata.shared.model.SoilMoisture
import kotlinx.datetime.LocalDate
import kotlinx.coroutines.flow.Flow

class IrrigationRepository(
    private val irrigationDao: IrrigationDao,
    private val irrigationSystemDao: IrrigationSystemDao,
    private val irrigationSystemWithIrrigationsDao: IrrigationSystemWithIrrigationsDao,
    private val pumpWithIrrigationSystemDao: PumpWithIrrigationSystemDao,
    private val orchardAndIrrigationSystemDao: OrchardAndIrrigationSystemDao,
    private val soilMoistureDao: SoilMoistureDao
) {
    suspend fun createIrrigation(irrigation: Irrigation): Long {
        return irrigationDao.insert(irrigation)
    }

    suspend fun updateIrrigation(irrigation: Irrigation) {
        irrigationDao.update(irrigation)
    }

    suspend fun deleteIrrigation(irrigation: Irrigation) {
        irrigationDao.delete(irrigation)
    }

    suspend fun createIrrigationSystem(irrigationSystem: IrrigationSystem): Long {
        return irrigationSystemDao.insert(irrigationSystem)
    }

    suspend fun updateIrrigationSystem(irrigationSystem: IrrigationSystem) {
        irrigationSystemDao.update(irrigationSystem)
    }

    suspend fun deleteIrrigationSystem(irrigationSystem: IrrigationSystem) {
        irrigationSystemDao.delete(irrigationSystem)
    }

    suspend fun createSoilMoisture(soilMoisture: SoilMoisture): Long {
        return soilMoistureDao.insert(soilMoisture)
    }

    suspend fun updateSoilMoisture(soilMoisture: SoilMoisture) {
        soilMoistureDao.update(soilMoisture)
    }

    suspend fun deleteSoilMoisture(soilMoisture: SoilMoisture) {
        soilMoistureDao.delete(soilMoisture)
    }

    fun getSoilMoisture(): Flow<List<SoilMoisture>> = soilMoistureDao.getSoilMoisture()

    fun getIrrigations(): Flow<List<Irrigation>> = irrigationDao.getIrrigations()

    fun getIrrigationSystems(): Flow<List<IrrigationSystem>> = irrigationSystemDao.getIrrigationSystems()

    fun getIrrigationSystemWithIrrigation(orchardId: Long) =
         irrigationSystemWithIrrigationsDao.getIrrigationSystemWithIrrigation(orchardId)

    fun getPumpWithIrrigationSystem(orchardId: Long) = 
        pumpWithIrrigationSystemDao.getPumpWithIrrigationSystem(orchardId)

    fun getIrrigationsBySeason(firstYear: Instant, endYear: Instant) =
        irrigationDao.getIrrigationsBySeason(firstYear, endYear)
}
