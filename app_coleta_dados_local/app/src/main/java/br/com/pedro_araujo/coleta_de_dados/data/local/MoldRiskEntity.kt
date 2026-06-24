package br.com.pedro_araujo.coleta_de_dados.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mold_risk_history")
data class MoldRiskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val state: String, // "Seguro", "Alerta", "Critico"
    val temperature: Double,
    val humidity: Double,
    val timestamp: Long
)
