package com.orchardlog.treedata.shared.viewmodels

import com.orchardlog.treedata.shared.database.DatabaseManager
import com.orchardlog.treedata.shared.repositories.*

/**
 * A singleton provider for ViewModels to be consumed by iOS (SwiftUI).
 * It handles the manual dependency injection of repositories.
 */
object ViewModelProvider {

    private val database by lazy { DatabaseManager.getDatabase() }

    // Lazy repositories to ensure they are only created once
    private val farmRepository by lazy { 
        FarmRepository(database.farmDao(), database.farmerDao(), database.farmerWithFarmDao()) 
    }
    
    private val farmerRepository by lazy { 
        FarmerRepository(database.farmerDao()) 
    }
    
    private val orchardRepository by lazy { 
        OrchardRepository(
            database.orchardDao(),
            database.orchardActivityDao(),
            database.farmWithOrchardsDao(),
            database.orchardWithOrchardActivitiesDao(),
            database.farmWithOrchardsWithOrchardActivities()
        )
    }
    
    private val treeRepository by lazy {
        TreeRepository(
            database.treeDao(),
            database.orchardWithTreesDao(),
            database.rootstockDao(),
            database.varietyDao()
        )
    }

    private val fertilizerRepository by lazy {
        FertilizerRepository(
            database.fertilizerDao(),
            database.fertilizerApplicationDao(),
            database.fertilizerApplicationItemDao(),
            database.fertilizerApplicationWithFertilizersDao()
        )
    }

    private val pesticideRepository by lazy {
        PesticideRepository(
            database.pesticideDao(),
            database.pesticideApplicationDao(),
            database.pesticideApplicationWithPesticidesDao()
        )
    }

    private val irrigationRepository by lazy {
        IrrigationRepository(
            database.irrigationDao(),
            database.irrigationSystemDao(),
            database.irrigationSystemWithIrrigationsDao(),
            database.pumpWithIrrigationSystemDao(),
            database.orchardAndIrrigationSystemDao(),
            database.soilMoistureDao()
        )
    }

    private val pumpRepository by lazy {
        PumpRepository(database.pumpDao())
    }

    // Singleton ViewModel instances
    val farmViewModel by lazy { FarmViewModel(farmRepository) }
    val farmerViewModel by lazy { FarmerViewModel(farmerRepository) }
    val orchardViewModel by lazy { OrchardViewModel(orchardRepository) }
    val treeViewModel by lazy { TreeViewModel(treeRepository) }
    val fertilizerViewModel by lazy { FertilizerViewModel(fertilizerRepository) }
    val pesticideViewModel by lazy { PesticideViewModel(pesticideRepository) }
    val irrigationViewModel by lazy { IrrigationViewModel(irrigationRepository) }
    val pumpViewModel by lazy { PumpViewModel(pumpRepository) }
}
