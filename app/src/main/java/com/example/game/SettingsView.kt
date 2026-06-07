package com.example.game

import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsView(
    viewModel: BusGameViewModel,
    onBack: () -> Unit
) {
    val controlType by viewModel.controlType.collectAsState()
    val sensitivity by viewModel.sensitivity.collectAsState()

    var activeControl by remember { mutableStateOf(controlType) }
    var sensVal by remember { mutableStateOf(sensitivity) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "DRIVING SETTINGS",
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
                        modifier = Modifier.testTag("settings_back_button")
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
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Text(
                    "CHOOSE PREFERRED DRIVING INTERFACE:",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color.Cyan)
                )

                // Layout Selector Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ControlTypeButton(
                        label = "STEERING",
                        subLabel = "Drag Wheel",
                        selected = (activeControl == ControlType.Steering),
                        modifier = Modifier.weight(1f).testTag("settings_ctrl_steering"),
                        onClick = { activeControl = ControlType.Steering }
                    )

                    ControlTypeButton(
                        label = "TILT",
                        subLabel = "Simulated Gyro",
                        selected = (activeControl == ControlType.Tilt),
                        modifier = Modifier.weight(1f).testTag("settings_ctrl_tilt"),
                        onClick = { activeControl = ControlType.Tilt }
                    )

                    ControlTypeButton(
                        label = "TOUCH",
                        subLabel = "Left/Right Buttons",
                        selected = (activeControl == ControlType.Touch),
                        modifier = Modifier.weight(1f).testTag("settings_ctrl_touch"),
                        onClick = { activeControl = ControlType.Touch }
                    )
                }

                Divider(color = Color.DarkGray)

                // Sensitivity slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "STEERING SENSITIVITY",
                            style = MaterialTheme.typography.labelMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            String.format("%.2f x", sensVal),
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.Cyan, fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Slider(
                        value = sensVal,
                        onValueChange = { sensVal = it },
                        valueRange = 0.5f..2.0f,
                        modifier = Modifier.fillMaxWidth().testTag("settings_sensitivity_slider"),
                        colors = SliderDefaults.colors(
                            thumbColor = Color.Cyan,
                            activeTrackColor = Color.Cyan,
                            inactiveTrackColor = Color.DarkGray
                        )
                    )

                    Text(
                        "Lower values give stable cornering, higher values permit aggressive turns inside compact grid roundabouts.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray),
                        fontSize = 11.sp
                    )
                }

                // Instructions panel
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = "Tips",
                            tint = Color.Yellow,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "DRIVING RULES & FINES",
                                style = MaterialTheme.typography.labelMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "• Stopping at RED signals is mandatory or incur a -$120 citation.\n" +
                                "• Refuel fuel tanks at PETROL stations (marked near roadways).\n" +
                                "• Repair structural engine collisions at any local Auto SERVICE Shop.\n" +
                                "• Adjusting camera angles can help park safely at terminals.",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray),
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // Save Settings CTA Button
            Button(
                onClick = {
                    viewModel.updateSettings(activeControl, sensVal)
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("settings_apply_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
            ) {
                Text("APPLY & SAVE CONFIGURATION", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ControlTypeButton(
    label: String,
    subLabel: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Color(0xFF1E3A8A) else Color(0xFF1E293B)
        ),
        shape = RoundedCornerShape(8.dp),
        border = if (selected) BorderStroke(2.dp, Color.Cyan) else null
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = if (selected) Color.White else Color.LightGray,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                subLabel,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (selected) Color.Cyan else Color.Gray,
                    fontSize = 9.sp
                ),
                maxLines = 1
            )
        }
    }
}
