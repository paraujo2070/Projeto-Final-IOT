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
    const val BAROMETER_MEAN = "barometro_hpa_media"
    const val BAROMETER_VARIANCE = "barometro_hpa_variancia"
    const val ACCEL_X_VARIANCE = "accel_x_variancia"
    const val ACCEL_Y_VARIANCE = "accel_y_variancia"
    const val ACCEL_Z_VARIANCE = "accel_z_variancia"
    const val GYRO_X_VARIANCE = "gyro_x_variancia"
    const val GYRO_Y_VARIANCE = "gyro_y_variancia"
    const val GYRO_Z_VARIANCE = "gyro_z_variancia"

    val MOTION_VARIANCE_VARIABLES = listOf(
        ACCEL_X_VARIANCE, ACCEL_Y_VARIANCE, ACCEL_Z_VARIANCE,
        GYRO_X_VARIANCE, GYRO_Y_VARIANCE, GYRO_Z_VARIANCE
    )
}

