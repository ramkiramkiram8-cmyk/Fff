package com.example.game

import androidx.compose.ui.graphics.Color

enum class GameState {
    MainMenu,
    Terminal,
    Garage,
    Driving,
    ServiceShop,
    Settings
}

enum class ControlType {
    Steering,
    Tilt,
    Touch
}

enum class Gear {
    D, // Drive
    R  // Reverse
}

enum class IndicatorState {
    None,
    Left,
    Right,
    Hazard
}

data class Route(
    val id: Int,
    val name: String,
    val startStation: String,
    val endStation: String,
    val distanceKm: Float,
    val priceReward: Int,
    val waitingPassengers: Int,
    val arrivalTimeText: String, // e.g. "2 min", "5 min"
    val startX: Float,
    val startY: Float,
    val endX: Float,
    val endY: Float
)

data class Vehicle(
    val id: String,
    val name: String,
    val isBus: Boolean,
    val price: Int,
    val maxSpeed: Float,
    val fuelTankCapacity: Float,
    val baseHandling: Float,
    val description: String,
    val paintColor: Color = Color.Red,
    val engineUpgradeLevel: Int = 0
)

data class MultiplayerFriend(
    val id: String,
    val name: String,
    val x: Float,
    val y: Float,
    val speed: Float,
    val paintColor: Color,
    val isBus: Boolean,
    val targetX: Float,
    val targetY: Float
)

data class CityTrafficSignal(
    val id: Int,
    val x: Float,
    val y: Float,
    var state: SignalColor,
    var timerSeconds: Int
)

enum class SignalColor {
    Green,
    Yellow,
    Red
}

data class Landmark(
    val name: String,
    val type: LandmarkType,
    val x: Float,
    val y: Float,
    val width: Float = 100f,
    val height: Float = 100f,
    val emoji: String
)

enum class LandmarkType {
    Terminal,
    PetrolStation,
    ServiceShop,
    Building,
    ScenicSpot
}

data class ChatMessage(
    val sender: String,
    val message: String,
    val color: Color
)

// Arcade Gameplay additions: Gold Coins to collect and tactical road Hazards to dodge
data class CollectableCoin(
    val id: Int,
    val x: Float,
    val y: Float,
    val value: Int = 100,
    var collected: Boolean = false,
    val size: Float = 30f
)

data class RoadHazard(
    val id: Int,
    val type: HazardType,
    val x: Float,
    val y: Float,
    var hitTime: Long = 0L,
    val size: Float = 35f
)

enum class HazardType {
    SafetyCone,
    RoadBarrier,
    OilSpill
}

