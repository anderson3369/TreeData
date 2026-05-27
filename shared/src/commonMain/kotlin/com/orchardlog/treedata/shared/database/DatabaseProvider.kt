package com.orchardlog.treedata.shared.database

import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import com.orchardlog.treedata.shared.TemporalUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

/**
 * Common configuration for the OrchardDatabase across all platforms.
 */
fun getRoomDatabase(
    builder: RoomDatabase.Builder<OrchardDatabase>
): OrchardDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .addMigrations(MIGRATION_3_4, MIGRATION_5_6, MIGRATION_6_7)
        // Destructive fallback for any other version gaps recreates the schema cleanly.
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()
}

/**
 * Migration from version 3 to 4:
 * Adds temporal fields (persistentId, validFrom, validTo) to Farm, Orchard, and Tree tables.
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(connection: SQLiteConnection) {
        val now = DateConverter.fromInstant(TemporalUtils.now())

        // Update Farm table
        connection.execSQL("ALTER TABLE Farm ADD COLUMN persistentId TEXT NOT NULL DEFAULT ''")
        connection.execSQL("ALTER TABLE Farm ADD COLUMN validFrom INTEGER NOT NULL DEFAULT $now")
        connection.execSQL("ALTER TABLE Farm ADD COLUMN validTo INTEGER")

        // Update Orchard table
        connection.execSQL("ALTER TABLE Orchard ADD COLUMN persistentId TEXT NOT NULL DEFAULT ''")
        connection.execSQL("ALTER TABLE Orchard ADD COLUMN validFrom INTEGER NOT NULL DEFAULT $now")
        connection.execSQL("ALTER TABLE Orchard ADD COLUMN validTo INTEGER")

        // Update Tree table
        connection.execSQL("ALTER TABLE Tree ADD COLUMN persistentId TEXT NOT NULL DEFAULT ''")
        connection.execSQL("ALTER TABLE Tree ADD COLUMN validFrom INTEGER NOT NULL DEFAULT $now")
        connection.execSQL("ALTER TABLE Tree ADD COLUMN validTo INTEGER")
    }
}

/**
 * Migration from version 5 to 6:
 * Adds firestoreId column to OrchardActivity for Firestore sync.
 */
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE OrchardActivity ADD COLUMN firestoreId TEXT NOT NULL DEFAULT ''")
        connection.execSQL("ALTER TABLE Farmer ADD COLUMN persistentId TEXT NOT NULL DEFAULT ''")
    }
}

/**
 * Migration from version 6 to 7:
 * Adds firestoreId column to remaining entities for Firestore sync.
 */
val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE Irrigation ADD COLUMN firestoreId TEXT NOT NULL DEFAULT ''")
        connection.execSQL("ALTER TABLE IrrigationSystem ADD COLUMN firestoreId TEXT NOT NULL DEFAULT ''")
        connection.execSQL("ALTER TABLE FertilizerApplication ADD COLUMN firestoreId TEXT NOT NULL DEFAULT ''")
        connection.execSQL("ALTER TABLE Fertilizer ADD COLUMN firestoreId TEXT NOT NULL DEFAULT ''")
        connection.execSQL("ALTER TABLE PesticideApplication ADD COLUMN firestoreId TEXT NOT NULL DEFAULT ''")
        connection.execSQL("ALTER TABLE Pesticide ADD COLUMN firestoreId TEXT NOT NULL DEFAULT ''")
        connection.execSQL("ALTER TABLE Pump ADD COLUMN firestoreId TEXT NOT NULL DEFAULT ''")
        connection.execSQL("ALTER TABLE SoilMoisture ADD COLUMN firestoreId TEXT NOT NULL DEFAULT ''")
        connection.execSQL("ALTER TABLE Variety ADD COLUMN firestoreId TEXT NOT NULL DEFAULT ''")
        connection.execSQL("ALTER TABLE Rootstock ADD COLUMN firestoreId TEXT NOT NULL DEFAULT ''")
        connection.execSQL("ALTER TABLE Disease ADD COLUMN firestoreId TEXT NOT NULL DEFAULT ''")
        connection.execSQL("ALTER TABLE SoilTest ADD COLUMN firestoreId TEXT NOT NULL DEFAULT ''")
    }
}
