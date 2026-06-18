package com.example.app_proprietario.ui.components.PropertyList

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyListTopBar() {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = "Meus imoveis",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "v1 - selecione um imovel para monitorar",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        },
//        actions = {
//            IconButton(onClick = { }) {
//                Icon(Icons.Default.Settings, contentDescription = "Configurações")
//            }
//        }
    )
}