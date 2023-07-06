package com.orchardmanager.treedata.data

import android.content.Context
import com.orchardmanager.treedata.daos.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class TreeDataModule {

    @Provides
    @Singleton
    fun provideOrchardDatabase(@ApplicationContext context: Context) : OrchardDatabase {
         //val db = OrchardDatabase.buildDatabase(context)
        return OrchardDatabase.getInstance(context)
        //return db
    }

    @Provides
    fun provideFarmDao(orchardDatabase: OrchardDatabase): FarmDao {
        return orchardDatabase.farmDao()
    }

    @Provides
    fun provideOrchardDao(orchardDatabase: OrchardDatabase): OrchardDao {
        return orchardDatabase.orchardDao()
    }

    @Provides
    fun provideFarmerDao(orchardDatabase: OrchardDatabase) : FarmerDao {
        return orchardDatabase.farmerDao()
    }

    @Provides
    fun provideFarmWithOrchardsDao(orchardDatabase: OrchardDatabase): FarmWithOrchardsDao {
        return orchardDatabase.farmWithOrchardsDao()
    }

    @Provides
    fun provideFarmerWithFarmDao(orchardDatabase: OrchardDatabase): FarmerWithFarmDao {
        return orchardDatabase.farmerWithFarmDao()
    }

    @Provides
    fun provideTreeDao(orchardDatabase: OrchardDatabase): TreeDao {
        return orchardDatabase.treeDao()
    }

    @Provides
    fun provideOrchardWithTreesDao(orchardDatabase: OrchardDatabase): OrchardWithTreesDao {
        return orchardDatabase.orchardWithTreesDao()
    }

    @Provides
    fun provideRootstock(orchardDatabase: OrchardDatabase): RootstockDao {
        return orchardDatabase.rootstockDao()
    }

    @Provides
    fun provideVariety(orchardDatabase: OrchardDatabase): VarietyDao {
        return orchardDatabase.varietyDao()
    }

    @Provides
    fun provideIrrigation(orchardDatabase: OrchardDatabase): IrrigationDao {
        return orchardDatabase.irrigationDao()
    }

    @Provides
    fun provideIrrigationSystem(orchardDatabase: OrchardDatabase): IrrigationSystemDao {
        return orchardDatabase.irrigationSystemDao()
    }

    @Provides
    fun provideIrrigationSystemWithIrrigation(orchardDatabase: OrchardDatabase): IrrigationSystemWithIrrigationsDao {
        return orchardDatabase.irrigationSystemWithIrrigationsDao()
    }

    @Provides
    fun provideIrrigationSystemWithPumps(orchardDatabase: OrchardDatabase): PumpWithIrrigationSystemDao {
        return orchardDatabase.pumpWithIrrigationSystemDao()
    }

    @Provides
    fun providePump(orchardDatabase: OrchardDatabase): PumpDao {
        return orchardDatabase.pumpDao()
    }

    @Provides
    fun provideOrchardAndIrrigationSystem(orchardDatabase: OrchardDatabase): OrcahardAndIrrigationSystemDao {
        return orchardDatabase.orchardAndIrrigationSystemDao()
    }

    @Provides
    fun provideFertilizerDao(orchardDatabase: OrchardDatabase): FertilizerDao {
        return orchardDatabase.fertilizerDao()
    }

    @Provides
    fun provideFertilizerApplicationDao(orchardDatabase: OrchardDatabase): FertilizerApplicationDao {
        return orchardDatabase.fertilizerApplicationDao()
    }

    @Provides
    fun providePesticideDao(orchardDatabase: OrchardDatabase): PesticideDao {
        return orchardDatabase.pesticideDao()
    }

    @Provides
    fun providePesticideApplicationDao(orchardDatabase: OrchardDatabase): PesticideApplicationDao {
        return orchardDatabase.pesticideApplicationDao()
    }

    @Provides
    fun provideOrchardActivityDao(orchardDatabase: OrchardDatabase): OrchardActivityDao {
        return orchardDatabase.orchardActivityDao()
    }

    @Provides
    fun provideSoilMoistureDao(orchardDatabase: OrchardDatabase): SoilMoistureDao {
        return orchardDatabase.soilMoistureDao()
    }

    @Provides
    fun providePesticideApplicationWithPesticidesDao(orchardDatabase: OrchardDatabase): PesticideApplicationWithPesticidesDao {
        return orchardDatabase.pesticideApplicationWithPesticidesDao()
    }

    @Provides
    fun providesFertilizerApplicationWithFertilizersDao(orchardDatabase: OrchardDatabase): FertilizerApplicationWithFertilizersDao {
        return orchardDatabase.fertilizerApplicationWithFertilizersDao()
    }
}