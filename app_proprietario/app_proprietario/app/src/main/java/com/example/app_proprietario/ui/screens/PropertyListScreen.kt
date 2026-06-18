package com.example.app_proprietario.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.app_proprietario.data.Property
import com.example.app_proprietario.data.SampleData
import com.example.app_proprietario.ui.components.PropertyList.PropertyCard
import com.example.app_proprietario.ui.components.PropertyList.PropertyListTopBar

@Composable
fun PropertyListScreen(
    properties: List<Property>,
    onPropertyClick: (Property) -> Unit
) {
    Scaffold(
        topBar = {
            PropertyListTopBar()
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(properties) { property ->
                PropertyCard(
                    property = property,
                    onClick = { onPropertyClick(property) }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PropertyListScreenPreview() {
    MaterialTheme {
        PropertyListScreen(
            properties = SampleData.properties,
            onPropertyClick = {
                print("clicked")
            }
        )
    }
}
