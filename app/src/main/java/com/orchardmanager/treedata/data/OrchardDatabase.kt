package com.orchardmanager.treedata.data

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteTable
import androidx.room.RenameColumn
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec
import androidx.sqlite.db.SupportSQLiteDatabase
import com.orchardmanager.treedata.daos.*
import com.orchardmanager.treedata.entities.*
import com.orchardmanager.treedata.utils.DATABASE_NAME

@Database(entities = [
    Disease::class, Orchard::class, Farm::class, Farmer::class,
    FertilizerApplication::class, PesticideApplication::class,
    Pump::class, Rootstock::class, Soil::class, SoilTest::class,
    Spacing::class, Tree::class, Variety::class, IrrigationSystem::class,
    Irrigation::class, Fertilizer::class,
                     ],
    version = 3,
    //autoMigrations = [
    //    AutoMigration (
    //        from = 2,
    //        to = 3,
    //        spec = OrchardDatabase.ODBAutoMigration::class
    //    )
    //],
    exportSchema = false)

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

    //@DeleteTable.Entries(DeleteTable(tableName = "Tree"))
    //class ODBAutoMigration: AutoMigrationSpec

    companion object {
        @Volatile private var instance: OrchardDatabase? = null

        fun getInstance(context: Context): OrchardDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also {instance = it}
            }
        }

        fun buildDatabase(context: Context): OrchardDatabase {
            return Room.databaseBuilder(context, OrchardDatabase::class.java, DATABASE_NAME)
                //.fallbackToDestructiveMigrationFrom(1,2,3)
                .build()
        }

    }

}