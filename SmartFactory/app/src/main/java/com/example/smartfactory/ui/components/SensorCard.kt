package com.example.smartfactory.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SensorCard(

    title: String,
    value: String,
    status: String

) {

    Card(

        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),

        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        )

    ) {

        Column(

            modifier = Modifier.padding(16.dp)

        ) {

            Text(

                text = title,

                style = MaterialTheme.typography.titleMedium

            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(

                text = value,

                style = MaterialTheme.typography.headlineMedium

            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(

                text = status,

                style = MaterialTheme.typography.bodyLarge

            )

        }

    }

}