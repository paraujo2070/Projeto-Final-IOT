package br.com.pedro_araujo.coleta_de_dados.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import br.com.pedro_araujo.coleta_de_dados.R
import br.com.pedro_araujo.coleta_de_dados.hardware.SenseHatController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SensorReadingsFragment : Fragment(R.layout.fragment_sensors) {

    @Inject lateinit var senseHatController: SenseHatController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startReadings(view)
    }

    private fun startReadings(view: View) {
        val tvTemp = view.findViewById<TextView>(R.id.tvTemperature)
        val tvHum = view.findViewById<TextView>(R.id.tvHumidity)
        val tvPress = view.findViewById<TextView>(R.id.tvPressure)
        val tvAx = view.findViewById<TextView>(R.id.tvAccelX)
        val tvAy = view.findViewById<TextView>(R.id.tvAccelY)
        val tvAz = view.findViewById<TextView>(R.id.tvAccelZ)
        val tvGx = view.findViewById<TextView>(R.id.tvGyroX)
        val tvGy = view.findViewById<TextView>(R.id.tvGyroY)
        val tvGz = view.findViewById<TextView>(R.id.tvGyroZ)

        viewLifecycleOwner.lifecycleScope.launch {
            while (isActive) {
                val climate = senseHatController.getClimateData()
                val pressure = senseHatController.getPressureData()
                val inertial = senseHatController.getInertialData()

                tvTemp.text = "Temperatura: ${climate?.temperatureC ?: "--"} °C"
                tvHum.text = "Umidade: ${climate?.humidityPct ?: "--"} %"
                tvPress.text = "Pressão: ${pressure?.barometerHpa ?: "--"} hPa"
                
                tvAx.text = "X: ${inertial?.accelX ?: "--"}"
                tvAy.text = "Y: ${inertial?.accelY ?: "--"}"
                tvAz.text = "Z: ${inertial?.accelZ ?: "--"}"
                
                tvGx.text = "X: ${inertial?.gyroX ?: "--"}"
                tvGy.text = "Y: ${inertial?.gyroY ?: "--"}"
                tvGz.text = "Z: ${inertial?.gyroZ ?: "--"}"

                delay(2000)
            }
        }
    }
}
