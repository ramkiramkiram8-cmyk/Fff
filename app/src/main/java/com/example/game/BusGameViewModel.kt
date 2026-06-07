package com.example.game

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

class BusGameViewModel : ViewModel() {

    // Gameplay and user status flows
    private val _userCash = MutableStateFlow(5000)
    val userCash: StateFlow<Int> = _userCash.asStateFlow()

    private val _gameState = MutableStateFlow(GameState.MainMenu)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val _ownedVehicleIds = MutableStateFlow(setOf("city_cruiser"))
    val ownedVehicleIds: StateFlow<Set<String>> = _ownedVehicleIds.asStateFlow()

    private val _activeVehicleId = MutableStateFlow("city_cruiser")
    val activeVehicleId: StateFlow<String> = _activeVehicleId.asStateFlow()

    // Key map customizations: Paint ColorHex, EngineUpgrade Level (0..3)
    private val _vehiclePaints = MutableStateFlow(mapOf("city_cruiser" to 0xFFFF3B30.toInt())) // Red
    val vehiclePaints: StateFlow<Map<String, Int>> = _vehiclePaints.asStateFlow()

    private val _vehicleEngineLevels = MutableStateFlow(mapOf("city_cruiser" to 0))
    val vehicleEngineLevels: StateFlow<Map<String, Int>> = _vehicleEngineLevels.asStateFlow()

    // Control and Settings
    private val _controlType = MutableStateFlow(ControlType.Touch)
    val controlType: StateFlow<ControlType> = _controlType.asStateFlow()

    private val _sensitivity = MutableStateFlow(1.0f)
    val sensitivity: StateFlow<Float> = _sensitivity.asStateFlow()

    private val _activeCameraAngle = MutableStateFlow(1) // 0 = Bird's Eye, 1 = 3rd Person, 2 = Interior/Dash
    val activeCameraAngle: StateFlow<Int> = _activeCameraAngle.asStateFlow()

    // Simulation Live Stats
    private val _currentFuel = MutableStateFlow(100f)
    val currentFuel: StateFlow<Float> = _currentFuel.asStateFlow()

    private val _engineCondition = MutableStateFlow(100f) // percent
    val engineCondition: StateFlow<Float> = _engineCondition.asStateFlow()

    private val _speed = MutableStateFlow(0f) // mph
    val speed: StateFlow<Float> = _speed.asStateFlow()

    private val _gear = MutableStateFlow(Gear.D)
    val gear: StateFlow<Gear> = _gear.asStateFlow()

    private val _engineStarted = MutableStateFlow(false)
    val engineStarted: StateFlow<Boolean> = _engineStarted.asStateFlow()

    private val _indicatorState = MutableStateFlow(IndicatorState.None)
    val indicatorState: StateFlow<IndicatorState> = _indicatorState.asStateFlow()

    private val _hornActive = MutableStateFlow(false)
    val hornActive: StateFlow<Boolean> = _hornActive.asStateFlow()

    private val _isFlipped = MutableStateFlow(false)
    val isFlipped: StateFlow<Boolean> = _isFlipped.asStateFlow()

    private val _currentPassengersLoaded = MutableStateFlow(0)
    val currentPassengersLoaded: StateFlow<Int> = _currentPassengersLoaded.asStateFlow()

    private val _isTerminalArrivalPrompt = MutableStateFlow<String?>(null)
    val isTerminalArrivalPrompt: StateFlow<String?> = _isTerminalArrivalPrompt.asStateFlow()

    // Coordinates (Map size is 2000 x 2000)
    private val _playerX = MutableStateFlow(1000f)
    val playerX: StateFlow<Float> = _playerX.asStateFlow()

    private val _playerY = MutableStateFlow(1000f)
    val playerY: StateFlow<Float> = _playerY.asStateFlow()

    private val _playerHeading = MutableStateFlow(0f) // angle in degrees
    val playerHeading: StateFlow<Float> = _playerHeading.asStateFlow()

    private val _selectedRoute = MutableStateFlow<Route?>(null)
    val selectedRoute: StateFlow<Route?> = _selectedRoute.asStateFlow()

    // Live Multiplayer peers
    private val _multiplayerPlayers = MutableStateFlow<List<MultiplayerFriend>>(emptyList())
    val multiplayerPlayers: StateFlow<List<MultiplayerFriend>> = _multiplayerPlayers.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    // Traffic signals and landmark database
    private val _trafficSignals = MutableStateFlow<List<CityTrafficSignal>>(emptyList())
    val trafficSignals: StateFlow<List<CityTrafficSignal>> = _trafficSignals.asStateFlow()

    // Visual log notifier
    private val _notificationLog = MutableStateFlow<String?>(null)
    val notificationLog: StateFlow<String?> = _notificationLog.asStateFlow()

    // GAMIFIED FLOWS
    private val _collectableCoins = MutableStateFlow<List<CollectableCoin>>(emptyList())
    val collectableCoins: StateFlow<List<CollectableCoin>> = _collectableCoins.asStateFlow()

    private val _roadHazards = MutableStateFlow<List<RoadHazard>>(emptyList())
    val roadHazards: StateFlow<List<RoadHazard>> = _roadHazards.asStateFlow()

    private val _passengerSatisfaction = MutableStateFlow(100f)
    val passengerSatisfaction: StateFlow<Float> = _passengerSatisfaction.asStateFlow()

    private val _passengerSpeak = MutableStateFlow<String?>(null)
    val passengerSpeak: StateFlow<String?> = _passengerSpeak.asStateFlow()

    private val _isFlashingRed = MutableStateFlow(false)
    val isFlashingRed: StateFlow<Boolean> = _isFlashingRed.asStateFlow()

    private val _isShaking = MutableStateFlow(false)
    val isShaking: StateFlow<Boolean> = _isShaking.asStateFlow()

    private val _currentScore = MutableStateFlow(0)
    val currentScore: StateFlow<Int> = _currentScore.asStateFlow()

    private val _highScore = MutableStateFlow(15800)
    val highScore: StateFlow<Int> = _highScore.asStateFlow()

    private val _numberOfCoinsCollected = MutableStateFlow(0)
    val numberOfCoinsCollected: StateFlow<Int> = _numberOfCoinsCollected.asStateFlow()

    var oilSpillSpinTimer = 0f

    // Score effect feedback
    data class ScoreText(val text: String, val x: Float, val y: Float, val birthTime: Long)
    private val _floatingScores = MutableStateFlow<List<ScoreText>>(emptyList())
    val floatingScores: StateFlow<List<ScoreText>> = _floatingScores.asStateFlow()

    fun triggerScoreFloatingText(text: String, x: Float, y: Float) {
        val list = _floatingScores.value.toMutableList()
        list.add(ScoreText(text, x, y, System.currentTimeMillis()))
        _floatingScores.value = list
    }

    // Standard list of garage vehicles
    val garageVehicles = listOf(
        Vehicle("city_cruiser", "City Cruiser Bus", isBus = true, price = 0, maxSpeed = 70f, fuelTankCapacity = 100f, baseHandling = 1.0f, "Standard transit carriage perfect for grid roadways."),
        Vehicle("transit_master", "Transit Master Bus", isBus = true, price = 3000, maxSpeed = 95f, fuelTankCapacity = 140f, baseHandling = 1.2f, "High capacity transport with robust acceleration."),
        Vehicle("cyber_coach", "Cyber Coach Bus", isBus = true, price = 8000, maxSpeed = 130f, fuelTankCapacity = 200f, baseHandling = 1.4f, "Double-decker hybrid with hyper aerodynamic shell."),
        Vehicle("apex_hatch", "Apex GT Hatchback", isBus = false, price = 1500, maxSpeed = 110f, fuelTankCapacity = 60f, baseHandling = 1.5f, "Responsive daily supermini with snappy cornering."),
        Vehicle("hyper_sedan", "Viper Hyper Sedan", isBus = false, price = 4500, maxSpeed = 155f, fuelTankCapacity = 85f, baseHandling = 1.7f, "Performance executive car designed for fast cruising.")
    )

    // Complete Database of 20 distinct premium routes to fulfill user specifications.
    val allRoutes = listOf(
        Route(1, "Suburban Link (Express 1)", "Central Terminal", "Suburban North", 2.2f, 450, 7, "2 min", 1000f, 1000f, 1000f, 300f),
        Route(2, "Downtown Loop (Line 2)", "Central Terminal", "Downtown West", 3.0f, 600, 11, "3 min", 1000f, 1000f, 300f, 1000f),
        Route(3, "Southern Coast Transit (Line 3)", "Central Terminal", "South beach", 4.1f, 850, 14, "4 min", 1000f, 1000f, 1000f, 1700f),
        Route(4, "Eastern Airport Express (Line 4)", "Central Terminal", "Airport East", 4.5f, 950, 18, "5 min", 1000f, 1000f, 1700f, 1000f),
        Route(5, "Industrial Sector Connector", "Suburban North", "Industrial South", 5.2f, 1100, 9, "4 min", 1000f, 300f, 1600f, 1600f),
        Route(6, "Metro Plaza Shuttle", "Downtown West", "Airport East", 5.6f, 1200, 15, "6 min", 300f, 1000f, 1700f, 1000f),
        Route(7, "Scenic Beach Blvd Highway", "South beach", "Airport East", 6.0f, 1300, 12, "5 min", 1000f, 1700f, 1700f, 1000f),
        Route(8, "Coastal Skyline Route", "South beach", "Downtown West", 3.2f, 700, 10, "3 min", 1000f, 1700f, 300f, 1000f),
        Route(9, "Shopping District Line", "Central Terminal", "Metro Mall North", 1.8f, 380, 8, "1 min", 1000f, 1000f, 1200f, 400f),
        Route(10, "Hilltop Scenic Expressway", "Suburban North", "Valley View East", 6.5f, 1400, 20, "7 min", 1000f, 300f, 1700f, 600f),
        Route(11, "Midnight Harbor Link", "Downtown West", "Port Center", 3.8f, 750, 6, "2 min", 300f, 1000f, 400f, 1600f),
        Route(12, "University Campus Circular", "Central Terminal", "Varsity Quad", 1.5f, 320, 13, "1 min", 1000f, 1000f, 600f, 1400f),
        Route(13, "Medical District Loop", "Central Terminal", "City Clinic", 2.0f, 400, 10, "2 min", 1000f, 1000f, 1400f, 1400f),
        Route(14, "Financial Central Line", "Downtown West", "Bank Towers", 2.4f, 520, 12, "3 min", 300f, 1000f, 500f, 500f),
        Route(15, "Scenic Lake Boulevard", "Suburban North", "Lakeside Marina", 4.9f, 980, 14, "4 min", 1000f, 300f, 1500f, 300f),
        Route(16, "Riverside Commuter Line", "Central Terminal", "Estuary Point", 3.5f, 720, 15, "3 min", 1000f, 1000f, 400f, 400f),
        Route(17, "Stadium Fan Express", "Metro Mall North", "Olympic Park", 4.0f, 800, 25, "10 min", 1200f, 400f, 1600f, 1500f),
        Route(18, "Suburban Boulevard Ring", "Suburban North", "Suburban West", 2.8f, 550, 10, "2 min", 1000f, 300f, 300f, 500f),
        Route(19, "Eastside Cargo Transit", "Industrial South", "Port Center", 4.6f, 1000, 5, "5 min", 1600f, 1600f, 400f, 1600f),
        Route(20, "Grand City Cross-Tour", "Airport East", "Port Center", 7.8f, 1800, 30, "8 min", 1700f, 1000f, 400f, 1600f)
    )

    // Landmarks database for driving view (terminals, petrol stations, and service shops)
    val landmarks = listOf(
        Landmark("Central Terminal", LandmarkType.Terminal, 1000f, 1000f, emoji = "🏫"),
        Landmark("Suburban North Station", LandmarkType.Terminal, 1000f, 300f, emoji = "🏡"),
        Landmark("Downtown West Station", LandmarkType.Terminal, 300f, 1000f, emoji = "🏢"),
        Landmark("South Beach Station", LandmarkType.Terminal, 1000f, 1700f, emoji = "🏖️"),
        Landmark("Airport East Terminal", LandmarkType.Terminal, 1700f, 1000f, emoji = "✈️"),
        Landmark("Industrial South Station", LandmarkType.Terminal, 1600f, 1600f, emoji = "🏭"),
        Landmark("Port Center Terminal", LandmarkType.Terminal, 400f, 1600f, emoji = "🚢"),
        
        // Petrol Stations to refuel
        Landmark("East Highway Fueling", LandmarkType.PetrolStation, 1400f, 800f, emoji = "⛽"),
        Landmark("West Grid Refueling", LandmarkType.PetrolStation, 600f, 1200f, emoji = "⛽"),
        Landmark("Central Fuel Station", LandmarkType.PetrolStation, 850f, 850f, emoji = "⛽"),
        
        // Service shop
        Landmark("City Auto Body & Service", LandmarkType.ServiceShop, 1200f, 1200f, emoji = "🔧"),
        Landmark("Suburban Repair Center", LandmarkType.ServiceShop, 800f, 400f, emoji = "🔧")
    )

    // Game loop Job
    private var gameLoopJob: Job? = null

    init {
        // Prepare original traffic signals
        _trafficSignals.value = listOf(
            CityTrafficSignal(1, 1000f, 850f, SignalColor.Green, 8),
            CityTrafficSignal(2, 1000f, 1150f, SignalColor.Red, 4),
            CityTrafficSignal(3, 850f, 1000f, SignalColor.Green, 6),
            CityTrafficSignal(4, 1150f, 1000f, SignalColor.Red, 5)
        )

        // Spawn mock multiplayer players info
        spawnMultiplayers()

        // Spawn coin and hazard entities once initially
        generateCoinsAndHazards()

        // Start asynchronous cycle for traffic signal lights, multiplayer intelligence, and chat log
        startBackgroundCycle()
    }

    fun generateCoinsAndHazards() {
        val coinsList = mutableListOf<CollectableCoin>()
        val hazardsList = mutableListOf<RoadHazard>()
        
        var idCounter = 1
        val roadsX = listOf(300f, 1000f, 1700f)
        val roadsY = listOf(300f, 1000f, 1700f)
        
        // Spawn gold coins along main gridded streets
        roadsX.forEach { rx ->
            for (y in 150..1850 step 140) {
                if (y != 300 && y != 1000 && y != 1700) {
                    coinsList.add(CollectableCoin(id = idCounter++, x = rx + Random.nextInt(-15, 15), y = y.toFloat()))
                }
            }
        }
        roadsY.forEach { ry ->
            for (x in 150..1850 step 140) {
                if (x != 300 && x != 1000 && x != 1700) {
                    coinsList.add(CollectableCoin(id = idCounter++, x = x.toFloat(), y = ry + Random.nextInt(-15, 15)))
                }
            }
        }

        // Spawn dangerous hazards along streets
        var hazardId = 1
        roadsX.forEach { rx ->
            for (y in 400..1800 step 280) {
                if (y != 300 && y != 1000 && y != 1700 && Random.nextFloat() < 0.5f) {
                    val types = HazardType.values()
                    val randomType = types[Random.nextInt(types.size)]
                    hazardsList.add(RoadHazard(id = hazardId++, type = randomType, x = rx + Random.nextInt(-25, 25), y = y.toFloat()))
                }
            }
        }
        roadsY.forEach { ry ->
            for (x in 400..1800 step 280) {
                if (x != 300 && x != 1000 && x != 1700 && Random.nextFloat() < 0.5f) {
                    val types = HazardType.values()
                    val randomType = types[Random.nextInt(types.size)]
                    hazardsList.add(RoadHazard(id = hazardId++, type = randomType, x = x.toFloat(), y = ry + Random.nextInt(-25, 25)))
                }
            }
        }

        _collectableCoins.value = coinsList
        _roadHazards.value = hazardsList
        _passengerSatisfaction.value = 100f
        _passengerSpeak.value = "Let's roll! Keep it smooth and speedy! 🚌💨"
    }

    fun reducePassengerSatisfaction(amount: Float, quote: String) {
        _passengerSatisfaction.value = (_passengerSatisfaction.value - amount).coerceAtLeast(0f)
        _passengerSpeak.value = quote
        _currentScore.value = (_currentScore.value - 120).coerceAtLeast(0)
        
        viewModelScope.launch {
            delay(3000)
            if (_passengerSpeak.value == quote) {
                _passengerSpeak.value = null
            }
        }
        
        if (_passengerSatisfaction.value <= 0f) {
            addChatMessage("Server", "Driver dismissed: Passengers fled! 😡", Color.Red)
            showNotification("❌ DISMISSED: Passenger satisfaction hit 0%!")
            _userCash.value = (_userCash.value - 400).coerceAtLeast(0)
            restartMission()
        }
    }

    private fun triggerScreenImpactEffect() {
        viewModelScope.launch {
            _isFlashingRed.value = true
            _isShaking.value = true
            delay(150)
            _isFlashingRed.value = false
            delay(150)
            _isShaking.value = false
        }
    }

    private fun spawnMultiplayers() {
        val names = listOf("RouteKing_99", "TransitPro_Echo", "CargoSedan_Max", "Neon_Glider")
        val cols = listOf(Color.Cyan, Color.Yellow, Color.Green, Color.Magenta)
        _multiplayerPlayers.value = names.mapIndexed { idx, name ->
            val randomX = 500f + Random.nextFloat() * 1000f
            val randomY = 500f + Random.nextFloat() * 1000f
            MultiplayerFriend(
                id = "peer_$idx",
                name = name,
                x = randomX,
                y = randomY,
                speed = 20f + Random.nextFloat() * 15f,
                paintColor = cols[idx],
                isBus = idx % 2 == 0,
                targetX = 600f + Random.nextFloat() * 800f,
                targetY = 600f + Random.nextFloat() * 800f
            )
        }
    }

    private fun startBackgroundCycle() {
        // Coordinate traffic light timer loops, multiplayer paths, and friendly chats!
        viewModelScope.launch {
            while (true) {
                delay(1000)
                // 1. Update signals timer
                val currentSignals = _trafficSignals.value.map { sig ->
                    val nextTimer = sig.timerSeconds - 1
                    if (nextTimer <= 0) {
                        val nextColor = when (sig.state) {
                            SignalColor.Green -> SignalColor.Yellow
                            SignalColor.Yellow -> SignalColor.Red
                            SignalColor.Red -> SignalColor.Green
                        }
                        sig.copy(
                            state = nextColor,
                            timerSeconds = when (nextColor) {
                                SignalColor.Green -> 8
                                SignalColor.Yellow -> 3
                                SignalColor.Red -> 7
                            }
                        )
                    } else {
                        sig.copy(timerSeconds = nextTimer)
                    }
                }
                _trafficSignals.value = currentSignals

                // 2. Random simulated real-time player chat to keep server lively
                if (Random.nextInt(12) == 0) {
                    val activePeers = _multiplayerPlayers.value
                    if (activePeers.isNotEmpty()) {
                        val randomPeer = activePeers[Random.nextInt(activePeers.size)]
                        val messages = listOf(
                            "Hey room, driving passengers along downtown loop!",
                            "Refueling at West Grid petrol terminal",
                            "Traffic signal went red, gotta stop!",
                            "Just upgraded my engine to stage 3! Zoom! 🚀",
                            "Who wants to follow my Cyber Coach to Central stand?",
                            "Need servicing, crashed on coastal freeway"
                        )
                        addChatMessage(randomPeer.name, messages[Random.nextInt(messages.size)], randomPeer.paintColor)
                    }
                }

                // 3. Move multiplayer mock peers subtly
                val nextPeers = _multiplayerPlayers.value.map { peer ->
                    val dx = peer.targetX - peer.x
                    val dy = peer.targetY - peer.y
                    val dist = sqrt(dx * dx + dy * dy)
                    if (dist < 40f) {
                        // pick new destination from landmarks randomly
                        val targetLandmark = landmarks[Random.nextInt(landmarks.size)]
                        peer.copy(targetX = targetLandmark.x, targetY = targetLandmark.y)
                    } else {
                        val moveStep = 18f // movement speed
                        val nx = peer.x + (dx / dist) * moveStep
                        val ny = peer.y + (dy / dist) * moveStep
                        peer.copy(x = nx, y = ny, speed = 25f + Random.nextFloat() * 10f)
                    }
                }
                _multiplayerPlayers.value = nextPeers
            }
        }
    }

    private fun addChatMessage(sender: String, msg: String, color: Color) {
        val currentLogs = _chatMessages.value.toMutableList()
        currentLogs.add(0, ChatMessage(sender, msg, color))
        if (currentLogs.size > 20) {
            currentLogs.removeLast()
        }
        _chatMessages.value = currentLogs
    }

    // Navigation and Action handlers
    fun selectState(state: GameState) {
        _gameState.value = state
        if (state == GameState.Driving) {
            generateCoinsAndHazards() // Refill items on starting!
            startGameLoop()
        } else {
            stopGameLoop()
        }
    }

    fun togglePause() {
        _isPaused.value = !_isPaused.value
        if (_isPaused.value) {
            stopGameLoop()
        } else {
            startGameLoop()
        }
    }

    fun restartMission() {
        // Reset player coordinates to route start point or central terminal
        val startX = _selectedRoute.value?.startX ?: 1000f
        val startY = _selectedRoute.value?.startY ?: 1000f
        _playerX.value = startX
        _playerY.value = startY
        _playerHeading.value = 0f
        _speed.value = 0f
        _currentPassengersLoaded.value = 0
        _isFlipped.value = false
        _isPaused.value = false
        _isTerminalArrivalPrompt.value = null
        this.oilSpillSpinTimer = 0f
        
        val activeCap = getActiveVehicle().fuelTankCapacity
        _currentFuel.value = activeCap
        _engineCondition.value = 100f
        
        // Regenerate arcade gems and obstacles
        generateCoinsAndHazards()
        
        showNotification("Mission Restarted!")
        startGameLoop()
    }

    // SOS Menu actions
    fun triggerSOSFlipRescue() {
        // Rescue flipping
        _isFlipped.value = false
        // Flip doesn't charge money or refuel, just restores vehicle balance
        showNotification("SOS: Vehicle un-flipped and aligned successfully!")
        if (_isPaused.value) togglePause()
    }

    fun triggerSOSRescuePaid() {
        // Emergency tow rescue with $2500 cost
        if (_userCash.value >= 2500) {
            _userCash.value -= 2500
            // Reset to Central Terminal
            _playerX.value = 1000f
            _playerY.value = 1000f
            _playerHeading.value = 0f
            _speed.value = 0f
            _isFlipped.value = false
            
            // Repair and Refuel fully
            val activeCap = getActiveVehicle().fuelTankCapacity
            _currentFuel.value = activeCap
            _engineCondition.value = 100f
            
            showNotification("SOS Action: Tow truck escorted you to central terminal. Cost: $2500")
            if (_isPaused.value) togglePause()
        } else {
            showNotification("Insufficient Cash for Paid Tow. Minimum $2500 required!")
        }
    }

    fun triggerSOSFreeRescueRefuel() {
        // Normal unstuck-rescue (stuck in road) -> reset coordinates slightly offset but don't charge or refill completely
        // To help player if they have $0
        _playerX.value = 1000f
        _playerY.value = 1000f
        _playerHeading.value = 0f
        _speed.value = 0f
        _isFlipped.value = false
        
        // Give minimal backup petrol if dry
        if (_currentFuel.value < 10f) {
            _currentFuel.value = 15f
        }
        
        showNotification("Free SOS: Reset position safely back to Central terminal.")
        if (_isPaused.value) togglePause()
    }

    fun startStopEngine() {
        _engineStarted.value = !_engineStarted.value
        val soundText = if (_engineStarted.value) "vroom" else "silent"
        showNotification("Engine is now: ${if (_engineStarted.value) "ON ⚡" else "OFF 💤"}")
    }

    fun cycleGear() {
        _gear.value = if (_gear.value == Gear.D) Gear.R else Gear.D
        showNotification("Gear switched to: ${_gear.value.name}")
    }

    fun selectRoute(route: Route) {
        _selectedRoute.value = route
        _playerX.value = route.startX
        _playerY.value = route.startY
        _playerHeading.value = 0f
        _speed.value = 0f
        _currentPassengersLoaded.value = 0
        _engineStarted.value = false
        
        val activeCap = getActiveVehicle().fuelTankCapacity
        _currentFuel.value = activeCap
        _engineCondition.value = 100f
        
        selectState(GameState.Driving)
        showNotification("Route Unlocked: ${route.name}. Start engine to drive!")
    }

    // Vehicle custom upgrade/purchase methods
    fun selectActiveVehicle(id: String) {
        if (_ownedVehicleIds.value.contains(id)) {
            _activeVehicleId.value = id
            val activeCap = getActiveVehicle().fuelTankCapacity
            _currentFuel.value = activeCap
            showNotification("Active Vehicle: ${getActiveVehicle().name}")
        }
    }

    fun buyVehicle(v: Vehicle) {
        if (_userCash.value >= v.price) {
            _userCash.value -= v.price
            val nextOwned = _ownedVehicleIds.value.toMutableSet()
            nextOwned.add(v.id)
            _ownedVehicleIds.value = nextOwned
            _activeVehicleId.value = v.id
            
            // set defaults
            val activeCap = getActiveVehicle().fuelTankCapacity
            _currentFuel.value = activeCap
            _engineCondition.value = 100f
            
            showNotification("Successful Purchase! Enjoy your new ${v.name}!")
        } else {
            showNotification("Insufficient funds! Earn cash by completing routes.")
        }
    }

    fun applyCustomPaint(colorHex: Int) {
        if (_userCash.value >= 150) {
            _userCash.value -= 150
            val activeId = _activeVehicleId.value
            val nextColors = _vehiclePaints.value.toMutableMap()
            nextColors[activeId] = colorHex
            _vehiclePaints.value = nextColors
            showNotification("Custom Paint applied! Cost: $150.")
        } else {
            showNotification("Insufficient cash! Needs $150 for paint job.")
        }
    }

    fun upgradeEngine() {
        val activeId = _activeVehicleId.value
        val currentLevel = _vehicleEngineLevels.value[activeId] ?: 0
        if (currentLevel >= 3) {
            showNotification("Engine already fully upgraded to maximum limit!")
            return
        }

        val price = when (currentLevel) {
            0 -> 300
            1 -> 600
            else -> 1200
        }

        if (_userCash.value >= price) {
            _userCash.value -= price
            val nextLevels = _vehicleEngineLevels.value.toMutableMap()
            nextLevels[activeId] = currentLevel + 1
            _vehicleEngineLevels.value = nextLevels
            showNotification("Engine Upgrade Level ${currentLevel + 1} Applied! Cost: $$price")
        } else {
            showNotification("Insufficient cash! Engine upgrade needs $$price.")
        }
    }

    fun playHorn() {
        _hornActive.value = true
        viewModelScope.launch {
            delay(800)
            _hornActive.value = false
        }
        showNotification("HONK! HONK! 🔊")
    }

    fun triggerIndicator(state: IndicatorState) {
        _indicatorState.value = if (_indicatorState.value == state) IndicatorState.None else state
    }

    fun switchCameraAngle() {
        // cycle between 0, 1, 2
        _activeCameraAngle.value = (_activeCameraAngle.value + 1) % 3
        val cameraNames = listOf("Bird's Eye", "Third Person", "Interior & Windshield")
        showNotification("Camera switched to: ${cameraNames[_activeCameraAngle.value]}")
    }

    fun updateSettings(ctrl: ControlType, sens: Float) {
        _controlType.value = ctrl
        _sensitivity.value = sens
        showNotification("Settings Saved: ${ctrl.name} Controls, Sens: $sens")
    }

    // Refuel / Repair Operations
    fun performRefuel() {
        val vehicle = getActiveVehicle()
        val missingFuel = vehicle.fuelTankCapacity - _currentFuel.value
        if (missingFuel <= 1f) {
            showNotification("Fuel tank is already completely full!")
            return
        }

        val cost = (missingFuel * 2.5f).toInt()
        if (_userCash.value >= cost) {
            _userCash.value -= cost
            _currentFuel.value = vehicle.fuelTankCapacity
            showNotification("Fully Refueled! Base price: $$cost")
            _isTerminalArrivalPrompt.value = null
        } else {
            // Partial refuel
            val affordableFuel = _userCash.value / 2.5f
            if (affordableFuel > 1f) {
                _userCash.value = 0
                _currentFuel.value += affordableFuel
                showNotification("Partial Refuel: Added ${affordableFuel.toInt()}L for all remaining cash!")
                _isTerminalArrivalPrompt.value = null
            } else {
                showNotification("No cash available to refuel!")
            }
        }
    }

    fun performServiceRepair() {
        if (_engineCondition.value >= 100f) {
            showNotification("Engine is in top working order! No service required.")
            return
        }

        val repairCost = 350
        if (_userCash.value >= repairCost) {
            _userCash.value -= repairCost
            _engineCondition.value = 100f
            showNotification("Bus Serviced! Engine condition restored to 100%. Cost: $350.")
            _isTerminalArrivalPrompt.value = null
        } else {
            showNotification("Need $350 for full garage repair!")
        }
    }

    // Passenger loading
    fun loadWaitingPassengers() {
        val route = _selectedRoute.value
        if (route != null) {
            _currentPassengersLoaded.value = route.waitingPassengers
            showNotification("Passengers Boarded! ${route.waitingPassengers} travelers ready.")
            _isTerminalArrivalPrompt.value = null
        }
    }

    fun completeActiveRoute() {
        val route = _selectedRoute.value
        if (route != null) {
            val totalEarned = route.priceReward
            _userCash.value += totalEarned
            _currentScore.value += 2000 // Completion score!
            _selectedRoute.value = null
            _currentPassengersLoaded.value = 0
            _isTerminalArrivalPrompt.value = null
            selectState(GameState.Terminal)
            
            // Send simple multiplayer message of success
            addChatMessage("Server", "Driver completed route '${route.name}' earning $$totalEarned!", Color.Green)
            showNotification("Mission Complete! Earned $$totalEarned")
        }
    }

    // Active stats helper
    fun getActiveVehicle(): Vehicle {
        val activeId = _activeVehicleId.value
        val base = garageVehicles.first { it.id == activeId }
        val customColorHex = _vehiclePaints.value[activeId] ?: 0xFFFF3B30.toInt()
        val upgradeLvl = _vehicleEngineLevels.value[activeId] ?: 0
        
        // Multiplier speed based on upgrade Level (0..3)
        val speedMultiplier = 1f + (upgradeLvl * 0.12f)
        return base.copy(
            paintColor = Color(customColorHex),
            engineUpgradeLevel = upgradeLvl,
            maxSpeed = base.maxSpeed * speedMultiplier
        )
    }

    // PHYSICS ENGINE game loop tick (Executes in separate scope at -60fps)
    private fun startGameLoop() {
        stopGameLoop()
        gameLoopJob = viewModelScope.launch {
            var lastTime = System.currentTimeMillis()
            while (true) {
                delay(30) // ~33fps
                val now = System.currentTimeMillis()
                val dt = (now - lastTime) / 1000f
                lastTime = now

                if (!_isPaused.value && _gameState.value == GameState.Driving) {
                    tickPhysics(dt)
                }
            }
        }
    }

    private fun stopGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = null
    }

    // Physical bus simulation calculations
    // steeringAmount ranges from -1f (extreme Left) to 1f (extreme Right)
    // isPedalPressed: 1f (holding accelerator), -1f (braking/reversing), 0f (neutral coasting)
    var inputSteer = 0f
    var inputPedal = 0f

    private fun tickPhysics(dt: Float) {
        if (_isFlipped.value) {
            _speed.value = (_speed.value - 20f * dt).coerceAtLeast(0f)
            return
        }

        val vehicle = getActiveVehicle()
        val conditionMult = if (_engineCondition.value < 20f) 0.5f else 1.0f

        if (_currentFuel.value <= 0f) {
            _engineStarted.value = false
            _currentFuel.value = 0f
        }

        // Steer angle delta
        val computedSensitivity = _sensitivity.value * 120f
        val steerDelta = inputSteer * computedSensitivity * dt
        
        // Spin if we hit oil spill
        if (oilSpillSpinTimer > 0f) {
            oilSpillSpinTimer -= dt
            _playerHeading.value = (_playerHeading.value + 360f * dt) % 360f
        } else if (speed.value > 1f || speed.value < -1f) {
            val facingDirectionFactor = if (_gear.value == Gear.D) 1.0f else -1.0f
            _playerHeading.value = (_playerHeading.value + steerDelta * facingDirectionFactor) % 360f
            if (_playerHeading.value < 0) _playerHeading.value += 360f
        }

        // Acceleration and braking
        val maxSpeed = vehicle.maxSpeed * conditionMult
        var currentSpeed = _speed.value

        if (_engineStarted.value) {
            if (inputPedal > 0f) {
                val accelPower = 18f * (1f + vehicle.engineUpgradeLevel * 0.15f) * conditionMult
                currentSpeed += accelPower * dt
                val fuelBurnRate = 0.08f * (1f + vehicle.engineUpgradeLevel * 0.05f)
                _currentFuel.value = (_currentFuel.value - fuelBurnRate * dt).coerceAtLeast(0f)
            } else if (inputPedal < 0f) {
                // Hard Braking - checking for hard deceleration satisfaction
                if (currentSpeed > 35f) {
                    reducePassengerSatisfaction(2f, "Woah! Watch the brakes/reverse! 🛑")
                }
                currentSpeed -= 45f * dt
            } else {
                currentSpeed -= 4f * dt
            }
        } else {
            currentSpeed -= 8f * dt
        }

        currentSpeed = currentSpeed.coerceIn(-20f, maxSpeed)

        if (_gear.value == Gear.R) {
            if (currentSpeed > 0) currentSpeed -= 40f * dt
            currentSpeed = currentSpeed.coerceAtLeast(-25f)
        } else {
            if (currentSpeed < 0) currentSpeed += 45f * dt
            currentSpeed = currentSpeed.coerceAtLeast(0f)
        }

        _speed.value = currentSpeed

        // Move player
        val headingRadians = (_playerHeading.value * PI / 180f)
        val posScale = 1.6f 
        val velocityVec = currentSpeed * posScale * dt
        
        val velocityX = cos(headingRadians) * velocityVec
        val velocityY = sin(headingRadians) * velocityVec

        val nextX = (_playerX.value + velocityX).toFloat()
        val nextY = (_playerY.value + velocityY).toFloat()

        if (nextX <= 100f || nextX >= 1900f || nextY <= 100f || nextY >= 1900f) {
            _speed.value = -_speed.value * 0.4f
            applyCrashDamage(15f)
            reducePassengerSatisfaction(15f, "Ouch! We hit a boundary wall! 😫")
            triggerScreenImpactEffect()
            showNotification("🚨 Boundary Collision! Engine damaged.")
        } else {
            _playerX.value = nextX
            _playerY.value = nextY
        }

        // ARCADE COLLISION DETECTIONS
        val pX = _playerX.value
        val pY = _playerY.value

        // Coin Collection
        val updatedCoins = _collectableCoins.value.map { coin ->
            if (!coin.collected) {
                val dx = coin.x - pX
                val dy = coin.y - pY
                val dist = sqrt(dx * dx + dy * dy)
                if (dist < 40f) {
                    _userCash.value += coin.value
                    _currentScore.value += 300
                    _numberOfCoinsCollected.value = _numberOfCoinsCollected.value + 1
                    triggerScoreFloatingText("+$${coin.value}", coin.x, coin.y)
                    showNotification("🪙 Picked up gold coin: +$${coin.value}!")
                    coin.copy(collected = true)
                } else {
                    coin
                }
            } else {
                coin
            }
        }
        _collectableCoins.value = updatedCoins

        // Hazard Collision
        val nowTime = System.currentTimeMillis()
        val currentHazards = _roadHazards.value
        currentHazards.forEach { hazard ->
            if (nowTime - hazard.hitTime > 2500) {
                val dx = hazard.x - pX
                val dy = hazard.y - pY
                val dist = sqrt(dx * dx + dy * dy)
                if (dist < 42f) {
                    hazard.hitTime = nowTime
                    triggerScreenImpactEffect()
                    when (hazard.type) {
                        HazardType.SafetyCone -> {
                            applyCrashDamage(5f)
                            reducePassengerSatisfaction(8f, "Dodge those safety traffic cones! 🚧")
                            showNotification("💥 safety cone hit!")
                        }
                        HazardType.RoadBarrier -> {
                            _speed.value = -_speed.value * 0.3f
                            applyCrashDamage(20f)
                            reducePassengerSatisfaction(18f, "SMASH! Watch out for road barricades! 🤕")
                            showNotification("🚨 Smashed Road Barricade!")
                        }
                        HazardType.OilSpill -> {
                            oilSpillSpinTimer = 1.0f
                            reducePassengerSatisfaction(5f, "Aaaah! Oil spill slick spin! 🌀")
                            showNotification("⚠️ Spun out on oil slip!")
                        }
                    }
                }
            }
        }

        // High-speed nice driver reward
        if (currentSpeed > 50f && Random.nextInt(150) == 42) {
            _passengerSpeak.value = listOf(
                "Loving this fast pace! 🚀",
                "Excellent speed, great job! ⏱️",
                "Such an exciting ride, 5 stars! ⭐⭐⭐⭐⭐"
            ).random()
            _passengerSatisfaction.value = (_passengerSatisfaction.value + 4f).coerceIn(0f, 100f)
            _currentScore.value += 150
            viewModelScope.launch {
                delay(2200)
                _passengerSpeak.value = null
            }
        }

        // High score updates
        if (_currentScore.value > _highScore.value) {
            _highScore.value = _currentScore.value
        }

        checkNearbyLandmarks()
        checkTrafficLightViolations()

        if (absSteer() > 0.8f && currentSpeed > 85f && Random.nextInt(150) == 7) {
            _isFlipped.value = true
            reducePassengerSatisfaction(35f, "AAAAH! We overturned! HELP! 😭")
            showNotification("⚠️ OVERTURN! Bus has flipped! Use Pause -> SOS rescue!")
        }
    }

    private fun absSteer(): Float = if (inputSteer < 0) -inputSteer else inputSteer

    // Evaluate proximity with landmarks (Petrol, Service, or Route Stops)
    private fun checkNearbyLandmarks() {
        val px = _playerX.value
        val py = _playerY.value

        var foundOverlayPrompt: String? = null

        for (lm in landmarks) {
            val dx = lm.x - px
            val dy = lm.y - py
            val dist = sqrt(dx * dx + dy * dy)
            
            if (dist < 75f && _speed.value <= 3f) {
                // Player stopped close to a landmark
                when (lm.type) {
                    LandmarkType.PetrolStation -> {
                        foundOverlayPrompt = "PETROL_STATION:${lm.name}"
                    }
                    LandmarkType.ServiceShop -> {
                        foundOverlayPrompt = "SERVICE_SHOP:${lm.name}"
                    }
                    LandmarkType.Terminal -> {
                        // Check if it is start or destination of current route!
                        val activeRoute = _selectedRoute.value
                        if (activeRoute != null) {
                            if (lm.name.lowercase().contains(activeRoute.startStation.lowercase()) && _currentPassengersLoaded.value == 0) {
                                foundOverlayPrompt = "ROUTE_START:${activeRoute.startStation}"
                            } else if (lm.name.lowercase().contains(activeRoute.endStation.lowercase()) && _currentPassengersLoaded.value > 0) {
                                foundOverlayPrompt = "ROUTE_END:${activeRoute.endStation}"
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
        
        _isTerminalArrivalPrompt.value = foundOverlayPrompt
    }

    // Traffic signal zone compliance
    private fun checkTrafficLightViolations() {
        // Test if player crosses any signal bounding area while the light is strictly RED at mid-to-high speeds
        if (_speed.value < 10f) return

        val px = _playerX.value
        val py = _playerY.value

        for (sig in _trafficSignals.value) {
            if (sig.state == SignalColor.Red) {
                val dx = sig.x - px
                val dy = sig.y - py
                val dist = sqrt(dx * dx + dy * dy)
                if (dist < 45f) {
                    // Signal infringement! Deduct small cash fine and sound alert
                    _userCash.value = (_userCash.value - 120).coerceAtLeast(0)
                    reducePassengerSatisfaction(10f, "Oh no! Red light crossing violation! 🚦🚨")
                    showNotification("🚨 Fine Issued: -$120 for Crossing Signal on Red! Keep city safe.")
                }
            }
        }
    }

    private fun applyCrashDamage(dmg: Float) {
        _engineCondition.value = (_engineCondition.value - dmg).coerceAtLeast(5f)
    }

    private fun showNotification(msg: String) {
        _notificationLog.value = msg
        viewModelScope.launch {
            delay(2500)
            if (_notificationLog.value == msg) {
                _notificationLog.value = null
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopGameLoop()
    }
}
