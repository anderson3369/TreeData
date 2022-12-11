package com.orchardmanager.treedata.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.workDataOf
import com.orchardmanager.treedata.daos.FarmDao
import com.orchardmanager.treedata.daos.FarmerDao
import com.orchardmanager.treedata.entities.*
import com.orchardmanager.treedata.utils.DATABASE_NAME
import dagger.hilt.android.qualifiers.ApplicationContext

@Database(entities = [
    Disease::class, Orchard::class, Farm::class, Farmer::class,
    FertilizerApplication::class, PesticideApplication::class,
    Pump::class, Rootstock::class, Soil::class, SoilTest::class,
    Spacing::class, Tree::class, Variety::class, IrrigationSystem::class,
    GeoLocation::class, Irrigation::class, Fertilizer::class,
                     ], version = 1, exportSchema = false)
abstract class OrchardDatabase : RoomDatabase() {
    abstract fun farmerDao():FarmerDao
    abstract fun farmDao():FarmDao

    companion object {
        @Volatile private var instance: OrchardDatabase? = null

        fun getInstance(context: Context): OrchardDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also {instance = it}
            }
        }

        private fun buildDatabase(context: Context): OrchardDatabase {
            return Room.databaseBuilder(context, OrchardDatabase::class.java, DATABASE_NAME)
                .addCallback(
                    object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            //val request = OneTimeWorkRequestBuilder<SeedDatabaseWorker>()
                            //    .setInputData(workDataOf(KEY_FILENAME, ))
                        }
                    }
                ).build()
        }


    }


}