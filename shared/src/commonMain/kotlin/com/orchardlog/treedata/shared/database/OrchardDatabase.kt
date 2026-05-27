package com.orchardlog.treedata.shared.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import com.orchardlog.treedata.shared.daos.*
import com.orchardlog.treedata.shared.model.*

@Database(entities = [
    Disease::class, Orchard::class, Farm::class, Farmer::class,
    FertilizerApplication::class, PesticideApplication::class,
    PesticideApplicationItem::class, FertilizerApplicationItem::class,
    Pump::class, Rootstock::class,  SoilTest::class, Pesticide::class,
    Tree::class, Variety::class, IrrigationSystem::class,
    Irrigation::class, Fertilizer::class, OrchardActivity::class,
    SoilMoisture::class
],
    version = 10
)
@TypeConverters(DateConverter::class, EnumConverter::class)
@ConstructedBy(OrchardDatabaseConstructor::class)
abstract class OrchardDatabase : RoomDatabase() {
    abstract fun farmerDao(): FarmerDao
    abstract fun farmDao(): FarmDao
    abstract fun orchardDao(): OrchardDao
    abstract fun farmWithOrchardsDao(): FarmWithOrchardsDao
    abstract fun farmerWithFarmDao(): FarmerWithFarmDao
    abstract fun treeDao(): TreeDao
    abstract fun orchardWithTreesDao(): OrchardWithTreesDao
    abstract fun rootstockDao(): RootstockDao
    abstract fun varietyDao(): VarietyDao
    abstract fun irrigationDao(): IrrigationDao
    abstract fun irrigationSystemDao(): IrrigationSystemDao
    abstract fun irrigationSystemWithIrrigationsDao(): IrrigationSystemWithIrrigationsDao
    abstract fun pumpWithIrrigationSystemDao(): PumpWithIrrigationSystemDao
    abstract fun pumpDao(): PumpDao
    abstract fun orchardAndIrrigationSystemDao(): OrchardAndIrrigationSystemDao
    abstract fun fertilizerDao(): FertilizerDao
    abstract fun fertilizerApplicationDao(): FertilizerApplicationDao
    abstract fun fertilizerApplicationItemDao(): FertilizerApplicationItemDao
    abstract fun pesticideDao(): PesticideDao
    abstract fun pesticideApplicationDao(): PesticideApplicationDao
    abstract fun orchardActivityDao(): OrchardActivityDao
    abstract fun soilMoistureDao(): SoilMoistureDao
    abstract fun pesticideApplicationWithPesticidesDao(): PesticideApplicationWithPesticidesDao
    abstract fun fertilizerApplicationWithFertilizersDao(): FertilizerApplicationWithFertilizersDao
    abstract fun orchardWithOrchardActivitiesDao(): OrchardWithOrchardActivitiesDao
    abstract fun farmWithOrchardsWithOrchardActivities(): FarmWithOrchardsWithOrchardActivitiesDao
}

expect object OrchardDatabaseConstructor : RoomDatabaseConstructor<OrchardDatabase>
