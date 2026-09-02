package com.example.uniqo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileFeatureScreen(
    title: String,
    description: String,
    onBack: () -> Unit,
    // ADD-ON: optional real rating, only used when a caller (e.g. Reviews
    // Received) explicitly passes it. Every existing call site that doesn't
    // pass these keeps compiling and rendering exactly as before.
    rating: Double? = null,
    ratingLabel: String = "Average Rating"
) {

    Scaffold(
        containerColor = Background,

        topBar = {

            TopAppBar(
                title = {
                    Text(title)
                },

                navigationIcon = {

                    IconButton(
                        onClick = onBack
                    ) {

                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background
                )
            )
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Background)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        CardWhite,
                        RoundedCornerShape(20.dp)
                    )
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // ADD-ON: real rating block, shown only when a caller passes one.
                if (rating != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFA000),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            String.format("%.1f", rating),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        ratingLabel,
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(18.dp))
                    HorizontalDivider(color = Divider)
                    Spacer(Modifier.height(18.dp))
                }

                Icon(
                    Icons.Default.Construction,
                    contentDescription = null,
                    tint = PurplePrimary,
                    modifier = Modifier.size(44.dp)
                )

                Spacer(
                    Modifier.height(14.dp)
                )

                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(
                    Modifier.height(8.dp)
                )

                Text(
                    description,
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}