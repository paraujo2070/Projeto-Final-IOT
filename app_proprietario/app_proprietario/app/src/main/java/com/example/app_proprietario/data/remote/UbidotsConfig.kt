package com.example.app_proprietario.data.remote

import com.example.app_proprietario.BuildConfig

object UbidotsConfig {
    const val BASE_URL = "https://industrial.api.ubidots.com/"
    const val AUTH_TOKEN = BuildConfig.UBIDOTS_TOKEN
    const val AUTO_REFRESH_INTERVAL_MS = 10 * 60 * 1000L // 10 min
}

object UbidotsVariables {
    const val HUMIDITY = "umidade_relativa_pct"
    const val TEMPERATURE = "temperatura_c"
    const val INTRUSION_DETECTED = "intrusao_detectada"
    const val INTRUSION_CONFIDENCE = "intrusao_confianca"
    const val MOLD_RISK_CODE = "risco_mofo_codigo"
    const val THERMAL_ACCELERATION = "aceleracao_termica"
}