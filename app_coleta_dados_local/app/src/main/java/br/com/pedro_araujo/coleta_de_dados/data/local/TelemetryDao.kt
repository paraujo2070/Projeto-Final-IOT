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

    // Mold Risk
    @Insert
    suspend fun insertMoldRisk(risk: MoldRiskEntity)

    @Query("SELECT * FROM mold_risk_history WHERE timestamp > :since ORDER BY timestamp ASC")
    suspend fun getMoldRiskHistory(since: Long): List<MoldRiskEntity>

    @Query("SELECT * FROM mold_risk_history WHERE timestamp <= :since ORDER BY timestamp DESC LIMIT 1")
    suspend fun getStateBefore(since: Long): MoldRiskEntity?

    @Query("SELECT * FROM mold_risk_history ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestMoldRisk(): MoldRiskEntity?

    @Query("DELETE FROM mold_risk_history WHERE timestamp < :before")
    suspend fun deleteOldMoldRisk(before: Long)
}
