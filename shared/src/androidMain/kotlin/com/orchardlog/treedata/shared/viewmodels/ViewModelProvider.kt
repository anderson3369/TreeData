package com.orchardlog.treedata.shared.viewmodels

import android.content.Context
import androidx.room.Room
import com.orchardlog.treedata.shared.database.OrchardDatabase
import com.orchardlog.treedata.shared.database.getRoomDatabase
import com.orchardlog.treedata.shared.repositories.*

/**
 * Android implementation of the ViewModelProvider.
 * In a full production app, you might use Hilt/Dagger, but for KMP consistency,
 * we'll provide a way to initialize the shared ViewModels.
 */
object ViewModelProvider {
    private var database: OrchardDatabase? = null

    fun initialize(context: Context) {
        if (database == null) {
            val dbFile = context.getDatabasePath("orchard.db")
            val builder = Room.databaseBuilder<OrchardDatabase>(
                context = context,
                name = dbFile.absolutePath
            )
            database = getRoomDatabase(builder)
        }
    }

    fun getDb(): OrchardDatabase {
        return database ?: throw IllegalStateException("ViewModelProvider must be initialized with context first")
    }

    // Lazy repositories
    private val farmerRepository by lazy { FarmerRepository(getDb().farmerDao()) }
    private val farmRepository by lazy {
        FarmRepository(
            getDb().farmDao(),
            getDb().farmerDao(),
            getDb().farmerWithFarmDao()
        )
    }
    private val orchardRepository by lazy {
        OrchardRepository(
            getDb().orchardDao(),
            getDb().orchardActivityDao(),
            getDb().farmWithOrchardsDao(),
            getDb().orchardWithOrchardActivitiesDao(),
            getDb().farmWithOrchardsWithOrchardActivities()
        )
    }
    private val irrigationRepository by lazy {
        IrrigationRepository(
            getDb().irrigationDao(),
            getDb().irrigationSystemDao(),
            getDb().irrigationSystemWithIrrigationsDao(),
            getDb().pumpWithIrrigationSystemDao(),
            getDb().orchardAndIrrigationSystemDao(),
            getDb().soilMoistureDao()
        )
    }

    private val fertilizerRepository by lazy {
        FertilizerRepository(
            getDb().fertilizerDao(),
            getDb().fertilizerApplicationDao(),
            getDb().fertilizerApplicationItemDao(),
            getDb().fertilizerApplicationWithFertilizersDao()
        )
    }

    private val pesticideRepository by lazy {
        PesticideRepository(
            getDb().pesticideDao(),
            getDb().pesticideApplicationDao(),
            getDb().pesticideApplicationWithPesticidesDao()
        )
    }

    private val pumpRepository by lazy {
        PumpRepository(getDb().pumpDao())
    }

    private val treeRepository by lazy {
        TreeRepository(
            getDb().treeDao(),
            getDb().orchardWithTreesDao(),
            getDb().rootstockDao(),
            getDb().varietyDao()
        )
    }

    val farmerViewModel by lazy { FarmerViewModel(farmerRepository) }
    val farmViewModel by lazy { FarmViewModel(farmRepository) }
    val orchardViewModel by lazy { OrchardViewModel(orchardRepository) }
    val irrigationViewModel by lazy { IrrigationViewModel(irrigationRepository) }
    val fertilizerViewModel by lazy { FertilizerViewModel(fertilizerRepository) }
    val pesticideViewModel by lazy { PesticideViewModel(pesticideRepository) }
    val pumpViewModel by lazy { PumpViewModel(pumpRepository) }
    val treeViewModel by lazy { TreeViewModel(treeRepository) }
}
