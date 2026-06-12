package br.com.pedro_araujo.coleta_de_dados.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [TelemetryEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun telemetryDao(): TelemetryDao

    companion object {
        const val DATABASE_NAME = "telemetry_db"
    }
}
