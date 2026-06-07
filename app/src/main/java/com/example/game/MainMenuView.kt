package com.example.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MainMenuView(
    viewModel: BusGameViewModel,
    onEnterTerminal: () -> Unit,
    onEnterGarage: () -> Unit,
    onEnterSettings: () -> Unit,
    onFreeRoam: () -> Unit
) {
    val userCash by viewModel.userCash.collectAsState()
    val activeVehicle = viewModel.getActiveVehicle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF080D1A), Color(0xFF04060B))
                )
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // App Launcher Title Header area
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 40.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(Color(0xFF3B82F6).copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFF3B82F6), RoundedCornerShape(8.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    "3D SIMULATION ENGINE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3B82F6),
                    letterSpacing = 2.sp
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "CITY TRANSIT",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 4.sp
                )
            )
            Text(
                "BUS DRIVER",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF10B981),
                    letterSpacing = 6.sp
                )
            )
            Text(
                "Version 2.4.9 // IMMERSIVE TELEMETRY ENGAGED",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = Color.LightGray.copy(alpha = 0.5f),
                letterSpacing = 1.sp
            )
        }

        // Cash flow & status card with immersive glass border theme
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(16.dp))
                .padding(vertical = 12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0x990A0A0A)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "WALLET CASH",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF3B82F6),
                            letterSpacing = 1.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        "$$userCash",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF22C55E)
                        )
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "MY ACTIVE COACH",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.LightGray,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        activeVehicle.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        "Lvl ${activeVehicle.engineUpgradeLevel} Tuning",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEAB308)
                    )
                }
            }
        }

        // Center CTA Button and Side CTAs Grid layout
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Main Action- PLAY free roam! with glowing borders and high contrast colors
            Button(
                onClick = onFreeRoam,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .border(1.5.dp, Color(0xFF22C55E).copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                    .testTag("menu_free_roam_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "START DRIVING SIM",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp,
                        letterSpacing = 1.5.sp,
                        color = Color.White
                    )
                }
            }

            // Quick navigation panel grids
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MenuQuickCard(
                    title = "BUS STAND",
                    info = "20 Routes List",
                    icon = Icons.Filled.DirectionsBus,
                    color = Color(0xFF3B82F6),
                    modifier = Modifier.weight(1f).testTag("menu_to_terminal"),
                    onClick = onEnterTerminal
                )

                MenuQuickCard(
                    title = "TUNING BAY",
                    info = "Garage & Paint",
                    icon = Icons.Filled.Palette,
                    color = Color(0xFF8B5CF6),
                    modifier = Modifier.weight(1f).testTag("menu_to_garage"),
                    onClick = onEnterGarage
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MenuQuickCard(
                    title = "CONTROLS",
                    info = "Steer Setup",
                    icon = Icons.Filled.Settings,
                    color = Color(0xFF64748B),
                    modifier = Modifier.weight(1f).testTag("menu_to_settings"),
                    onClick = onEnterSettings
                )

                MenuQuickCard(
                    title = "AUTO SERVICE",
                    info = "Service Workshops",
                    icon = Icons.Filled.Build,
                    color = Color(0xFFEC4899),
                    modifier = Modifier.weight(1f).testTag("menu_to_service"),
                    onClick = {
                        // Directly trigger driving state and let GPS focus on repair landmarks for quick repair!
                        onFreeRoam()
                        viewModel.selectState(GameState.Driving)
                    }
                )
            }
        }

        // Sub footer
        Text(
            "Metro Transit Driver © 2026. Drive carefully. Obey city traffic light lanes.",
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 10.sp,
                color = Color.LightGray.copy(alpha = 0.4f),
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.padding(bottom = 12.dp)
        )
    }
}

@Composable
fun MenuQuickCard(
    title: String,
    info: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(72.dp)
            .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(12.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F131E)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                    .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    title,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 12.sp,
                    maxLines = 1,
                    letterSpacing = 0.5.sp
                )
                Text(
                    info,
                    fontSize = 9.sp,
                    color = Color.LightGray.copy(alpha = 0.7f),
                    maxLines = 1
                )
            }
        }
    }
}
