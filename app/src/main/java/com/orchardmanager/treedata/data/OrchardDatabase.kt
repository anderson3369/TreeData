package com.orchardmanager.treedata.data

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RenameColumn
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec
import androidx.sqlite.db.SupportSQLiteDatabase
import com.orchardmanager.treedata.daos.*
import com.orchardmanager.treedata.entities.*
import com.orchardmanager.treedata.entities.GeoLocation
import com.orchardmanager.treedata.utils.DATABASE_NAME

@Database(entities = [
    Disease::class, Orchard::class, Farm::class, Farmer::class,
    FertilizerApplication::class, PesticideApplication::class,
    Pump::class, Rootstock::class, Soil::class, SoilTest::class,
    Spacing::class, Tree::class, Variety::class, IrrigationSystem::class,
    GeoLocation::class, Irrigation::class, Fertilizer::class,
                     ],
    version = 2,
    exportSchema = false)
abstract class OrchardDatabase : RoomDatabase() {
    abstract fun farmerDao(): FarmerDao
    abstract fun farmDao(): FarmDao
    abstract fun orchardDao(): OrchardDao
    abstract fun farmWithOrchardsDao(): FarmWithOrchardsDao
    abstract fun farmerWithFarmDao(): FarmerWithFarmDao


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
                .addCallback(
                    object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            //val request = OneTimeWorkRequestBuilder<SeedDatabaseWorker>()
                            //    .setInputData(workDataOf(KEY_FILENAME, ))
                        }
                    }
                )
            .build()
        }


    }


}