package com.example.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalView(
    viewModel: BusGameViewModel,
    onBack: () -> Unit
) {
    val userCash by viewModel.userCash.collectAsState()
    val activeVehicle = viewModel.getActiveVehicle()
    val routes = viewModel.allRoutes

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "METRO BUS STAND",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            color = Color.White
                        )
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("terminal_back_button")
                    ) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back to Menu")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF070B14),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF070B14) // Deep immersive black background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Stats Panel
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(14.dp))
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F131E)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "CURRENT VEHICLE",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Color.LightGray.copy(alpha = 0.6f),
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            activeVehicle.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "BALANCE",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Color(0xFF3B82F6),
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            "$$userCash",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = Color(0xFF22C55E),
                                fontWeight = FontWeight.Black
                            )
                        )
                    }
                }
            }

            // Route Terminal departure banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.DirectionsBus,
                    contentDescription = "Bus terminal departures",
                    tint = Color(0xFF3B82F6),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "DEPARTURE BOARD (10 MAIN LINES)",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                )
            }

            Text(
                "Select a destination below to set GPS telemetry and load passengers. Complete the route inside the city to receive a high-cash prize.",
                style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray.copy(alpha = 0.8f)),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Departure Grid / List of top routes
            LazyColumn(
                modifier = Modifier.weight(1.5f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Show nearly 10-12 routes with distance and arrival status
                items(routes) { route ->
                    RouteDepartureCard(
                        route = route,
                        onClick = { viewModel.selectRoute(route) }
                    )
                }
            }
        }
    }
}

@Composable
fun RouteDepartureCard(
    route: Route,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag("route_card_${route.id}"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111420)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "${route.startStation} ➔ ${route.endStation}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        route.name,
                        style = MaterialTheme.typography.labelMedium.copy(color = Color(0xFF3B82F6), fontWeight = FontWeight.Bold)
                    )
                }
                
                // Live boarding status / ETA
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFFF9F0A).copy(alpha = 0.1f))
                        .border(1.dp, Color(0xFFFF9F0A).copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Navigation,
                            contentDescription = "ETA",
                            tint = Color(0xFFFF9F0A),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "ETA: ${route.arrivalTimeText}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFFFF9F0A),
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            HorizontalDivider(color = Color(0x1F22D55E), modifier = Modifier.padding(vertical = 4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.DirectionsBus,
                        contentDescription = "Distance",
                        tint = Color.Cyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "${route.distanceKm} KM",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.Cyan,
                            fontWeight = FontWeight.SemiBold
                        )
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Passengers",
                        tint = Color.LightGray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "${route.waitingPassengers} Waiting",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray)
                    )
                }

                Text(
                    "Reward: $${route.priceReward}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF22C55E),
                        fontWeight = FontWeight.Black
                    )
                )
            }
        }
    }
}
