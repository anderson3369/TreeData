package com.orchardlog.treedata.repositories

import com.orchardlog.treedata.daos.*
import com.orchardlog.treedata.entities.Irrigation
import com.orchardlog.treedata.entities.IrrigationSystem
import com.orchardlog.treedata.entities.SoilMoisture
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IrrigationRepository @Inject constructor(private val irrigationDao: IrrigationDao,
                                               private val irrigationSystemDao: IrrigationSystemDao,
                                               private val irrigationSystemWithIrrigationsDao: IrrigationSystemWithIrrigationsDao,
                                               private val pumpWithIrrigationSystemDao: PumpWithIrrigationSystemDao,
                                               private val orchardAndIrrigationSystemDao: OrcahardAndIrrigationSystemDao,
                                               private val soilMoistureDao: SoilMoistureDao
) {
    suspend fun createIrrigation(irrigation: Irrigation): Long {
        return irrigationDao.insert(irrigation)
    }

    fun update(irrigation: Irrigation) {
        irrigationDao.update(irrigation)
    }

    fun delete(irrigation: Irrigation) {
        irrigationDao.delete(irrigation)
    }

    fun getIrrigations() = irrigationDao.getIrrigations()

    suspend fun createIrrigationSystem(irrigationSystem: IrrigationSystem): Long {
        return irrigationSystemDao.insert(irrigationSystem)
    }

    fun update(irrigationSystem: IrrigationSystem) {
        irrigationSystemDao.update(irrigationSystem)
    }

    fun delete(irrigationSystem: IrrigationSystem) {
        irrigationSystemDao.delete(irrigationSystem)
    }

    suspend fun createSoilMoisture(soilMoisture: SoilMoisture): Long {
        return soilMoistureDao.insert(soilMoisture)
    }

    fun updateSoilMoisture(soilMoisture: SoilMoisture) {
        soilMoistureDao.update(soilMoisture)
    }

    fun deleteSoilMoisture(soilMoisture: SoilMoisture) {
        soilMoistureDao.delete(soilMoisture)
    }

    fun getSoilMoisture() = soilMoistureDao.getSoilMoisture()

    fun getIrrigationSystems() = irrigationSystemDao.getIrrigationSystems()

    fun getIrrigationSystemWithIrrigation(orchardId: Long, startTime: LocalDate, endTime: LocalDate) =
         irrigationSystemWithIrrigationsDao.getIrrigationSystemWithIrrigation(orchardId, startTime, endTime)

    fun getPumpWithIrrigationSystem(orchardId: Long) = pumpWithIrrigationSystemDao.getPumpWithIrrigationSystem(orchardId)

    fun getOrchardAndIrrigationSystem() = orchardAndIrrigationSystemDao.getOrchardAndIrrigationSystem()

    fun getIrrigationsBetween(firstYear: LocalDate, endYear: LocalDate) = irrigationDao.getIrrigations(firstYear, endYear)


}