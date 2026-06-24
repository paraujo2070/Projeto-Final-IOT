package br.com.pedro_araujo.coleta_de_dados.ui.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import br.com.pedro_araujo.coleta_de_dados.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SetupFragment : Fragment(R.layout.fragment_setup) {

    private lateinit var encryptedPrefs: SharedPreferences
    private lateinit var normalPrefs: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupPrefs()
        setupUI(view)
    }

    private fun setupPrefs() {
        val masterKey = MasterKey.Builder(requireContext())
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        encryptedPrefs = EncryptedSharedPreferences.create(
            requireContext(),
            "secret_shared_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        normalPrefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    }

    private fun setupUI(view: View) {
        val etDeviceId = view.findViewById<EditText>(R.id.etDeviceId)
        val etImovelId = view.findViewById<EditText>(R.id.etImovelId)
        val etBrokerUrl = view.findViewById<EditText>(R.id.etBrokerUrl)
        val etMqttUser = view.findViewById<EditText>(R.id.etMqttUser)
        val etMqttPass = view.findViewById<EditText>(R.id.etMqttPass)
        val btnSave = view.findViewById<Button>(R.id.btnSave)
        val etLabel = view.findViewById<EditText>(R.id.etLabel)

        // Load
        etDeviceId.setText(normalPrefs.getString("device_id", ""))
        etImovelId.setText(normalPrefs.getString("imovel_id", ""))
        etBrokerUrl.setText(normalPrefs.getString("broker_url", "tcp://192.168.1.100:1883"))
        etMqttUser.setText(encryptedPrefs.getString("mqtt_user", ""))
        etMqttPass.setText(encryptedPrefs.getString("mqtt_pass", ""))
        etLabel.setText(normalPrefs.getString("label_coleta", "ambiente_normal"))

        btnSave.setOnClickListener {
            Log.i("SetupFragment", "💾 Salvando novas configurações")
            normalPrefs.edit().apply {
                putString("device_id", etDeviceId.text.toString())
                putString("imovel_id", etImovelId.text.toString())
                putString("broker_url", etBrokerUrl.text.toString())
                putString("label_coleta", etLabel.text.toString())
                apply()
            }

            encryptedPrefs.edit().apply {
                putString("mqtt_user", etMqttUser.text.toString())
                putString("mqtt_pass", etMqttPass.text.toString())
                apply()
            }
            Log.d("SetupFragment", "✅ Configurações salvas com sucesso!")
        }
    }
}
