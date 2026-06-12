package br.com.pedro_araujo.coleta_de_dados.ui.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import br.com.pedro_araujo.coleta_de_dados.R
import br.com.pedro_araujo.coleta_de_dados.data.local.TelemetryDao
import br.com.pedro_araujo.coleta_de_dados.service.TelemetryService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CollectionFragment : Fragment(R.layout.fragment_collection) {

    @Inject lateinit var telemetryDao: TelemetryDao

    private var isServiceRunning = false
    private var totalCyclesCount = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val btnStartStop = view.findViewById<Button>(R.id.btnStartStop)
        val tvStatus = view.findViewById<TextView>(R.id.tvConnectionStatus)
        val tvTotal = view.findViewById<TextView>(R.id.tvTotalCycles)
        val tvPending = view.findViewById<TextView>(R.id.tvPendingCycles)

        val prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        isServiceRunning = prefs.getBoolean("service_enabled", false)
        totalCyclesCount = prefs.getInt("total_cycles_session", 0)

        tvTotal.text = totalCyclesCount.toString()

        updateButtonState(btnStartStop)
        
        if (isServiceRunning) {
            startTelemetryService()
        }

        btnStartStop.setOnClickListener {
            if (isServiceRunning) {
                stopTelemetryService()
                isServiceRunning = false
            } else {
                startTelemetryService()
                isServiceRunning = true
            }
            prefs.edit().putBoolean("service_enabled", isServiceRunning).apply()
            updateButtonState(btnStartStop)
        }

        // Monitorar ciclos pendentes no BD (Real-time Flow)
        viewLifecycleOwner.lifecycleScope.launch {
            telemetryDao.getPendingCountFlow().collect { count ->
                tvPending.text = count.toString()
            }
        }

        // Monitorar Total e Status da Conexão
        viewLifecycleOwner.lifecycleScope.launch {
            while (isActive) {
                val currentTotal = prefs.getInt("total_cycles_session", 0)
                tvTotal.text = currentTotal.toString()

                val broker = prefs.getString("broker_url", "") ?: ""
                if (broker.isEmpty()) {
                    tvStatus.text = "Servidor: Não configurado"
                    tvStatus.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
                } else {
                    // Aqui futuramente checaremos o status real do MqttPublisher
                    tvStatus.text = "Servidor: Pronto (Aguardando Broker)"
                    tvStatus.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_orange_dark))
                }
                delay(2000)
            }
        }
    }

    private fun updateButtonState(button: Button) {
        if (isServiceRunning) {
            button.text = "Parar Coleta"
            button.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_light))
        } else {
            button.text = "Iniciar Coleta"
            button.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark))
        }
    }

    private fun startTelemetryService() {
        val intent = Intent(requireContext(), TelemetryService::class.java)
        requireContext().startService(intent)
    }

    private fun stopTelemetryService() {
        val intent = Intent(requireContext(), TelemetryService::class.java)
        requireContext().stopService(intent)
    }
}
