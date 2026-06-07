package com.example.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun GamePlayView(
    viewModel: BusGameViewModel,
    onOpenSettings: () -> Unit,
    onBackToMenu: () -> Unit
) {
    val isPaused by viewModel.isPaused.collectAsState()
    val isFlipped by viewModel.isFlipped.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070B19))
            .testTag("gameplay_root")
    ) {
        // Main split-screen driving view
        Column(modifier = Modifier.fillMaxSize()) {
            // Upper Area: Active Game Screen + Dashboard
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.3f)
            ) {
                MainDrivingScene(viewModel)
            }

            // Lower Area: Dynamic control panel (Steering, Pedals, Indicators, Stats)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.9f),
                color = Color(0xFF131A30),
                tonalElevation = 8.dp
            ) {
                DriveControlCabinet(viewModel, onOpenSettings)
            }
        }

        // Active notification banner overlay
        val notifyMsg by viewModel.notificationLog.collectAsState()
        AnimatedVisibility(
            visible = notifyMsg != null,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
        ) {
            notifyMsg?.let {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFACC15)),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        ),
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // Overturn flip alert
        if (isFlipped) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFBE123C)),
                    modifier = Modifier.width(300.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = "Alert",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "BUS OVERTURNED!",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Driving too sharply capsized the transport. Leverage the SOS menu in pause board to recover.",
                            color = Color.White.copy(alpha = 0.9f),
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { viewModel.togglePause() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                        ) {
                            Text("Open Pause & SOS")
                        }
                    }
                }
            }
        }

        // HUD Pause Button
        IconButton(
            onClick = { viewModel.togglePause() },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                .testTag("gameplay_pause_button")
        ) {
            Icon(
                imageVector = Icons.Filled.Pause,
                contentDescription = "Pause Game",
                tint = Color.White
            )
        }

        // Expanded Pause Panel with Map & Emergency SOS System
        if (isPaused) {
            PauseOverlay(
                viewModel = viewModel,
                onClose = { viewModel.togglePause() },
                onSettings = onOpenSettings,
                onExit = onBackToMenu
            )
        }
    }
}

@Composable
fun MainDrivingScene(viewModel: BusGameViewModel) {
    val px by viewModel.playerX.collectAsState()
    val py by viewModel.playerY.collectAsState()
    val heading by viewModel.playerHeading.collectAsState()
    val cameraAngle by viewModel.activeCameraAngle.collectAsState()
    val speed by viewModel.speed.collectAsState()
    val currentRoute by viewModel.selectedRoute.collectAsState()
    val activeVehicle = viewModel.getActiveVehicle()
    val peers by viewModel.multiplayerPlayers.collectAsState()
    val signals by viewModel.trafficSignals.collectAsState()
    val routePrompt by viewModel.isTerminalArrivalPrompt.collectAsState()

    // RETRO GAME STATISTICS & ENHANCEMENTS STATE COLLECTORS
    val coins by viewModel.collectableCoins.collectAsState()
    val hazards by viewModel.roadHazards.collectAsState()
    val passengerSatisfaction by viewModel.passengerSatisfaction.collectAsState()
    val passengerSpeak by viewModel.passengerSpeak.collectAsState()
    val isFlashingRed by viewModel.isFlashingRed.collectAsState()
    val isShaking by viewModel.isShaking.collectAsState()
    val currentScore by viewModel.currentScore.collectAsState()
    val highScore by viewModel.highScore.collectAsState()

    val shakeOffset = if (isShaking) {
        Offset(
            x = kotlin.random.Random.nextInt(-10, 10).toFloat(),
            y = kotlin.random.Random.nextInt(-10, 10).toFloat()
        )
    } else {
        Offset.Zero
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Render World based on Camera/Screen angles + shake modifiers
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = shakeOffset.x.dp, y = shakeOffset.y.dp)
                .pointerInput(Unit) {
                    detectTapGestures { }
                }
                .testTag("driving_canvas")
        ) {
            val canvasW = size.width
            val canvasH = size.height

            // 1. Draw solid grassy backdrop
            drawRect(color = Color(0xFF14243B))

            if (cameraAngle == 0) {
                // FIELD CAMERA: Bird's Eye (player in center static, camera panning)
                val zoom = 0.5f // zoom out
                val cx = canvasW / 2f
                val cy = canvasH / 2f

                drawRoadGridSystem(this, px, py, cx, cy, zoom)
                drawTrafficSigns(this, signals, px, py, cx, cy, zoom)
                drawTerminalsAndFuelStops(this, viewModel.landmarks, currentRoute, px, py, cx, cy, zoom)
                
                // Draw custom Gold Coins and Hurdles!
                drawArcadeCollectablesAndHazards(this, coins, hazards, px, py, cx, cy, zoom)
                
                drawMultiplayerPeers(this, peers, px, py, cx, cy, zoom)
                
                // Draw local player vehicle
                rotate(degrees = heading, pivot = Offset(cx, cy)) {
                    drawPlayerVehicleRect(this, cx, cy, activeVehicle, true)
                }
            } else if (cameraAngle == 1) {
                // FIELD CAMERA: 3rd Person follow (Camera rotates and keeps player moving upwards)
                val zoom = 0.8f
                val cx = canvasW / 2f
                val cy = canvasH / 2f + 80f // shift down slightly
                
                rotate(degrees = -heading - 90f, pivot = Offset(cx, cy)) {
                    drawRoadGridSystem(this, px, py, cx, cy, zoom)
                    drawTrafficSigns(this, signals, px, py, cx, cy, zoom)
                    drawTerminalsAndFuelStops(this, viewModel.landmarks, currentRoute, px, py, cx, cy, zoom)
                    
                    // Draw custom Gold Coins and Hurdles!
                    drawArcadeCollectablesAndHazards(this, coins, hazards, px, py, cx, cy, zoom)
                    
                    drawMultiplayerPeers(this, peers, px, py, cx, cy, zoom)
                }

                // Player oriented facing up in rotation frame
                rotate(degrees = 90f, pivot = Offset(cx, cy)) {
                    drawPlayerVehicleRect(this, cx, cy, activeVehicle, false)
                }
            } else {
                // COCKPIT VIEW: Full cockpit dashboard, windshield view showing road and upcoming hazards
                // Draw windshield graphic showing road scrolling up
                val roadScroll = (py % 120f)
                val cx = canvasW / 2f
                val cy = canvasH / 2f - 40f

                // Windshield roads
                drawRect(color = Color(0xFF0F172A), size = Size(canvasW, canvasH * 0.7f)) // Horizon back
                
                // Vanishing point perspective lines representation
                drawPolygonWindshieldRoad(this, canvasW, canvasH * 0.7f, heading)

                // Render other multiplayer cars coming down
                drawWindshieldCars(this, peers, canvasW, canvasH * 0.7f)

                // RENDER STEERING WHEEL AND COCKPIT HUD OVERLAY
                drawCockpitInteriorHousing(this, canvasW, canvasH, heading)
            }
        }

        // RED IMPACT DAMAGE FLASH Overlay
        if (isFlashingRed) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Red.copy(alpha = 0.38f))
            )
        }

        val fuel by viewModel.currentFuel.collectAsState()
        val cash by viewModel.userCash.collectAsState()
        val maxFuel = activeVehicle.fuelTankCapacity
        val fuelPercent = (fuel / maxFuel).coerceIn(0f, 1f)

        // IMMERSIVE OVERLAY: Center speedometer gauge panel
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 54.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${speed.toInt()}",
                fontSize = 58.sp,
                fontWeight = FontWeight.Bold,
                color = if (speed > 55f) Color(0xFFFFCC00) else Color.White,
                letterSpacing = (-2).sp,
                modifier = Modifier.padding(0.dp)
            )
            Box(
                modifier = Modifier
                    .background(Color(0xFF3B82F6).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "KM/H",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF60A5FA),
                    letterSpacing = 1.5.sp
                )
            }
        }

        // HUD Dashboard Left Panel (Floating Route telemetry & Visual mini outline)
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
                .width(135.dp)
                .background(Color(0xCC07090F), RoundedCornerShape(16.dp))
                .border(1.dp, Color(0x2BFFFFFF), RoundedCornerShape(16.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // ARCADE SCORE BOARD UNIT
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("SCORE", fontSize = 8.sp, color = Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                    Text("$currentScore", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFFFFCC00))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("HI-BEST", fontSize = 8.sp, color = Color.LightGray.copy(alpha = 0.5f))
                    Text("$highScore", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            // Divider Line
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.1f)))

            // Passenger satisfaction stats bar
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("MOOD RATING", fontSize = 8.sp, color = Color.LightGray, fontWeight = FontWeight.Bold)
                    Text("${passengerSatisfaction.toInt()}%", fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, color = if (passengerSatisfaction < 45f) Color(0xFFEF4444) else Color(0xFF10B981))
                }
                LinearProgressIndicator(
                    progress = { passengerSatisfaction / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = if (passengerSatisfaction < 45f) Color(0xFFEF4444) else Color(0xFF10B981),
                    trackColor = Color(0xFF1E293B)
                )
            }

            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.1f)))

            if (currentRoute != null) {
                Text(
                    text = "TO: ${currentRoute?.endStation}",
                    fontSize = 11.sp,
                    color = Color(0xFFFF9F0A),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${viewModel.currentPassengersLoaded.collectAsState().value} Loaded",
                    fontSize = 10.sp,
                    color = Color(0xFF10B981),
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = "NO ACTIVE DEST",
                    fontSize = 9.sp,
                    color = Color.LightGray.copy(alpha = 0.6f)
                )
            }
        }

        // HUD Dashboard Right Panel (Floating Cash and Fuel gauge status metrics)
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 12.dp, end = 68.dp) // Leave alignment room for circle Pause button overlay
                .width(135.dp)
                .background(Color(0xCC07090F), RoundedCornerShape(16.dp))
                .border(1.dp, Color(0x2BFFFFFF), RoundedCornerShape(16.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Cash counter
            Column {
                Text(
                    text = "WALLET CASH",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF60A5FA),
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "$$cash",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF22C55E)
                )
            }

            // Fuel slider bar indicator
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("FUEL TANK", fontSize = 8.sp, color = Color.LightGray)
                    Text("${(fuelPercent * 100).toInt()}%", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEAB308))
                }
                LinearProgressIndicator(
                    progress = { fuelPercent },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (fuelPercent < 0.22f) Color(0xFFEF4444) else Color(0xFFEAB308),
                    trackColor = Color(0xFF1E293B)
                )
            }

            // Camera status text indicator
            Text(
                text = if (cameraAngle == 0) "CAM: TOPDOWN" else if (cameraAngle == 1) "CAM: FOLLOW" else "CAM: COCKPIT",
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = Color.LightGray.copy(alpha = 0.6f)
            )
        }

        // Simulated online peers count in lobby
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp)
                .background(Color(0xFF0F172A).copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                .border(0.8.dp, Color(0xFF10B981), RoundedCornerShape(12.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF10B981)))
                Spacer(modifier = Modifier.width(6.dp))
                Text("CITY SERVER: active", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
            }
        }

        // Interactive prompts overlay when parked inside active terminal zones
        routePrompt?.let { prompt ->
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(bottom = 50.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.width(280.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = getPromptHeader(prompt),
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = getPromptInfo(prompt, viewModel),
                            fontSize = 12.sp,
                            color = Color.LightGray,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { handlePromptAction(prompt, viewModel) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            modifier = Modifier.fillMaxWidth().testTag("prompt_action_btn")
                        ) {
                            Text(getPromptButtonText(prompt), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // PASSENGER SPEAKING DYNAMIC OVERLAY CARD
        passengerSpeak?.let { quote ->
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
                    .background(Color.Black.copy(alpha = 0.85f), RoundedCornerShape(12.dp))
                    .border(1.2.dp, Color(0xFFFFD700), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .widthIn(max = 290.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "💬",
                        fontSize = 18.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Column {
                        Text(
                            text = "PASSENGER",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = quote,
                            fontSize = 11.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DriveControlCabinet(
    viewModel: BusGameViewModel,
    onOpenSettings: () -> Unit
) {
    val speed by viewModel.speed.collectAsState()
    val fuel by viewModel.currentFuel.collectAsState()
    val engineStarted by viewModel.engineStarted.collectAsState()
    val gear by viewModel.gear.collectAsState()
    val indicators by viewModel.indicatorState.collectAsState()
    val hornActive by viewModel.hornActive.collectAsState()
    val controlType by viewModel.controlType.collectAsState()
    val energyState by viewModel.engineCondition.collectAsState()
    val activeVehicle = viewModel.getActiveVehicle()

    // Blinking effect for indicators
    var blinkerOn by remember { mutableStateOf(false) }
    LaunchedEffect(indicators) {
        if (indicators != IndicatorState.None) {
            while (true) {
                blinkerOn = !blinkerOn
                delay(350)
            }
        } else {
            blinkerOn = false
        }
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Col 1: System controls (Gear, Ignition, Horn, Camera, Settings)
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            // Ignition Start/Stop
            Button(
                onClick = { viewModel.startStopEngine() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (engineStarted) Color(0xFFDC2626) else Color(0xFF16A34A)
                ),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(42.dp).testTag("ignition_btn")
            ) {
                Text(
                    if (engineStarted) "STOP ENGINE" else "START ENGINE",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }

            // Gear shift selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Button(
                    onClick = { viewModel.cycleGear() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (gear == Gear.D) Color.Cyan else Color.DarkGray
                    ),
                    modifier = Modifier.weight(1f).height(38.dp).testTag("gear_drive_btn"),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("DRIVE (D)", fontSize = 10.sp, color = if (gear == Gear.D) Color.Black else Color.White)
                }

                Button(
                    onClick = { viewModel.cycleGear() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (gear == Gear.R) Color.Cyan else Color.DarkGray
                    ),
                    modifier = Modifier.weight(1f).height(38.dp).testTag("gear_reverse_btn"),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("REV (R)", fontSize = 10.sp, color = if (gear == Gear.R) Color.Black else Color.White)
                }
            }

            // Horn & Lights Board
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = { viewModel.playHorn() },
                    modifier = Modifier
                        .weight(1f)
                        .background(if (hornActive) Color.Yellow else Color(0xFF334155), RoundedCornerShape(8.dp))
                        .height(38.dp)
                        .testTag("horn_btn")
                ) {
                    Icon(Icons.Filled.DirectionsBus, contentDescription = "Horn", tint = Color.White)
                }

                IconButton(
                    onClick = { viewModel.switchCameraAngle() },
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFF334155), RoundedCornerShape(8.dp))
                        .height(38.dp)
                        .testTag("camera_btn")
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Camera", tint = Color.Cyan)
                }
            }

            // Indicator blinker selectors
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = { viewModel.triggerIndicator(IndicatorState.Left) },
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (indicators == IndicatorState.Left && blinkerOn) Color(0xFFFF9F0A) else Color(0xFF475569),
                            RoundedCornerShape(6.dp)
                        )
                        .height(34.dp)
                ) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Left Blink", tint = Color.White)
                }

                IconButton(
                    onClick = { viewModel.triggerIndicator(IndicatorState.Right) },
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (indicators == IndicatorState.Right && blinkerOn) Color(0xFFFF9F0A) else Color(0xFF475569),
                            RoundedCornerShape(6.dp)
                        )
                        .height(34.dp)
                ) {
                    Icon(Icons.Filled.ArrowForward, contentDescription = "Right Blink", tint = Color.White)
                }
            }
        }

        // Col 2: High-Fi Dashboard Instruments Dial Panel (Speed, Fuel, Damage)
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1.3f)
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Speed indicator display
            Text(
                "${speed.toInt()} mph",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color.Green,
                    letterSpacing = 1.sp
                ),
                modifier = Modifier.testTag("instrument_speedometer")
            )
            Text("VELOCITY", fontSize = 9.sp, color = Color.LightGray)

            Spacer(modifier = Modifier.height(10.dp))

            // Fuel progress percentage indicator
            val tankMax = activeVehicle.fuelTankCapacity
            val fuelPercent = (fuel / tankMax).coerceIn(0f, 1f)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.LocalGasStation, contentDescription = "Gas", tint = Color.Cyan, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                LinearProgressIndicator(
                    progress = fuelPercent,
                    color = if (fuelPercent < 0.2f) Color.Yellow else Color.Cyan,
                    trackColor = Color.DarkGray,
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
            }
            Text("${fuel.toInt()}L / ${tankMax.toInt()}L FUEL", fontSize = 9.sp, color = Color.Cyan)

            Spacer(modifier = Modifier.height(8.dp))

            // Damage engine conditions
            val conditionPercent = energyState / 100f
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Build, contentDescription = "Repair", tint = Color(0xFFEC4899), modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                LinearProgressIndicator(
                    progress = conditionPercent,
                    color = if (conditionPercent < 0.3f) Color.Red else Color(0xFFEC4899),
                    trackColor = Color.DarkGray,
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
            }
            Text("ENGINE HEALTH: ${energyState.toInt()}%", fontSize = 9.sp, color = Color(0xFFEC4899))
        }

        // Col 3: Kinetic Steering interface matching control configurations (Wheel Drag, Gyro Tilt Slider, or Touch Keys)
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (controlType) {
                ControlType.Touch -> {
                    // Classic direct left / right arrow controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = { },
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp)
                                .pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragStart = { viewModel.inputSteer = -1.0f },
                                        onDragEnd = { viewModel.inputSteer = 0.0f },
                                        onDragCancel = { viewModel.inputSteer = 0.0f },
                                        onDrag = { _, _ -> viewModel.inputSteer = -1.0f }
                                    )
                                }
                                .testTag("touch_steer_left"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Turn Left", tint = Color.White)
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Button(
                            onClick = { },
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp)
                                .pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragStart = { viewModel.inputSteer = 1.0f },
                                        onDragEnd = { viewModel.inputSteer = 0.0f },
                                        onDragCancel = { viewModel.inputSteer = 0.0f },
                                        onDrag = { _, _ -> viewModel.inputSteer = 1.0f }
                                    )
                                }
                                .testTag("touch_steer_right"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.ArrowForward, contentDescription = "Turn Right", tint = Color.White)
                        }
                    }
                }
                
                ControlType.Tilt -> {
                    // Simulated tilt slider representing device rotating left or right
                    var sliderAngle by remember { mutableStateOf(0f) }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Simulated Tilt Pivot", fontSize = 9.sp, color = Color.White)
                        Slider(
                            value = sliderAngle,
                            onValueChange = {
                                sliderAngle = it
                                viewModel.inputSteer = it
                            },
                            valueRange = -1.0f..1.0f,
                            onValueChangeFinished = {
                                sliderAngle = 0f
                                viewModel.inputSteer = 0f
                            },
                            modifier = Modifier.fillMaxWidth().testTag("tilt_steer_slider"),
                            colors = SliderDefaults.colors(thumbColor = Color.Yellow)
                        )
                    }
                }

                ControlType.Steering -> {
                    // Large interactive drag steering wheel
                    var wheelAngle by remember { mutableStateOf(0f) }
                    Box(
                        modifier = Modifier
                            .size(75.dp)
                            .clip(CircleShape)
                            .background(Color.Black)
                            .rotate(wheelAngle)
                            .testTag("steering_wheel")
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDrag = { change, dragAmount ->
                                        // Rotate wheel based on offset
                                        wheelAngle = (wheelAngle + dragAmount.x * 2.5f).coerceIn(-150f, 150f)
                                        viewModel.inputSteer = (wheelAngle / 150f)
                                    },
                                    onDragEnd = {
                                        wheelAngle = 0f
                                        viewModel.inputSteer = 0f
                                    },
                                    onDragCancel = {
                                        wheelAngle = 0f
                                        viewModel.inputSteer = 0f
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        // Drawing steering wheel spokes inside canvas
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(color = Color.DarkGray, radius = size.minDimension / 2f, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8f))
                            drawLine(color = Color.LightGray, start = Offset(0f, size.height/2), end = Offset(size.width, size.height/2), strokeWidth = 6f)
                            drawLine(color = Color.LightGray, start = Offset(size.width/2, size.height/2), end = Offset(size.width/2, size.height), strokeWidth = 6f)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // GAS / REVERSE PEDALS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                // BRAKE pedal
                Button(
                    onClick = { },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { viewModel.inputPedal = -1.0f },
                                onDragEnd = { viewModel.inputPedal = 0.0f },
                                onDragCancel = { viewModel.inputPedal = 0.0f },
                                onDrag = { _, _ -> viewModel.inputPedal = -1.0f }
                            )
                        }
                        .testTag("pedal_brake"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64748B)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("BRAKE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(modifier = Modifier.width(6.dp))

                // ACCEL pedal
                Button(
                    onClick = { },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { viewModel.inputPedal = 1.0f },
                                onDragEnd = { viewModel.inputPedal = 0.0f },
                                onDrag = { _, _ -> viewModel.inputPedal = 1.0f },
                                onDragCancel = { viewModel.inputPedal = 0.0f }
                            )
                        }
                        .testTag("pedal_accel"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("DRIVE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

// Draw world roads
private fun drawRoadGridSystem(
    scope: DrawScope,
    px: Float,
    py: Float,
    cx: Float,
    cy: Float,
    zoom: Float
) {
    val horizontalRoadsY = listOf(300f, 1000f, 1700f)
    val verticalRoadsX = listOf(300f, 1000f, 1700f)
    val roadWidth = 140f

    // Draw Roads
    horizontalRoadsY.forEach { ry ->
        val mappedY = cy + (ry - py) * zoom
        scope.drawRect(
            color = Color(0xFF273549),
            topLeft = Offset(0f, mappedY - (roadWidth / 2f) * zoom),
            size = Size(scope.size.width, roadWidth * zoom)
        )
        // Dashed centering line
        scope.drawLine(
            color = Color.White.copy(alpha = 0.6f),
            start = Offset(0f, mappedY),
            end = Offset(scope.size.width, mappedY),
            strokeWidth = 3f,
            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(20f, 15f), 0f)
        )
    }

    verticalRoadsX.forEach { rx ->
        val mappedX = cx + (rx - px) * zoom
        scope.drawRect(
            color = Color(0xFF273549),
            topLeft = Offset(mappedX - (roadWidth / 2f) * zoom, 0f),
            size = Size(roadWidth * zoom, scope.size.height)
        )
        scope.drawLine(
            color = Color.White.copy(alpha = 0.6f),
            start = Offset(mappedX, 0f),
            end = Offset(mappedX, scope.size.height),
            strokeWidth = 3f,
            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(20f, 15f), 0f)
        )
    }
}

// Draw red/yellow/green light signs
private fun drawTrafficSigns(
    scope: DrawScope,
    signals: List<CityTrafficSignal>,
    px: Float,
    py: Float,
    cx: Float,
    cy: Float,
    zoom: Float
) {
    signals.forEach { sig ->
        val mappedX = cx + (sig.x - px) * zoom
        val mappedY = cy + (sig.y - py) * zoom

        val color = when (sig.state) {
            SignalColor.Green -> Color.Green
            SignalColor.Yellow -> Color.Yellow
            SignalColor.Red -> Color.Red
        }

        // Draw outer pole
        scope.drawCircle(color = Color.Black, radius = 18f * zoom, center = Offset(mappedX, mappedY))
        scope.drawCircle(color = color, radius = 11f * zoom, center = Offset(mappedX, mappedY))
    }
}

// Draw stations, service shops, and fuel icons
private fun drawTerminalsAndFuelStops(
    scope: DrawScope,
    landmarks: List<Landmark>,
    currentRoute: Route?,
    px: Float,
    py: Float,
    cx: Float,
    cy: Float,
    zoom: Float
) {
    landmarks.forEach { lm ->
        val mappedX = cx + (lm.x - px) * zoom
        val mappedY = cy + (lm.y - py) * zoom

        val isTarget = currentRoute != null && (lm.name.lowercase().contains(currentRoute.endStation.lowercase()) || lm.name.lowercase().contains(currentRoute.startStation.lowercase()))
        val color = when (lm.type) {
            LandmarkType.Terminal -> if (isTarget) Color(0xFFFF9F0A) else Color(0xFF3B82F6)
            LandmarkType.PetrolStation -> Color(0xFF0EA5E9)
            LandmarkType.ServiceShop -> Color(0xFFE11D48)
            else -> Color.Gray
        }

        // Base square card
        scope.drawRect(
            color = color,
            topLeft = Offset(mappedX - 25f * zoom, mappedY - 25f * zoom),
            size = Size(50f * zoom, 50f * zoom)
        )

        // Draw landmark halo ring
        scope.drawCircle(
            color = color.copy(alpha = 0.3f),
            radius = 60f * zoom,
            center = Offset(mappedX, mappedY),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f * zoom)
        )
    }
}

// Draw player
private fun drawPlayerVehicleRect(
    scope: DrawScope,
    cx: Float,
    cy: Float,
    vehicle: Vehicle,
    isTopDown: Boolean
) {
    val coachW = if (vehicle.isBus) 48f else 32f
    val coachH = if (vehicle.isBus) 100f else 62f

    // Coach body
    scope.drawRect(
        color = vehicle.paintColor,
        topLeft = Offset(cx - coachW / 2f, cy - coachH / 2f),
        size = Size(coachW, coachH)
    )

    // Windshield mirror
    scope.drawRect(
        color = Color.Black,
        topLeft = Offset(cx - coachW * 0.4f, cy - coachH * 0.42f),
        size = Size(coachW * 0.8f, coachH * 0.16f)
    )

    // Headlamps glowing
    scope.drawCircle(color = Color.Yellow, radius = 6f, center = Offset(cx - coachW * 0.3f, cy - coachH * 0.48f))
    scope.drawCircle(color = Color.Yellow, radius = 6f, center = Offset(cx + coachW * 0.3f, cy - coachH * 0.48f))
}

// Spawn multiplayer friends on 2D map
private fun drawMultiplayerPeers(
    scope: DrawScope,
    peers: List<MultiplayerFriend>,
    px: Float,
    py: Float,
    cx: Float,
    cy: Float,
    zoom: Float
) {
    peers.forEach { peer ->
        val mappedX = cx + (peer.x - px) * zoom
        val mappedY = cy + (peer.y - py) * zoom

        val sizeW = if (peer.isBus) 34f * zoom else 24f * zoom
        val sizeH = if (peer.isBus) 70f * zoom else 44f * zoom

        // Peer body block
        scope.drawRect(
            color = peer.paintColor,
            topLeft = Offset(mappedX - sizeW / 2f, mappedY - sizeH / 2f),
            size = Size(sizeW, sizeH)
        )

        // Small headlights indicator
        scope.drawCircle(color = Color.White, radius = 3f * zoom, center = Offset(mappedX - sizeW * 0.3f, mappedY - sizeH * 0.45f))
        scope.drawCircle(color = Color.White, radius = 3f * zoom, center = Offset(mappedX + sizeW * 0.3f, mappedY - sizeH * 0.45f))
    }
}

// Windshield geometry equations
private fun drawPolygonWindshieldRoad(scope: DrawScope, w: Float, h: Float, heading: Float) {
    // Left border vanishing
    scope.drawLine(
        color = Color.DarkGray,
        start = Offset(w / 2f - 20f, h * 0.3f),
        end = Offset(-100f, h),
        strokeWidth = 14f
    )

    // Right border vanishing
    scope.drawLine(
        color = Color.DarkGray,
        start = Offset(w / 2f + 20f, h * 0.3f),
        end = Offset(w + 100f, h),
        strokeWidth = 14f
    )

    // center dividing dashes
    scope.drawLine(
        color = Color.Yellow.copy(alpha = 0.5f),
        start = Offset(w / 2f, h * 0.35f),
        end = Offset(w / 2f, h),
        strokeWidth = 4f,
        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(30f, 25f), 0f)
    )
}

private fun drawWindshieldCars(scope: DrawScope, peers: List<MultiplayerFriend>, w: Float, h: Float) {
    // Draw peers randomly as oncoming rectangles of scaling sizes based on proximity
    peers.take(2).forEachIndexed { idx, peer ->
        val scale = 0.5f + idx * 0.5f
        val carW = 60f * scale
        val carH = 40f * scale
        val col = peer.paintColor

        scope.drawRect(
            color = col,
            topLeft = Offset(w / 2f - carW / 2f + (idx * 130f - 65f), h - 120f + idx * 40f),
            size = Size(carW, carH)
        )
        // lights
        scope.drawCircle(color = Color.Yellow, radius = 4f * scale, center = Offset(w / 2f - carW * 0.3f + (idx * 130f - 65f), h - 110f + idx * 40f))
        scope.drawCircle(color = Color.Yellow, radius = 4f * scale, center = Offset(w / 2f + carW * 0.3f + (idx * 130f - 65f), h - 110f + idx * 40f))
    }
}

private fun drawCockpitInteriorHousing(scope: DrawScope, w: Float, h: Float, steeringAngle: Float) {
    // Dash housing at the bottom 40% of standard cockpit Viewport
    val dashTopY = h * 0.58f
    scope.drawRect(
        color = Color(0xFF0F172A),
        topLeft = Offset(0f, dashTopY),
        size = Size(w, h - dashTopY)
    )

    // Analog instrument panel layout
    scope.drawCircle(
        color = Color(0xFF1E293B),
        radius = 55f,
        center = Offset(w * 0.3f, dashTopY + 70f),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f)
    )
    // Speed text dial
    scope.drawCircle(
        color = Color.Black,
        radius = 50f,
        center = Offset(w * 0.3f, dashTopY + 70f)
    )

    // Cockpit physical Steering Wheel (drawn rotated dynamically!)
    scope.rotate(degrees = steeringAngle, pivot = Offset(w * 0.7f, dashTopY + 70f)) {
        scope.drawCircle(
            color = Color(0xFF334155),
            radius = 50f,
            center = Offset(w * 0.7f, dashTopY + 70f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 12f)
        )
        scope.drawLine(
            color = Color.Black,
            start = Offset(w * 0.7f - 40f, dashTopY + 70f),
            end = Offset(w * 0.7f + 40f, dashTopY + 70f),
            strokeWidth = 8f
        )
        scope.drawLine(
            color = Color.Black,
            start = Offset(w * 0.7f, dashTopY + 70f),
            end = Offset(w * 0.7f, dashTopY + 115f),
            strokeWidth = 8f
        )
    }
}

// Helpers for on-road prompts
private fun getPromptHeader(prompt: String): String {
    val items = prompt.split(":")
    return when (items[0]) {
        "PETROL_STATION" -> "⛽ PETROL STATION ARRIVAL"
        "SERVICE_SHOP" -> "🔧 SERVICE GARAGE INBOUND"
        "ROUTE_START" -> "🚌 BUS STATION BOARDING"
        "ROUTE_END" -> "🏁 ROUTE DESTINATION SUCCESS"
        else -> "LANDMARK"
    }
}

private fun getPromptInfo(prompt: String, viewModel: BusGameViewModel): String {
    val items = prompt.split(":")
    return when (items[0]) {
        "PETROL_STATION" -> "Stopping beside fuel racks. Refill storage fully?\nCost: $2.50 per Litre."
        "SERVICE_SHOP" -> "Auto mechanics waiting inside bay. Clean mud and repair engine health.\nCost: $350 flat price."
        "ROUTE_START" -> {
            val count = viewModel.selectedRoute.value?.waitingPassengers ?: 10
            "Load and board $count waiting travelers onto the transport."
        }
        "ROUTE_END" -> {
            val reward = viewModel.selectedRoute.value?.priceReward ?: 600
            "Safely delivered passengers! Deliver transit report for a $$reward payoff!"
        }
        else -> ""
    }
}

private fun getPromptButtonText(prompt: String): String {
    val items = prompt.split(":")
    return when (items[0]) {
        "PETROL_STATION" -> "REFUEL TANK"
        "SERVICE_SHOP" -> "REPAIR & SERVICE"
        "ROUTE_START" -> "LOAD PASSENGERS"
        "ROUTE_END" -> "CLAIM REWARD"
        else -> "OK"
    }
}

private fun handlePromptAction(prompt: String, viewModel: BusGameViewModel) {
    val items = prompt.split(":")
    when (items[0]) {
        "PETROL_STATION" -> viewModel.performRefuel()
        "SERVICE_SHOP" -> viewModel.performServiceRepair()
        "ROUTE_START" -> viewModel.loadWaitingPassengers()
        "ROUTE_END" -> viewModel.completeActiveRoute()
    }
}

// Pause menu, Map HUD, and SOS Assist panel
@Composable
fun PauseOverlay(
    viewModel: BusGameViewModel,
    onClose: () -> Unit,
    onSettings: () -> Unit,
    onExit: () -> Unit
) {
    val userCash by viewModel.userCash.collectAsState()
    val px by viewModel.playerX.collectAsState()
    val py by viewModel.playerY.collectAsState()
    val chat by viewModel.chatMessages.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.82f))
            .pointerInput(Unit) { detectTapGestures { } }
            .testTag("pause_overlay"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "PAUSE DESKTOP",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
                Text(
                    "$$userCash BANK",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            // Central layout: City Map (left side/top) and Actions list (right side/bottom)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Interactive 2D Map of the city grid
                Box(
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.2.dp, Color.Cyan, RoundedCornerShape(12.dp))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val zoom = size.width / 2000f // Scale map onto available size class

                        // grid roads background grey lines
                        val horizontalRoadsY = listOf(300f, 1000f, 1700f)
                        val verticalRoadsX = listOf(300f, 1000f, 1700f)
                        horizontalRoadsY.forEach { yVal ->
                            drawRect(color = Color(0xFF1E293B), topLeft = Offset(0f, yVal * zoom - 8f), size = Size(size.width, 16f))
                        }
                        verticalRoadsX.forEach { xVal ->
                            drawRect(color = Color(0xFF1E293B), topLeft = Offset(xVal * zoom - 8f, 0f), size = Size(16f, size.height))
                        }

                        // Landmarks (Stations/Fuel/Shop symbols)
                        viewModel.landmarks.forEach { lm ->
                            val color = when (lm.type) {
                                LandmarkType.Terminal -> Color(0xFF3B82F6)
                                LandmarkType.PetrolStation -> Color(0xFFFF9F0A)
                                LandmarkType.ServiceShop -> Color(0xFFE52763)
                                else -> Color.LightGray
                            }
                            drawCircle(color = color, radius = 9f, center = Offset(lm.x * zoom, lm.y * zoom))
                        }

                        // player coordinates marked as dot
                        drawCircle(color = Color.Cyan, radius = 12f, center = Offset(px * zoom, py * zoom))
                        drawCircle(color = Color.White, radius = 4f, center = Offset(px * zoom, py * zoom))
                    }
                    
                    Text(
                        "LIVE GPS CITIMAP",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Cyan,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 6.dp)
                    )
                }

                // Action controls list + EMERGENCY SOS System!
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Core navigation modifiers
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { viewModel.restartMission() },
                            modifier = Modifier.fillMaxWidth().testTag("pause_restart_mission"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155))
                        ) {
                            Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("RESTART MISSION", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onSettings,
                            modifier = Modifier.fillMaxWidth().testTag("pause_open_settings"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155))
                        ) {
                            Icon(Icons.Filled.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("DRIVING SETTINGS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // EMERGENCY SOS MODULE panel to rescue flipped/stuck vehicles
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFBE1233).copy(alpha = 0.25f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.border(1.2.dp, Color(0xFFBE1233), RoundedCornerShape(10.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "🚨 EMERGENCY VEHICLE SOS rescue",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            // Paid tow: reset, repair max, refuel max ($2500)
                            Button(
                                onClick = { viewModel.triggerSOSRescuePaid() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBE1233)),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(34.dp)
                                    .testTag("sos_paid_tow"),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Premium Escort: $2500", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))

                            // Free tow: reset flipped state ($0)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.triggerSOSFlipRescue() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569)),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(30.dp)
                                        .testTag("sos_unflip"),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("Unflip Balance: $0", fontSize = 8.5.sp)
                                }

                                Button(
                                    onClick = { viewModel.triggerSOSFreeRescueRefuel() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569)),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(30.dp)
                                        .testTag("sos_free_respawn"),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("Tow Grid Center: $0", fontSize = 8.5.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Lower chat section list (Simulating online Lobby friends)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Text("LOBBY MOCK SYSTEM CHAT:", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                if (chat.isEmpty()) {
                    Text("No wireless transmission active currently.", color = Color.LightGray, fontSize = 10.sp)
                } else {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            chat.take(3).forEach { ch ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "[${ch.sender}]: ",
                                        color = ch.color,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                    Text(ch.message, color = Color.White, fontSize = 11.sp, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Exit CTAs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onClose,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    modifier = Modifier.weight(1f).testTag("pause_resume")
                ) {
                    Text("RESUME SIMULATOR", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onExit,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    modifier = Modifier.weight(1f).testTag("pause_exit")
                ) {
                    Text("QUIT TO MENU")
                }
            }
        }
    }
}

// ARCADE VECTOR GRAPHICS RENDERING HELPERS
private fun drawArcadeCollectablesAndHazards(
    scope: DrawScope,
    coins: List<CollectableCoin>,
    hazards: List<RoadHazard>,
    px: Float,
    py: Float,
    cx: Float,
    cy: Float,
    zoom: Float
) {
    // Draw Coins
    coins.forEach { coin ->
        if (!coin.collected) {
            val mappedX = cx + (coin.x - px) * zoom
            val mappedY = cy + (coin.y - py) * zoom
            val coinSize = coin.size * zoom

            // Gold Coin Circle
            scope.drawCircle(
                color = Color(0xFFFFD700), // Gold
                radius = coinSize / 2f,
                center = Offset(mappedX, mappedY)
            )
            // Inner embossed ring for vintage arcade feel
            scope.drawCircle(
                color = Color(0xFFB8860B), // Dark Goldenrod
                radius = coinSize / 3.2f,
                center = Offset(mappedX, mappedY),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5f * zoom)
            )
        }
    }

    // Draw Road Hazards
    hazards.forEach { hazard ->
        val mappedX = cx + (hazard.x - px) * zoom
        val mappedY = cy + (hazard.y - py) * zoom
        val hazSize = hazard.size * zoom

        when (hazard.type) {
            HazardType.SafetyCone -> {
                // Draw dynamic safety cone (orange triangle + white band)
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(mappedX, mappedY - hazSize / 2f)
                    lineTo(mappedX + hazSize / 2f, mappedY + hazSize / 2f)
                    lineTo(mappedX - hazSize / 2f, mappedY + hazSize / 2f)
                    close()
                }
                scope.drawPath(path, color = Color(0xFFFF6600)) // Bright Safety Orange
                
                // Draw base
                scope.drawRect(
                    color = Color.Black,
                    topLeft = Offset(mappedX - hazSize * 0.6f, mappedY + hazSize * 0.35f),
                    size = Size(hazSize * 1.2f, 5f * zoom)
                )
            }
            HazardType.RoadBarrier -> {
                // Bold black/yellow diagonal stripes barricade block
                // Main box
                scope.drawRect(
                    color = Color(0xFFFFCC00), // Traffic yellow
                    topLeft = Offset(mappedX - hazSize * 0.7f, mappedY - hazSize * 0.25f),
                    size = Size(hazSize * 1.4f, hazSize * 0.5f)
                )
                // Black support posts
                scope.drawRect(color = Color.Black, topLeft = Offset(mappedX - hazSize * 0.6f, mappedY + hazSize * 0.25f), size = Size(5f * zoom, hazSize * 0.3f))
                scope.drawRect(color = Color.Black, topLeft = Offset(mappedX + hazSize * 0.45f, mappedY + hazSize * 0.25f), size = Size(5f * zoom, hazSize * 0.3f))
                
                // Stripe line
                scope.drawLine(
                    color = Color.Black,
                    start = Offset(mappedX - hazSize * 0.5f, mappedY - hazSize * 0.15f),
                    end = Offset(mappedX + hazSize * 0.5f, mappedY + hazSize * 0.15f),
                    strokeWidth = 6f * zoom
                )
            }
            HazardType.OilSpill -> {
                // Dark slippery puddle on bottom road
                scope.drawOval(
                    color = Color(0xFF1C1917).copy(alpha = 0.85f), // Charcoal stone
                    topLeft = Offset(mappedX - hazSize * 0.8f, mappedY - hazSize * 0.4f),
                    size = Size(hazSize * 1.6f, hazSize * 0.8f)
                )
                // Rainbow sheen outline (oil puddle vibe)
                scope.drawOval(
                    color = Color.Cyan.copy(alpha = 0.5f),
                    topLeft = Offset(mappedX - hazSize * 0.7f, mappedY - hazSize * 0.35f),
                    size = Size(hazSize * 1.4f, hazSize * 0.7f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f * zoom)
                )
            }
        }
    }
}
