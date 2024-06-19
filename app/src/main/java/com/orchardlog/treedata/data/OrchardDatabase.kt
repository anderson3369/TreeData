package com.orchardlog.treedata.data

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.orchardlog.treedata.daos.FarmDao
import com.orchardlog.treedata.daos.FarmWithOrchardsDao
import com.orchardlog.treedata.daos.FarmWithOrchardsWithOrchardActivitiesDao
import com.orchardlog.treedata.daos.FarmerDao
import com.orchardlog.treedata.daos.FarmerWithFarmDao
import com.orchardlog.treedata.daos.FertilizerApplicationDao
import com.orchardlog.treedata.daos.FertilizerApplicationWithFertilizersDao
import com.orchardlog.treedata.daos.FertilizerDao
import com.orchardlog.treedata.daos.IrrigationDao
import com.orchardlog.treedata.daos.IrrigationSystemDao
import com.orchardlog.treedata.daos.IrrigationSystemWithIrrigationsDao
import com.orchardlog.treedata.daos.OrcahardAndIrrigationSystemDao
import com.orchardlog.treedata.daos.OrchardActivityDao
import com.orchardlog.treedata.daos.OrchardDao
import com.orchardlog.treedata.daos.OrchardWithOrchardActivitiesDao
import com.orchardlog.treedata.daos.OrchardWithTreesDao
import com.orchardlog.treedata.daos.PesticideApplicationDao
import com.orchardlog.treedata.daos.PesticideApplicationWithPesticidesDao
import com.orchardlog.treedata.daos.PesticideDao
import com.orchardlog.treedata.daos.PumpDao
import com.orchardlog.treedata.daos.PumpWithIrrigationSystemDao
import com.orchardlog.treedata.daos.RootstockDao
import com.orchardlog.treedata.daos.SoilMoistureDao
import com.orchardlog.treedata.daos.TreeDao
import com.orchardlog.treedata.daos.VarietyDao
import com.orchardlog.treedata.entities.Disease
import com.orchardlog.treedata.entities.Farm
import com.orchardlog.treedata.entities.Farmer
import com.orchardlog.treedata.entities.Fertilizer
import com.orchardlog.treedata.entities.FertilizerApplication
import com.orchardlog.treedata.entities.Irrigation
import com.orchardlog.treedata.entities.IrrigationSystem
import com.orchardlog.treedata.entities.Orchard
import com.orchardlog.treedata.entities.OrchardActivity
import com.orchardlog.treedata.entities.Pesticide
import com.orchardlog.treedata.entities.PesticideApplication
import com.orchardlog.treedata.entities.Pump
import com.orchardlog.treedata.entities.Rootstock
import com.orchardlog.treedata.entities.SoilMoisture
import com.orchardlog.treedata.entities.SoilTest
import com.orchardlog.treedata.entities.Tree
import com.orchardlog.treedata.entities.Variety
import com.orchardlog.treedata.utils.DATABASE_NAME

@Database(entities = [
    Disease::class, Orchard::class, Farm::class, Farmer::class,
    FertilizerApplication::class, PesticideApplication::class,
    Pump::class, Rootstock::class,  SoilTest::class, Pesticide::class,
    Tree::class, Variety::class, IrrigationSystem::class,
    Irrigation::class, Fertilizer::class, OrchardActivity::class,
    SoilMoisture::class
                     ],
    version = 5,

    autoMigrations = [
        AutoMigration (
            from = 4,
            to = 5
        )
    ],
    exportSchema = true)
@TypeConverters(DateConverter::class, EnumConverter::class)
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
    abstract fun orchardAndIrrigationSystemDao(): OrcahardAndIrrigationSystemDao
    abstract fun fertilizerDao(): FertilizerDao
    abstract fun fertilizerApplicationDao(): FertilizerApplicationDao
    abstract fun pesticideDao(): PesticideDao
    abstract fun pesticideApplicationDao(): PesticideApplicationDao
    abstract fun orchardActivityDao(): OrchardActivityDao
    abstract fun soilMoistureDao(): SoilMoistureDao
    abstract fun pesticideApplicationWithPesticidesDao(): PesticideApplicationWithPesticidesDao
    abstract fun fertilizerApplicationWithFertilizersDao(): FertilizerApplicationWithFertilizersDao
    abstract fun orchardWithOrchardActivitiesDao(): OrchardWithOrchardActivitiesDao
    abstract fun farmWithOrchardsWithOrchardActivities(): FarmWithOrchardsWithOrchardActivitiesDao

    //@DeleteTable.Entries(DeleteTable(tableName = "Tree"))
    //class ODBAutoMigration: AutoMigrationSpec

    companion object {
        @Volatile private var instance: OrchardDatabase? = null

        fun getInstance(context: Context): OrchardDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also {instance = it}
            }
        }

        private fun buildDatabase(context: Context): OrchardDatabase {
            return Room.databaseBuilder(context, OrchardDatabase::class.java, DATABASE_NAME)
                //.fallbackToDestructiveMigration()
                .build()
        }

    }

}