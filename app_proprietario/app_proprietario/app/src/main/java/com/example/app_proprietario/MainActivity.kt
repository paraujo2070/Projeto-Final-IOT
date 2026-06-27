package com.example.app_proprietario

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.example.app_proprietario.navigation.MonitorNavGraph
import com.example.app_proprietario.ui.theme.Background
import com.example.app_proprietario.ui.theme.BorderStroke
import com.example.app_proprietario.ui.theme.BrandAccent
import com.example.app_proprietario.ui.theme.IntrusionColor
import com.example.app_proprietario.ui.theme.Surface
import com.example.app_proprietario.ui.theme.TextPrimary
import com.example.app_proprietario.ui.theme.TextSecondary

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MonitorAppTheme {
                MonitorNavGraph()
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun MonitorAppTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = lightColorScheme(
        primary = BrandAccent,
        onPrimary = Surface,
        background = Surface,
        onBackground = TextPrimary,
        surface = Surface,
        onSurface = TextPrimary,
        surfaceVariant = Background,
        onSurfaceVariant = TextSecondary,
        outline = BorderStroke,
        error = IntrusionColor,
        onError = Surface,
        errorContainer = IntrusionColor.copy(alpha = 0.1f),
        onErrorContainer = IntrusionColor
    )

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}