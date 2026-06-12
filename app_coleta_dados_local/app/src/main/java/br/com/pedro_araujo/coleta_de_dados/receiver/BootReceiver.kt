package br.com.pedro_araujo.coleta_de_dados.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import br.com.pedro_araujo.coleta_de_dados.service.TelemetryService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val isEnabled = prefs.getBoolean("service_enabled", false)
            
            Log.i("BootReceiver", "🔄 Dispositivo reiniciado. Coleta habilitada: $isEnabled")
            
            if (isEnabled) {
                val serviceIntent = Intent(context, TelemetryService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            } else {
                Log.d("BootReceiver", "ℹ️ Coleta não iniciada automaticamente (desativada pelo usuário)")
            }
        }
    }
}
