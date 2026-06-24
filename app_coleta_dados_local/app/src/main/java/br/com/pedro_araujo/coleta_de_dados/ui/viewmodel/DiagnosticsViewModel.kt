package br.com.pedro_araujo.coleta_de_dados.ui.viewmodel

import androidx.lifecycle.ViewModel
import br.com.pedro_araujo.coleta_de_dados.data.repository.DiagnosticRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DiagnosticsViewModel @Inject constructor(
    private val repository: DiagnosticRepository
) : ViewModel() {
    val inferenceResult = repository.inferenceResult
    val moldRiskResult = repository.moldRiskResult
    val moldDurations = repository.moldDurations

    fun formatDuration(millis: Long) = repository.formatDuration(millis)
}
