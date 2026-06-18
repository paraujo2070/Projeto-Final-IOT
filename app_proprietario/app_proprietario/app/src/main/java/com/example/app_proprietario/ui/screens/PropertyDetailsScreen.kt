package com.example.app_proprietario.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app_proprietario.data.Property
import com.example.app_proprietario.data.Room
import com.example.app_proprietario.data.SampleData
import com.example.app_proprietario.ui.components.PropertyDetails.PropertyDetailsTopBar
import com.example.app_proprietario.ui.components.PropertyDetails.PropertyStatusBanner
import com.example.app_proprietario.ui.components.PropertyDetails.RoomCard
import com.example.app_proprietario.ui.components.SyncFooter

@Composable
fun PropertyDetailsScreen(
    property: Property,
    onBack: () -> Unit,
    onRoomClick: (Room) -> Unit
) {
    Scaffold(
        topBar = {
            PropertyDetailsTopBar(
                title = property.name,
                onBack = onBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                item {
                    PropertyStatusBanner(property = property)
                }

                item {
                    Text(
                        text = "COMODOS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                items(property.rooms) { room ->
                    RoomCard(
                        room = room,
                        onClick = { onRoomClick(room) }
                    )
                }
            }

            SyncFooter(
                text = "Ultima sincronização ${property.lastSync}"
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PropertyDetailsScreenPreview() {
    MaterialTheme {
        PropertyDetailsScreen(
            property = SampleData.properties.first(),
            onBack = {},
            onRoomClick = {}
        )
    }
}
