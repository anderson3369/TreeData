package com.orchardmanager.treedata.repositories

import com.orchardmanager.treedata.daos.*
import com.orchardmanager.treedata.entities.Irrigation
import com.orchardmanager.treedata.entities.IrrigationSystem
import com.orchardmanager.treedata.entities.SoilMoisture
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

    fun getIrrigationSystemWithIrrigatioin() = irrigationSystemWithIrrigationsDao.getIrrigationSystemWithIrrigation()

    fun getPumpWithIrrigationSystem() = pumpWithIrrigationSystemDao.getPumpWithIrrigationSystem()

    fun getOrchardAndIrrigationSystem() = orchardAndIrrigationSystemDao.getOrchardAndIrrigationSystem()

    fun getIrrigationsBetween(firstYear: LocalDate, endYear: LocalDate) = irrigationDao.getIrrigations(firstYear, endYear)

}