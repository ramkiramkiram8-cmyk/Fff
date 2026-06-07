package com.example.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GarageView(
    viewModel: BusGameViewModel,
    onBack: () -> Unit
) {
    val userCash by viewModel.userCash.collectAsState()
    val ownedVehicles by viewModel.ownedVehicleIds.collectAsState()
    val activeId by viewModel.activeVehicleId.collectAsState()
    val paints by viewModel.vehiclePaints.collectAsState()
    val upgrades by viewModel.vehicleEngineLevels.collectAsState()

    // Color swatches to select from for paint customization ($150)
    val colorSwatches = listOf(
        0xFFFF3B30.toInt() to "Fierce Red",
        0xFF007AFF.toInt() to "Electric Blue",
        0xFF34C759.toInt() to "Emerald Green",
        0xFFFFCC00.toInt() to "Canary Yellow",
        0xFFFF9500.toInt() to "Sunset Purple",
        0xFFAF52DE.toInt() to "Cyber Violet",
        0xFF5AC8FA.toInt() to "Sleek Teal",
        0xFF1E293B.toInt() to "Matte Black"
    )

    var currentSelectedIdx by remember { mutableStateOf(0) }
    val currentSelectedVehicle = viewModel.garageVehicles[currentSelectedIdx]
    val isOwned = ownedVehicles.contains(currentSelectedVehicle.id)
    val isActive = (activeId == currentSelectedVehicle.id)

    // Current paint and upgrade of selected vehicle (or default if not customized yet)
    val activePaint = paints[currentSelectedVehicle.id] ?: 0xFFFF3B30.toInt()
    val activeUpgrade = upgrades[currentSelectedVehicle.id] ?: 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "METRO TUNING GARAGE",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp,
                            color = Color.White
                        )
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("garage_back_button")
                    ) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF070B14),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF070B14)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Money Indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "FLEET CUSTOMIZATION",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = Color.LightGray.copy(alpha = 0.6f),
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    "$$userCash",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Color(0xFF22C55E),
                        fontWeight = FontWeight.Black
                    ),
                    modifier = Modifier.testTag("garage_cash_indicator")
                )
            }

            // Fleet Carousels
            Text(
                "SELECT BUS OR CAR TO VIEW:",
                style = MaterialTheme.typography.labelSmall.copy(color = Color.Cyan)
            )
            Spacer(modifier = Modifier.height(4.dp))

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.garageVehicles.size) { idx ->
                    val v = viewModel.garageVehicles[idx]
                    val owned = ownedVehicles.contains(v.id)
                    val active = activeId == v.id
                    val selected = currentSelectedIdx == idx

                    Card(
                        modifier = Modifier
                            .width(130.dp)
                            .clickable { currentSelectedIdx = idx }
                            .border(
                                width = 2.dp,
                                color = if (selected) Color.Cyan else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .testTag("garage_selector_${v.id}"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selected) Color(0xFF334155) else Color(0xFF1E293B)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DirectionsBus,
                                contentDescription = "Vehicle Icon",
                                tint = if (v.isBus) Color.Green else Color.Yellow,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                v.name,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                ),
                                maxLines = 1,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                if (active) "Active" else if (owned) "Owned" else "$${v.price}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (active) Color.Cyan else if (owned) Color.LightGray else Color.Yellow
                                )
                            )
                        }
                    }
                }
            }

            // Selected Vehicle View Screen
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Vehicle Preview Model
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            currentSelectedVehicle.name,
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            currentSelectedVehicle.description,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.LightGray,
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Visual Coach Color Box representation
                        Box(
                            modifier = Modifier
                                .size(width = 200.dp, height = 75.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(activePaint))
                                .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    if (currentSelectedVehicle.isBus) "🚌 METRO BUS" else "🚗 METRO CAR",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    "Upgrade Level: Lvl $activeUpgrade",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.8f))
                                )
                            }
                        }
                    }

                    // Performance Stats Panel
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        PerformanceMetric("V-Max", "${currentSelectedVehicle.maxSpeed.toInt()}mph", Color.Green)
                        PerformanceMetric("Fuel Tank", "${currentSelectedVehicle.fuelTankCapacity.toInt()}L", Color.Cyan)
                        PerformanceMetric("Tuning", "Stage $activeUpgrade", Color.Yellow)
                    }

                    // Ownership / Actions
                    if (isOwned) {
                        // Custom modding capabilities
                        Column(modifier = Modifier.fillMaxWidth()) {
                            if (!isActive) {
                                Button(
                                    onClick = { viewModel.selectActiveVehicle(currentSelectedVehicle.id) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp)
                                        .testTag("garage_activate_btn")
                                ) {
                                    Text("SELECT ACTIVE VEHICLE")
                                }
                            } else {
                                Text(
                                    "✓ ACTIVE VEHICLE CONFIGURED",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = Color.Cyan,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier
                                        .padding(bottom = 8.dp)
                                        .align(Alignment.CenterHorizontally)
                                )
                            }

                            // Mod Paints Row
                            Divider(color = Color.DarkGray, modifier = Modifier.padding(bottom = 8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Palette, contentDescription = null, tint = Color.LightGray)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Custom Paint ($150)",
                                    style = MaterialTheme.typography.labelMedium.copy(color = Color.White)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(colorSwatches) { (hex, name) ->
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Color(hex))
                                            .clickable { viewModel.applyCustomPaint(hex) }
                                            .border(
                                                width = if (activePaint == hex) 2.dp else 0.dp,
                                                color = Color.White,
                                                shape = CircleShape
                                            )
                                            .testTag("paint_color_${name.replace(" ", "_")}")
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Engine tuning upgrades
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Build, contentDescription = null, tint = Color.LightGray)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "Engine Core Tuning",
                                        style = MaterialTheme.typography.labelMedium.copy(color = Color.White)
                                    )
                                }
                                
                                val upgradeCost = when (activeUpgrade) {
                                    0 -> 300
                                    1 -> 600
                                    2 -> 1200
                                    else -> 0
                                }

                                if (activeUpgrade < 3) {
                                    Button(
                                        onClick = { viewModel.upgradeEngine() },
                                        shape = RoundedCornerShape(6.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.testTag("garage_upgrade_btn")
                                    ) {
                                        Text("Stage ${activeUpgrade + 1}: $$upgradeCost", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Text("Stage 3 (MAX)", style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray))
                                }
                            }
                        }
                    } else {
                        // Purchase Button
                        Button(
                            onClick = { viewModel.buyVehicle(currentSelectedVehicle) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("garage_buy_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Icon(Icons.Filled.ShoppingCart, contentDescription = "Buy")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("BUY VEHICLE ($${currentSelectedVehicle.price})", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PerformanceMetric(title: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, style = MaterialTheme.typography.labelSmall.copy(color = Color.LightGray))
        Text(value, style = MaterialTheme.typography.titleMedium.copy(color = color, fontWeight = FontWeight.Bold))
    }
}
