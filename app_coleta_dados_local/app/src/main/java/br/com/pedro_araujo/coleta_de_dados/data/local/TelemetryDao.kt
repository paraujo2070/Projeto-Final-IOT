package br.com.pedro_araujo.coleta_de_dados.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

import kotlinx.coroutines.flow.Flow

@Dao
interface TelemetryDao {
    @Insert
    suspend fun insert(telemetry: TelemetryEntity)

    @Query("SELECT * FROM telemetry_outbox ORDER BY timestamp ASC LIMIT 50")
    suspend fun getPendingTelemetry(): List<TelemetryEntity>

    @Query("SELECT COUNT(*) FROM telemetry_outbox")
    fun getPendingCountFlow(): Flow<Int>

    @Query("DELETE FROM telemetry_outbox WHERE id = :id")
    suspend fun deleteById(id: Long)
}
