package br.com.pedro_araujo.coleta_de_dados.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import br.com.pedro_araujo.coleta_de_dados.R
import br.com.pedro_araujo.coleta_de_dados.ui.viewmodel.DiagnosticsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DiagnosticsFragment : Fragment(R.layout.fragment_diagnostics) {

    private val viewModel: DiagnosticsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val tvInference = view.findViewById<TextView>(R.id.tvInferenceResult)
        val tvMoldRisk = view.findViewById<TextView>(R.id.tvMoldRisk)
        val tvDurationSeguro = view.findViewById<TextView>(R.id.tvDurationSeguro)
        val tvDurationAlerta = view.findViewById<TextView>(R.id.tvDurationAlerta)
        val tvDurationCritico = view.findViewById<TextView>(R.id.tvDurationCritico)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.inferenceResult.collectLatest {
                tvInference.text = it
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.moldRiskResult.collectLatest {
                tvMoldRisk.text = it
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.moldDurations.collectLatest { durations ->
                tvDurationSeguro.text = "Seguro: ${viewModel.formatDuration(durations["Seguro"] ?: 0L)}"
                tvDurationAlerta.text = "Alerta: ${viewModel.formatDuration(durations["Alerta"] ?: 0L)}"
                tvDurationCritico.text = "Critico: ${viewModel.formatDuration(durations["Critico"] ?: 0L)}"
            }
        }
    }
}
