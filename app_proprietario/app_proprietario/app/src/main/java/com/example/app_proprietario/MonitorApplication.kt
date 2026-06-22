package com.example.app_proprietario

import android.app.Application
import com.example.app_proprietario.data.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MonitorApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@MonitorApplication)
            modules(appModule)
        }
    }
}
