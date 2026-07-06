package pt.caitano.jetpackcityads

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

// 3D Math Representation
data class Vector3D(val x: Float, val y: Float, val z: Float) {
    operator fun plus(other: Vector3D) = Vector3D(x + other.x, y + other.y, z + other.z)
    operator fun minus(other: Vector3D) = Vector3D(x - other.x, y - other.y, z - other.z)
    operator fun times(factor: Float) = Vector3D(x * factor, y * factor, z * factor)
    fun length(): Float = sqrt(x * x + y * y + z * z)
    fun dist(other: Vector3D): Float = (this - other).length()
}

// Game State Enum
enum class GameState {
    MENU,
    FLIGHT,
    PAUSED,
    VICTORY,
    GAME_OVER,
    UPGRADES,
    SKINS,
    MOCK_AD
}

// Low-Poly Game Entities
data class Building(
    val id: Int,
    val x: Float,
    val z: Float,
    val widthX: Float,
    val widthZ: Float,
    val height: Float,
    val color: Long,
    val adText: String? = null,
    val adSponsor: String? = null
)

data class Car(
    var x: Float,
    val z: Float,
    val color: Long,
    val speed: Float,
    val dirX: Float,
    val minX: Float,
    val maxX: Float
)

data class Tree3D(
    val x: Float,
    val z: Float,
    val height: Float,
    val trunkColor: Long = 0xFF5D4037, // Brown
    val leafColor: Long = 0xFF2E7D32  // Dark Green
)

data class FuelCanister(
    val id: Int,
    val pos: Vector3D,
    var collected: Boolean = false,
    var rotationAngle: Float = 0f
)

data class AdBeacon(
    val id: Int,
    val pos: Vector3D,
    var completed: Boolean = false,
    val sponsorName: String,
    val message: String
)

data class Particle(
    var pos: Vector3D,
    var vel: Vector3D,
    var life: Float, // 1.0 down to 0.0
    val maxLife: Float,
    val color: Long,
    val size: Float
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPrefs = application.getSharedPreferences("jetpack_city_ads_prefs", Context.MODE_PRIVATE)

    // Game variables state flow
    private val _gameState = MutableStateFlow(GameState.MENU)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    // Player position, velocity, and rotation
    private val _playerPosition = MutableStateFlow(Vector3D(0f, 25f, -120f))
    val playerPosition: StateFlow<Vector3D> = _playerPosition.asStateFlow()

    private val _playerRotation = MutableStateFlow(0f) // Yaw rotation in radians
    val playerRotation: StateFlow<Float> = _playerRotation.asStateFlow()

    private val _playerVelocity = MutableStateFlow(Vector3D(0f, 0f, 0f))
    val playerVelocity: StateFlow<Vector3D> = _playerVelocity.asStateFlow()

    // Upgrades
    private val _fuelLevel = MutableStateFlow(sharedPrefs.getInt("upgrade_fuel", 1))
    val fuelLevel: StateFlow<Int> = _fuelLevel.asStateFlow()

    private val _speedLevel = MutableStateFlow(sharedPrefs.getInt("upgrade_speed", 1))
    val speedLevel: StateFlow<Int> = _speedLevel.asStateFlow()

    private val _turboLevel = MutableStateFlow(sharedPrefs.getInt("upgrade_turbo", 1))
    val turboLevel: StateFlow<Int> = _turboLevel.asStateFlow()

    // Currency and Progress
    private val _coins = MutableStateFlow(sharedPrefs.getInt("player_coins", 50)) // Start with 50 coins to help onboarding
    val coins: StateFlow<Int> = _coins.asStateFlow()

    private val _activeSkin = MutableStateFlow(sharedPrefs.getString("active_skin", "Classic Red") ?: "Classic Red")
    val activeSkin: StateFlow<String> = _activeSkin.asStateFlow()

    private val _unlockedSkins = MutableStateFlow(
        sharedPrefs.getStringSet("unlocked_skins", setOf("Classic Red")) ?: setOf("Classic Red")
    )
    val unlockedSkins: StateFlow<Set<String>> = _unlockedSkins.asStateFlow()

    // Game stats inside active run
    private val _fuel = MutableStateFlow(100f)
    val fuel: StateFlow<Float> = _fuel.asStateFlow()

    private val _turboCharge = MutableStateFlow(100f)
    val turboCharge: StateFlow<Float> = _turboCharge.asStateFlow()

    private val _isTurboActive = MutableStateFlow(false)
    val isTurboActive: StateFlow<Boolean> = _isTurboActive.asStateFlow()

    private val _scoreThisRun = MutableStateFlow(0)
    val scoreThisRun: StateFlow<Int> = _scoreThisRun.asStateFlow()

    // Entities in the game
    val buildings = mutableListOf<Building>()
    val cars = mutableListOf<Car>()
    val trees = mutableListOf<Tree3D>()

    private val _fuelCanisters = MutableStateFlow<List<FuelCanister>>(emptyList())
    val fuelCanisters: StateFlow<List<FuelCanister>> = _fuelCanisters.asStateFlow()

    private val _adBeacons = MutableStateFlow<List<AdBeacon>>(emptyList())
    val adBeacons: StateFlow<List<AdBeacon>> = _adBeacons.asStateFlow()

    private val _particles = MutableStateFlow<List<Particle>>(emptyList())
    val particles: StateFlow<List<Particle>> = _particles.asStateFlow()

    // Feedback messages (e.g. "+$50", "Fuel Refilled!")
    private val _hudMessage = MutableStateFlow<String?>(null)
    val hudMessage: StateFlow<String?> = _hudMessage.asStateFlow()

    // Virtual Joystick coordinates (input)
    private var joystickX = 0f
    private var joystickY = 0f
    private var isPressingUp = false
    private var isPressingDown = false

    // Game loop thread/job
    private var gameLoopJob: Job? = null

    // Configuration limits based on upgrades
    val maxFuel: Float get() = 100f + (fuelLevel.value - 1) * 35f
    val normalSpeed: Float get() = 0.5f + (speedLevel.value - 1) * 0.15f
    val maxTurboDuration: Float get() = 100f + (turboLevel.value - 1) * 40f

    // Ad Simulation helpers
    private val _adCountdown = MutableStateFlow(3)
    val adCountdown: StateFlow<Int> = _adCountdown.asStateFlow()
    private var pendingAdAction: (() -> Unit)? = null

    init {
        generateWorld()
    }

    private fun generateWorld() {
        buildings.clear()
        cars.clear()
        trees.clear()

        // Generate building grid: 6x6 grid with streets
        // Coordinates -180 to 180 step 72
        var buildingId = 1
        val sponsors = listOf(
            Pair("BYTE COLA", "Refresque seu voo com zero açúcar!"),
            Pair("ORBIT BURGER", "O hambúrguer mais espacial da cidade!"),
            Pair("PIXEL SHOES", "Calçados leves que fazem você flutuar!"),
            Pair("CYBER BANK", "O banco digital da nova era com rendimento!"),
            Pair("GIGA GAMES", "A melhor jogabilidade em realidade virtual!")
        )

        var sponsorIdx = 0

        for (gridX in -2..3) {
            val bx = gridX * 65f
            for (gridZ in -2..3) {
                val bz = gridZ * 65f

                // Randomize building heights, styles, and give some of them ads
                val height = Random.nextFloat() * 55f + 25f
                val wX = Random.nextFloat() * 10f + 16f
                val wZ = Random.nextFloat() * 10f + 16f

                // Material 3 high contrast colors
                val colors = listOf(
                    0xFF1E293B, // Slate Dark
                    0xFF334155, // Slate Slate
                    0xFF0F172A, // Dark Blue
                    0xFF1E40AF, // Deep Blue
                    0xFF0369A1, // Sky Blue
                    0xFF0D9488, // Teal
                    0xFF15803D, // Forest Green
                    0xFFB45309, // Orange Amber
                    0xFF6B21A8, // Violet
                    0xFF831843  // Maroon Rose
                )
                val color = colors[Random.nextInt(colors.size)]

                // Select 5 specific blocks to hold Ads
                var adText: String? = null
                var adSponsor: String? = null
                if ((gridX == -1 && gridZ == -1) || (gridX == 1 && gridZ == -2) || 
                    (gridX == 0 && gridZ == 2) || (gridX == 2 && gridZ == 0) || 
                    (gridX == -2 && gridZ == 1)) {
                    val sp = sponsors[sponsorIdx % sponsors.size]
                    adSponsor = sp.first
                    adText = sp.second
                    sponsorIdx++
                }

                buildings.add(Building(buildingId++, bx, bz, wX, wZ, height, color, adText, adSponsor))

                // Add surrounding trees in blocks or pavements
                if (Random.nextFloat() > 0.65f) {
                    trees.add(Tree3D(bx + wX/2 + 8f, bz + wZ/2 + 8f, Random.nextFloat() * 6f + 4f))
                }
                if (Random.nextFloat() > 0.65f) {
                    trees.add(Tree3D(bx - wX/2 - 8f, bz - wZ/2 - 8f, Random.nextFloat() * 6f + 4f))
                }
            }
        }

        // Add 6 simple cars driving on main streets (X limits)
        val carColors = listOf(0xFFDC2626, 0xFFEAB308, 0xFF2563EB, 0xFF16A34A, 0xFFD946EF)
        for (i in 1..6) {
            val isZStreet = i % 2 == 0
            val streetPos = (i - 3) * 65f + 32.5f
            val color = carColors[Random.nextInt(carColors.size)]
            val speed = Random.nextFloat() * 0.4f + 0.3f
            cars.add(Car(
                x = if (isZStreet) streetPos else Random.nextFloat() * 300f - 150f,
                z = if (isZStreet) Random.nextFloat() * 300f - 150f else streetPos,
                color = color,
                speed = speed,
                dirX = if (isZStreet) 0f else 1f,
                minX = -180f,
                maxX = 180f
            ))
        }
    }

    fun startRun() {
        _gameState.value = GameState.FLIGHT
        _playerPosition.value = Vector3D(0f, 25f, -120f)
        _playerRotation.value = 0f
        _playerVelocity.value = Vector3D(0f, 0f, 0f)
        _fuel.value = maxFuel
        _turboCharge.value = maxTurboDuration
        _isTurboActive.value = false
        _scoreThisRun.value = 0

        // Reset particles
        _particles.value = emptyList()

        // Generate 3 active Beacons representing billboards to broadcast ads
        val selectedAdsBuildings = buildings.filter { it.adText != null }
        val beacons = selectedAdsBuildings.mapIndexed { idx, building ->
            AdBeacon(
                id = idx + 1,
                pos = Vector3D(building.x, building.height + 4f, building.z),
                completed = false,
                sponsorName = building.adSponsor ?: "ANÚNCIO",
                message = building.adText ?: "Ative a publicidade!"
            )
        }
        _adBeacons.value = beacons

        // Generate 5 fuel canisters spread around
        val canisters = mutableListOf<FuelCanister>()
        for (i in 1..6) {
            val randomBuilding = buildings[Random.nextInt(buildings.size)]
            // Place canisters either on building roofs or floating high in the sky over street junctions
            val canisterX = randomBuilding.x + (Random.nextFloat() * 30f - 15f)
            val canisterZ = randomBuilding.z + (Random.nextFloat() * 30f - 15f)
            val canisterY = Random.nextFloat() * 40f + 5f
            canisters.add(FuelCanister(id = i, pos = Vector3D(canisterX, canisterY, canisterZ)))
        }
        _fuelCanisters.value = canisters

        // Launch Game Physics Loop
        startGameLoop()
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            while (_gameState.value == GameState.FLIGHT) {
                delay(16) // Roughly 60 FPS
                tickPhysics()
            }
        }
    }

    private fun tickPhysics() {
        val currentPos = _playerPosition.value
        val currentVel = _playerVelocity.value
        var currentRot = _playerRotation.value

        // 1. Steering & Rotation
        // O joystick esquerdo agora move de verdade em 4 direções.
        // X também vira levemente a câmera para dar sensação de voo, mas não depende só de rotação.
        val rotationSensitivity = 0.018f
        currentRot += joystickX * rotationSensitivity
        _playerRotation.value = currentRot

        // 2. Linear Forces
        // Y = frente/trás, X = esquerda/direita, ambos relativos à direção da câmera.
        val headingX = sin(currentRot)
        val headingZ = cos(currentRot)
        val rightX = cos(currentRot)
        val rightZ = -sin(currentRot)

        // Select speed factor based on upgraded engine
        var speedMultiplier = normalSpeed
        if (_isTurboActive.value && _turboCharge.value > 0f) {
            speedMultiplier *= 2.0f
            _turboCharge.value = (_turboCharge.value - 1.2f).coerceAtLeast(0f)
            if (_turboCharge.value <= 0f) {
                _isTurboActive.value = false
            }
        } else {
            // Passive recharge when not in turbo
            _turboCharge.value = (_turboCharge.value + 0.3f).coerceAtMost(maxTurboDuration)
        }

        // Apply skin-specific bonus (Gold jetpack consumes 35% less fuel, cyber particles are brighter)
        val fuelConsumptionFactor = if (_activeSkin.value == "Gold Royalty") 0.65f else 1.0f

        // Fuel Depletion (only if flying/running jetpack)
        val isMovingHorizontally = kotlin.math.abs(joystickX) > 0.03f || kotlin.math.abs(joystickY) > 0.03f
        val isEngineOn = isMovingHorizontally || isPressingUp || isPressingDown || _isTurboActive.value
        val fuelCost = if (_isTurboActive.value) 0.38f else (if (isEngineOn) 0.14f else 0.04f)
        _fuel.value = (_fuel.value - fuelCost * fuelConsumptionFactor).coerceAtLeast(0f)

        if (_fuel.value <= 0f) {
            // Out of Fuel!
            _gameState.value = GameState.GAME_OVER
            showHUDMessage("Sem combustível!")
            return
        }

        // Apply velocities
        val targetVelX = (headingX * joystickY + rightX * joystickX) * speedMultiplier
        val targetVelZ = (headingZ * joystickY + rightZ * joystickX) * speedMultiplier

        // Interpolate horizontal velocity for smooth gliding inertia
        val drag = 0.15f
        val vx = currentVel.x + (targetVelX - currentVel.x) * drag
        val vz = currentVel.z + (targetVelZ - currentVel.z) * drag

        // Vertical Movement (Up/Down buttons)
        var vy = currentVel.y
        val hoverDamping = 0.86f
        val jetpackLift = 0.24f
        val descendForce = -0.22f

        if (isPressingUp) {
            vy += jetpackLift
            // Generate emission sparks
            generateFlameParticles(currentPos, currentRot, isTurbo = _isTurboActive.value)
        } else if (isPressingDown) {
            vy += descendForce
        } else {
            // No MVP o boneco paira no ar em vez de cair para o chão.
            // Isso deixa claro que ele está a voar e torna o comando jogável no telemóvel.
            vy *= hoverDamping
        }

        // Limit vertical speeds
        vy = vy.coerceIn(-1.2f, 1.2f)

        // Proposed new player coordinates
        var newX = currentPos.x + vx
        var newY = currentPos.y + vy
        var newZ = currentPos.z + vz

        // Bounding limits
        newX = newX.coerceIn(-230f, 230f)
        newZ = newZ.coerceIn(-230f, 230f)
        if (newY < 1.5f) {
            newY = 1.5f
            vy = 0f // reset vertical speed on floor
        }
        if (newY > 105f) {
            newY = 105f
            vy = 0f
        }

        // 3. Collision Check: AABB (Axis-Aligned Bounding Box) for buildings
        var hasCollision = false
        val playerRadius = 1.8f
        for (building in buildings) {
            val halfW = building.widthX / 2f
            val halfD = building.widthZ / 2f

            val minBX = building.x - halfW - playerRadius
            val maxBX = building.x + halfW + playerRadius
            val minBZ = building.z - halfD - playerRadius
            val maxBZ = building.z + halfD + playerRadius

            // Check horizontal collision
            if (newX in minBX..maxBX && newZ in minBZ..maxBZ) {
                // Check height collision
                if (newY <= building.height + 0.5f) {
                    hasCollision = true
                    break
                }
            }
        }

        // If collided, slide along building walls or revert to previous safe coordinates
        if (!hasCollision) {
            _playerPosition.value = Vector3D(newX, newY, newZ)
            _playerVelocity.value = Vector3D(vx, vy, vz)
        } else {
            // Elastic collision bounce-back
            _playerVelocity.value = Vector3D(-vx * 0.4f, vy * 0.2f, -vz * 0.4f)
            // Generate crash smoke sparks
            generateCrashParticles(currentPos)
        }

        // 4. Update Cars positions along streets
        for (car in cars) {
            car.x += car.speed * car.dirX
            if (car.x > car.maxX) car.x = car.minX
        }

        // 5. Update Entity Spins & Collision Collects (Fuel / Beacons)
        val canisters = _fuelCanisters.value.map { canister ->
            canister.copy(rotationAngle = (canister.rotationAngle + 3f) % 360f)
        }
        _fuelCanisters.value = canisters

        // Check fuel canister collections
        for (canister in canisters) {
            if (!canister.collected && currentPos.dist(canister.pos) < 6.5f) {
                canister.collected = true
                _fuel.value = (_fuel.value + 40f).coerceAtMost(maxFuel)
                showHUDMessage("Combustível Coletado! +40")
                // Earn small bonus coins
                _coins.value += 5
                saveCoinsToPrefs()
            }
        }

        // Check active Advertising Beacons
        val beacons = _adBeacons.value
        var anyBeaconStateChanged = false
        for (beacon in beacons) {
            if (!beacon.completed && currentPos.dist(beacon.pos) < 14.5f) {
                beacon.completed = true
                anyBeaconStateChanged = true
                val reward = 120 + (speedLevel.value * 20) // Bigger payout as speed tier increases
                _coins.value += reward
                _scoreThisRun.value += 1
                saveCoinsToPrefs()
                showHUDMessage("Anúncio Transmitido! +$reward para ${beacon.sponsorName}")

                // Generate celebration fireworks
                generateVictoryParticles(beacon.pos)
            }
        }
        if (anyBeaconStateChanged) {
            _adBeacons.value = beacons.toList()

            // Check Victory Condition (All 3 beacons completed)
            if (beacons.all { it.completed }) {
                _gameState.value = GameState.VICTORY
                showHUDMessage("Missão Cumprida! Parabéns!")
            }
        }

        // 6. Particle Physics Updates
        updateParticles()
    }

    // Joystick Inputs
    fun updateJoystick(x: Float, y: Float) {
        joystickX = x.coerceIn(-1f, 1f)
        joystickY = y.coerceIn(-1f, 1f)
    }

    fun setUpDownForces(up: Boolean, down: Boolean) {
        isPressingUp = up
        isPressingDown = down
    }

    fun toggleTurbo(active: Boolean) {
        if (active && _turboCharge.value > 10f) {
            _isTurboActive.value = true
        } else {
            _isTurboActive.value = false
        }
    }

    // Upgrade System
    fun purchaseUpgrade(type: String) {
        val coinBalance = _coins.value
        when (type) {
            "fuel" -> {
                val currentLvl = _fuelLevel.value
                if (currentLvl < 5) {
                    val cost = currentLvl * 80
                    if (coinBalance >= cost) {
                        _coins.value -= cost
                        _fuelLevel.value += 1
                        saveUpgrades()
                    }
                }
            }
            "speed" -> {
                val currentLvl = _speedLevel.value
                if (currentLvl < 5) {
                    val cost = currentLvl * 90
                    if (coinBalance >= cost) {
                        _coins.value -= cost
                        _speedLevel.value += 1
                        saveUpgrades()
                    }
                }
            }
            "turbo" -> {
                val currentLvl = _turboLevel.value
                if (currentLvl < 5) {
                    val cost = currentLvl * 100
                    if (coinBalance >= cost) {
                        _coins.value -= cost
                        _turboLevel.value += 1
                        saveUpgrades()
                    }
                }
            }
        }
    }

    // Skin customization
    fun purchaseSkin(skinName: String, cost: Int) {
        val coinBalance = _coins.value
        if (coinBalance >= cost && !_unlockedSkins.value.contains(skinName)) {
            _coins.value -= cost
            val newSet = _unlockedSkins.value.toMutableSet().apply { add(skinName) }
            _unlockedSkins.value = newSet
            _activeSkin.value = skinName

            sharedPrefs.edit()
                .putStringSet("unlocked_skins", newSet)
                .putString("active_skin", skinName)
                .putInt("player_coins", _coins.value)
                .apply()
        }
    }

    fun selectSkin(skinName: String) {
        if (_unlockedSkins.value.contains(skinName)) {
            _activeSkin.value = skinName
            sharedPrefs.edit().putString("active_skin", skinName).apply()
        }
    }

    // Save helpers
    private fun saveUpgrades() {
        sharedPrefs.edit()
            .putInt("upgrade_fuel", _fuelLevel.value)
            .putInt("upgrade_speed", _speedLevel.value)
            .putInt("upgrade_turbo", _turboLevel.value)
            .putInt("player_coins", _coins.value)
            .apply()
    }

    private fun saveCoinsToPrefs() {
        sharedPrefs.edit().putInt("player_coins", _coins.value).apply()
    }

    // Particle engines
    private fun generateFlameParticles(pos: Vector3D, yawAngle: Float, isTurbo: Boolean) {
        val particleList = _particles.value.toMutableList()
        val particleCount = if (isTurbo) 5 else 2

        // Select colors based on skin
        val colorHex = when (_activeSkin.value) {
            "Cyber Neon" -> 0xFF00FFFF // Cyan
            "Gold Royalty" -> 0xFFFFD700 // Golden Yellow
            else -> 0xFFFF4500 // Orange Red
        }

        for (i in 1..particleCount) {
            // Jetpack nozzles are slightly behind player
            val dx = -sin(yawAngle) * 1.5f + (Random.nextFloat() * 0.4f - 0.2f)
            val dz = -cos(yawAngle) * 1.5f + (Random.nextFloat() * 0.4f - 0.2f)
            val pPos = Vector3D(pos.x + dx, pos.y - 1.2f, pos.z + dz)

            val pVel = Vector3D(
                x = (Random.nextFloat() * 0.2f - 0.1f) - sin(yawAngle) * 0.1f,
                y = -0.4f - Random.nextFloat() * 0.3f,
                z = (Random.nextFloat() * 0.2f - 0.1f) - cos(yawAngle) * 0.1f
            )

            particleList.add(Particle(pPos, pVel, 1f, 1f, colorHex, Random.nextFloat() * 4f + 3f))
        }
        _particles.value = particleList.take(60) // Keep particle count clean and optimized
    }

    private fun generateCrashParticles(pos: Vector3D) {
        val particleList = _particles.value.toMutableList()
        for (i in 1..15) {
            val pVel = Vector3D(
                x = Random.nextFloat() * 0.8f - 0.4f,
                y = Random.nextFloat() * 0.8f - 0.2f,
                z = Random.nextFloat() * 0.8f - 0.4f
            )
            particleList.add(Particle(pos, pVel, 1f, 1f, 0xFF7F8C8D, Random.nextFloat() * 6f + 4f)) // Grey smoke
        }
        _particles.value = particleList.take(60)
    }

    private fun generateVictoryParticles(pos: Vector3D) {
        val particleList = _particles.value.toMutableList()
        val festiveColors = listOf(0xFFFF007F, 0xFF00FFCC, 0xFFFFFF00, 0xFF9933FF, 0xFF00FF00)
        for (i in 1..25) {
            val pVel = Vector3D(
                x = Random.nextFloat() * 1.5f - 0.75f,
                y = Random.nextFloat() * 1.5f - 0.2f,
                z = Random.nextFloat() * 1.5f - 0.75f
            )
            val color = festiveColors[Random.nextInt(festiveColors.size)]
            particleList.add(Particle(pos, pVel, 1f, 1f, color, Random.nextFloat() * 8f + 5f))
        }
        _particles.value = particleList.take(100)
    }

    private fun updateParticles() {
        val active = _particles.value.mapNotNull { p ->
            val nextLife = p.life - 0.04f
            if (nextLife <= 0f) null
            else {
                p.copy(
                    pos = p.pos + p.vel,
                    life = nextLife
                )
            }
        }
        _particles.value = active
    }

    private fun showHUDMessage(msg: String) {
        viewModelScope.launch {
            _hudMessage.value = msg
            delay(2800)
            if (_hudMessage.value == msg) {
                _hudMessage.value = null
            }
        }
    }

    // Simulated Ad Mob integration methods
    fun triggerInterstitialAd(onClosed: () -> Unit) {
        _gameState.value = GameState.MOCK_AD
        _adCountdown.value = 3
        pendingAdAction = onClosed

        viewModelScope.launch {
            while (_adCountdown.value > 0 && _gameState.value == GameState.MOCK_AD) {
                delay(1000)
                _adCountdown.value -= 1
            }
            closeMockAd()
        }
    }

    fun triggerRewardedAd(onEarned: () -> Unit) {
        _gameState.value = GameState.MOCK_AD
        _adCountdown.value = 5 // Rewarded ads take slightly longer
        pendingAdAction = onEarned

        viewModelScope.launch {
            while (_adCountdown.value > 0 && _gameState.value == GameState.MOCK_AD) {
                delay(1000)
                _adCountdown.value -= 1
            }
            closeMockAd()
        }
    }

    private fun closeMockAd() {
        val action = pendingAdAction
        pendingAdAction = null
        if (_gameState.value == GameState.MOCK_AD) {
            _gameState.value = GameState.FLIGHT
            action?.invoke()
            // Resume physics loop if we are back in flight
            startGameLoop()
        }
    }

    fun returnToMenu() {
        gameLoopJob?.cancel()
        _gameState.value = GameState.MENU
    }

    fun openShop() {
        _gameState.value = GameState.UPGRADES
    }

    fun openSkins() {
        _gameState.value = GameState.SKINS
    }

    override fun onCleared() {
        super.onCleared()
        gameLoopJob?.cancel()
    }
}
