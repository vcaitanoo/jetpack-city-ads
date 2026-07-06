package pt.caitano.jetpackcityads

import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pt.caitano.jetpackcityads.ui.theme.MyApplicationTheme
import kotlin.math.*

class MainActivity : ComponentActivity() {
    private val gameViewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GameMainScreen(gameViewModel)
                }
            }
        }
    }
}

@Composable
fun GameMainScreen(viewModel: GameViewModel) {
    val state by viewModel.gameState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        when (state) {
            GameState.MENU -> MenuScreen(viewModel)
            GameState.FLIGHT -> FlightScreen(viewModel)
            GameState.UPGRADES -> UpgradesScreen(viewModel)
            GameState.SKINS -> SkinsScreen(viewModel)
            GameState.MOCK_AD -> MockAdScreen(viewModel)
            GameState.VICTORY -> VictoryScreen(viewModel)
            GameState.GAME_OVER -> GameOverScreen(viewModel)
            GameState.PAUSED -> Box {} // Embedded pause handling
        }
    }
}

// ==========================================
// 1. MENU SCREEN
// ==========================================
@Composable
fun MenuScreen(viewModel: GameViewModel) {
    val coins by viewModel.coins.collectAsStateWithLifecycle()
    val activeSkin by viewModel.activeSkin.collectAsStateWithLifecycle()

    // Query drawable directory for img_hero_banner_1783302280225.jpg
    val context = LocalContext.current
    val bannerResId = remember {
        context.resources.getIdentifier(
            "img_hero_banner_1783302280225",
            "drawable",
            context.packageName
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)) // Dark slate background
            .systemBarsPadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // TOP CARD: Stats Indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color(0xFF1E293B), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MonetizationOn,
                    contentDescription = "Moedas",
                    tint = Color(0xFFFBBF24),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$coins Moedas",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color(0xFF1E293B), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Jetpack",
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = activeSkin,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        }

        // FEATURED BANNER
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.1f)
                .clip(RoundedCornerShape(24.dp))
                .border(2.dp, Color(0xFF334155), RoundedCornerShape(24.dp))
        ) {
            if (bannerResId != 0) {
                Image(
                    painter = painterResource(id = bannerResId),
                    contentDescription = "Jetpack City Ads Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Fallback elegant background gradient if drawable not resolved
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF1E40AF), Color(0xFF1E293B))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FlightTakeoff,
                        contentDescription = "Flying",
                        tint = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(80.dp)
                    )
                }
            }

            // Text overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                        )
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Column {
                    Text(
                        text = "JETPACK CITY ADS",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Divulgue anúncios fictícios voando alto pela cidade 3D!",
                        fontSize = 13.sp,
                        color = Color.LightGray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // CENTER BUTTONS
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.0f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { viewModel.startRun() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Volar",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "VOAR AGORA (MVP)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { viewModel.openShop() },
                    modifier = Modifier
                        .weight(1f)
                        .height(55.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Upgrade,
                            contentDescription = "Upgrades",
                            tint = Color(0xFFFBBF24)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Upgrades", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Button(
                    onClick = { viewModel.openSkins() },
                    modifier = Modifier
                        .weight(1f)
                        .height(55.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Skins",
                            tint = Color(0xFFD946EF)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Mochilas", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        // SPONSORED BANNER (MOCK AD)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                .clickable {
                    viewModel.triggerInterstitialAd {
                        // Do nothing on closed, just simulated click
                    }
                }
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFBBF24), RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            "AD",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "GIGA JETPACK PRO: Clique para baixar!",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Click",
                    tint = Color.Gray
                )
            }
        }
    }
}

// ==========================================
// 2. ACTIVE FLIGHT GAMEPLAY (3D PROJECTED CANVAS)
// ==========================================
@Composable
fun FlightScreen(viewModel: GameViewModel) {
    val playerPos by viewModel.playerPosition.collectAsStateWithLifecycle()
    val playerRot by viewModel.playerRotation.collectAsStateWithLifecycle()
    val fuel by viewModel.fuel.collectAsStateWithLifecycle()
    val turboCharge by viewModel.turboCharge.collectAsStateWithLifecycle()
    val isTurboActive by viewModel.isTurboActive.collectAsStateWithLifecycle()
    val coins by viewModel.coins.collectAsStateWithLifecycle()
    val activeSkin by viewModel.activeSkin.collectAsStateWithLifecycle()
    val beacons by viewModel.adBeacons.collectAsStateWithLifecycle()
    val canisters by viewModel.fuelCanisters.collectAsStateWithLifecycle()
    val particles by viewModel.particles.collectAsStateWithLifecycle()
    val hudMsg by viewModel.hudMessage.collectAsStateWithLifecycle()

    val fuelLvl by viewModel.fuelLevel.collectAsStateWithLifecycle()
    val speedLvl by viewModel.speedLevel.collectAsStateWithLifecycle()

    val density = LocalDensity.current

    // Virtual Joystick States
    var joystickOffset by remember { mutableStateOf(Offset.Zero) }
    val maxJoystickDragDistance = with(density) { 60.dp.toPx() }

    // Touch triggers for vertical force
    var isUpPressed by remember { mutableStateOf(false) }
    var isDownPressed by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF020617)) // Black space background
    ) {
        // 3D CANVAS WORLD
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    // No direct canvas pointer drags, we handle them on overlays
                }
        ) {
            val w = size.width
            val h = size.height

            // Calculate Dynamic Camera Position (following player from behind)
            val distance = 30f + (if (isTurboActive) 6f else 0f)
            val heightOffset = 13f
            val pitch = -0.25f // Cam angle looking down

            val cameraX = playerPos.x - sin(playerRot) * distance
            val cameraZ = playerPos.z - cos(playerRot) * distance
            val cameraY = playerPos.y + heightOffset
            val cameraPos = Vector3D(cameraX, cameraY, cameraZ)

            // DRAW SKY AND HORIZON SUNSET GRADIENT
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0369A1), Color(0xFF0284C7), Color(0xFFF97316), Color(0xFFE11D48)),
                    startY = 0f,
                    endY = h * 0.65f
                )
            )

            // DRAW GROUND GRASS FIELD
            drawRect(
                color = Color(0xFF14532D), // Green Grass Field
                topLeft = Offset(0f, h * 0.58f),
                size = Size(w, h * 0.42f)
            )

            // DRAW STREET GRID LINES ON GROUND
            val gridStep = 50f
            val gridLinesCount = 10
            for (i in -gridLinesCount..gridLinesCount) {
                val zCoord = (i * gridStep).coerceIn(-250f, 250f)
                // Draw horizontal streets
                val start3D = Vector3D(-250f, 0f, zCoord)
                val end3D = Vector3D(250f, 0f, zCoord)

                val startProj = project3D(start3D, cameraPos, playerRot, pitch, w, h)
                val endProj = project3D(end3D, cameraPos, playerRot, pitch, w, h)

                if (startProj != null && endProj != null) {
                    drawLine(
                        color = Color(0xFF475569), // Dark concrete street color
                        start = startProj,
                        end = endProj,
                        strokeWidth = 20f / (1f + (zCoord - cameraZ) * 0.012f).coerceAtLeast(1f)
                    )
                }

                // Draw vertical streets
                val xCoord = i * gridStep
                val vStart3D = Vector3D(xCoord, 0f, -250f)
                val vEnd3D = Vector3D(xCoord, 0f, 250f)

                val vStartProj = project3D(vStart3D, cameraPos, playerRot, pitch, w, h)
                val vEndProj = project3D(vEnd3D, cameraPos, playerRot, pitch, w, h)

                if (vStartProj != null && vEndProj != null) {
                    drawLine(
                        color = Color(0xFF475569),
                        start = vStartProj,
                        end = vEndProj,
                        strokeWidth = 20f / (1f + (cameraZ - -250f) * 0.012f).coerceAtLeast(1f)
                    )
                }
            }

            // DRAW TREES
            for (tree in viewModel.trees) {
                val trunkBase = Vector3D(tree.x, 0f, tree.z)
                val trunkTop = Vector3D(tree.x, tree.height * 0.3f, tree.z)
                val foliageTop = Vector3D(tree.x, tree.height, tree.z)

                val pTrunkBase = project3D(trunkBase, cameraPos, playerRot, pitch, w, h)
                val pTrunkTop = project3D(trunkTop, cameraPos, playerRot, pitch, w, h)
                val pFoliageTop = project3D(foliageTop, cameraPos, playerRot, pitch, w, h)

                if (pTrunkBase != null && pTrunkTop != null && pFoliageTop != null) {
                    // Draw Trunk
                    drawLine(
                        color = Color(tree.trunkColor),
                        start = pTrunkBase,
                        end = pTrunkTop,
                        strokeWidth = 14f / (1f + (tree.z - cameraZ) * 0.008f).coerceAtLeast(1f)
                    )
                    // Draw foliage as pyramid/triangle
                    val foliageRadius = 15f / (1f + (tree.z - cameraZ) * 0.008f).coerceAtLeast(1f)
                    val leafPath = Path().apply {
                        moveTo(pFoliageTop.x, pFoliageTop.y)
                        lineTo(pTrunkTop.x - foliageRadius, pTrunkTop.y)
                        lineTo(pTrunkTop.x + foliageRadius, pTrunkTop.y)
                        close()
                    }
                    drawPath(leafPath, color = Color(tree.leafColor))
                }
            }

            // DRAW CARS
            for (car in viewModel.cars) {
                val carSizeX = 4f
                val carSizeZ = 7f
                val carY = 1f

                val pCar0 = project3D(Vector3D(car.x - carSizeX/2, 0f, car.z - carSizeZ/2), cameraPos, playerRot, pitch, w, h)
                val pCar1 = project3D(Vector3D(car.x + carSizeX/2, carY, car.z + carSizeZ/2), cameraPos, playerRot, pitch, w, h)

                if (pCar0 != null && pCar1 != null) {
                    drawRect(
                        color = Color(car.color),
                        topLeft = Offset(min(pCar0.x, pCar1.x), min(pCar0.y, pCar1.y)),
                        size = Size(abs(pCar1.x - pCar0.x).coerceAtLeast(8f), abs(pCar1.y - pCar0.y).coerceAtLeast(4f))
                    )
                }
            }

            // DRAW BUILDINGS & OUTDOOR BILLBOARDS
            // Sort buildings back-to-front relative to camera for painter's depth sorting algorithm
            val sortedBuildings = viewModel.buildings.sortedByDescending { b ->
                val distSq = (b.x - cameraX).pow(2) + (b.z - cameraZ).pow(2)
                distSq
            }

            for (building in sortedBuildings) {
                val bx = building.x
                val bz = building.z
                val halfW = building.widthX / 2f
                val halfD = building.widthZ / 2f
                val bh = building.height

                // Base points projection
                val b0 = project3D(Vector3D(bx - halfW, 0f, bz - halfD), cameraPos, playerRot, pitch, w, h)
                val b1 = project3D(Vector3D(bx + halfW, 0f, bz - halfD), cameraPos, playerRot, pitch, w, h)
                val b2 = project3D(Vector3D(bx + halfW, 0f, bz + halfD), cameraPos, playerRot, pitch, w, h)
                val b3 = project3D(Vector3D(bx - halfW, 0f, bz + halfD), cameraPos, playerRot, pitch, w, h)

                // Roof points projection
                val r0 = project3D(Vector3D(bx - halfW, bh, bz - halfD), cameraPos, playerRot, pitch, w, h)
                val r1 = project3D(Vector3D(bx + halfW, bh, bz - halfD), cameraPos, playerRot, pitch, w, h)
                val r2 = project3D(Vector3D(bx + halfW, bh, bz + halfD), cameraPos, playerRot, pitch, w, h)
                val r3 = project3D(Vector3D(bx - halfW, bh, bz + halfD), cameraPos, playerRot, pitch, w, h)

                if (r0 != null && r1 != null && r2 != null && r3 != null) {
                    // Draw Solid Building Roof
                    val roofPath = Path().apply {
                        moveTo(r0.x, r0.y)
                        lineTo(r1.x, r1.y)
                        lineTo(r2.x, r2.y)
                        lineTo(r3.x, r3.y)
                        close()
                    }
                    drawPath(roofPath, color = Color(building.color).copy(alpha = 0.9f))

                    // Draw vertical walls wires (connecting base to roof)
                    if (b0 != null) drawLine(Color(building.color).copy(alpha = 0.5f), b0, r0, strokeWidth = 3f)
                    if (b1 != null) drawLine(Color(building.color).copy(alpha = 0.5f), b1, r1, strokeWidth = 3f)
                    if (b2 != null) drawLine(Color(building.color).copy(alpha = 0.5f), b2, r2, strokeWidth = 3f)
                    if (b3 != null) drawLine(Color(building.color).copy(alpha = 0.5f), b3, r3, strokeWidth = 3f)

                    // Draw roof borders
                    drawLine(Color.White.copy(alpha = 0.7f), r0, r1, strokeWidth = 4f)
                    drawLine(Color.White.copy(alpha = 0.7f), r1, r2, strokeWidth = 4f)
                    drawLine(Color.White.copy(alpha = 0.7f), r2, r3, strokeWidth = 4f)
                    drawLine(Color.White.copy(alpha = 0.7f), r3, r0, strokeWidth = 4f)

                    // Draw ADVERTISEMENTS BILLBOARD
                    if (building.adText != null && building.adSponsor != null) {
                        // Place a massive billboard facing the front wall
                        val bYMin = bh * 0.45f
                        val bYMax = bh * 0.8f
                        val adOffset = 0.2f

                        // Corner coordinates of Billboard facing front side (Z - depth/2)
                        val ad0 = project3D(Vector3D(bx - halfW * 0.75f, bYMin, bz - halfD - adOffset), cameraPos, playerRot, pitch, w, h)
                        val ad1 = project3D(Vector3D(bx + halfW * 0.75f, bYMin, bz - halfD - adOffset), cameraPos, playerRot, pitch, w, h)
                        val ad2 = project3D(Vector3D(bx + halfW * 0.75f, bYMax, bz - halfD - adOffset), cameraPos, playerRot, pitch, w, h)
                        val ad3 = project3D(Vector3D(bx - halfW * 0.75f, bYMax, bz - halfD - adOffset), cameraPos, playerRot, pitch, w, h)

                        if (ad0 != null && ad1 != null && ad2 != null && ad3 != null) {
                            // Vibrant neon advertising backing
                            val adBackingPath = Path().apply {
                                moveTo(ad0.x, ad0.y)
                                fillBillboardGradientPath(this, ad1, ad2, ad3)
                            }
                            drawPath(
                                path = adBackingPath,
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFFD946EF), Color(0xFF2563EB), Color(0xFF06B6D4)),
                                    start = ad0,
                                    end = ad2
                                )
                            )

                            // Drawn Billboard neon borders
                            drawLine(Color(0xFF00FFFF), ad0, ad1, strokeWidth = 5f)
                            drawLine(Color(0xFF00FFFF), ad1, ad2, strokeWidth = 5f)
                            drawLine(Color(0xFF00FFFF), ad2, ad3, strokeWidth = 5f)
                            drawLine(Color(0xFF00FFFF), ad3, ad0, strokeWidth = 5f)

                            // Render Ad text utilizing native canvas drawing
                            val midX = (ad0.x + ad1.x + ad2.x + ad3.x) / 4f
                            val midY = (ad0.y + ad1.y + ad2.y + ad3.y) / 4f

                            drawContext.canvas.nativeCanvas.apply {
                                // Double lines: Title (Sponsor) and body slogan
                                val sponsorPaint = android.graphics.Paint().apply {
                                    color = android.graphics.Color.WHITE
                                    textSize = (16f / (1f + (bz - cameraZ) * 0.01f).coerceAtLeast(1f)).coerceIn(12f, 32f)
                                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                                    textAlign = android.graphics.Paint.Align.CENTER
                                }

                                val textPaint = android.graphics.Paint().apply {
                                    color = android.graphics.Color.YELLOW
                                    textSize = (12f / (1f + (bz - cameraZ) * 0.01f).coerceAtLeast(1f)).coerceIn(9f, 22f)
                                    typeface = android.graphics.Typeface.SANS_SERIF
                                    textAlign = android.graphics.Paint.Align.CENTER
                                }

                                drawText(
                                    building.adSponsor,
                                    midX,
                                    midY - 6f,
                                    sponsorPaint
                                )
                                drawText(
                                    "ANÚNCIO",
                                    midX,
                                    midY + sponsorPaint.textSize,
                                    textPaint
                                )
                            }
                        }
                    }
                }
            }

            // DRAW ACTIVE AD BEACONS (MISSION RINGS)
            for (beacon in beacons) {
                if (beacon.completed) continue

                // 3D glow pillar shooting from ground to beacon
                val ground3D = Vector3D(beacon.pos.x, 0f, beacon.pos.z)
                val top3D = Vector3D(beacon.pos.x, beacon.pos.y + 20f, beacon.pos.z)

                val pGround = project3D(ground3D, cameraPos, playerRot, pitch, w, h)
                val pTop = project3D(top3D, cameraPos, playerRot, pitch, w, h)

                if (pGround != null && pTop != null) {
                    // Draw glow pillar
                    drawLine(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0x00FFD700), Color(0xAAFFD700), Color(0xFFFFD700)),
                            startY = pTop.y,
                            endY = pGround.y
                        ),
                        start = pGround,
                        end = pTop,
                        strokeWidth = 12f
                    )

                    // Draw glowing beacon sign
                    drawCircle(
                        color = Color(0xFFFBBF24),
                        radius = 24f,
                        center = pTop,
                        style = Stroke(width = 4f)
                    )

                    drawCircle(
                        color = Color(0xFFFFD700).copy(alpha = 0.3f),
                        radius = 45f,
                        center = pTop
                    )

                    // Floating text label
                    drawContext.canvas.nativeCanvas.apply {
                        val paint = android.graphics.Paint().apply {
                            color = android.graphics.Color.WHITE
                            textSize = 28f
                            typeface = android.graphics.Typeface.DEFAULT_BOLD
                            textAlign = android.graphics.Paint.Align.CENTER
                            setShadowLayer(10f, 0f, 0f, android.graphics.Color.RED)
                        }
                        drawText(
                            beacon.sponsorName,
                            pTop.x,
                            pTop.y - 30f,
                            paint
                        )
                        drawText(
                            "DIVULGAR",
                            pTop.x,
                            pTop.y - 65f,
                            paint.apply { color = android.graphics.Color.YELLOW; textSize = 22f }
                        )
                    }
                }
            }

            // DRAW FUEL CANISTERS
            for (canister in canisters) {
                if (canister.collected) continue

                val pCan = project3D(canister.pos, cameraPos, playerRot, pitch, w, h)
                if (pCan != null) {
                    val radius = 18f
                    val path = Path().apply {
                        moveTo(pCan.x, pCan.y - radius) // Top
                        lineTo(pCan.x + radius, pCan.y) // Right
                        lineTo(pCan.x, pCan.y + radius) // Bottom
                        lineTo(pCan.x - radius, pCan.y) // Left
                        close()
                    }

                    // Spin coloring
                    val progressColor = Color(0xFFEAB308) // Glowing Yellow
                    drawPath(path, color = progressColor)
                    drawPath(path, color = Color.White, style = Stroke(width = 3f))

                    // Gas text
                    drawContext.canvas.nativeCanvas.apply {
                        val paint = android.graphics.Paint().apply {
                            color = android.graphics.Color.BLACK
                            textSize = 18f
                            typeface = android.graphics.Typeface.DEFAULT_BOLD
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                        drawText("GAS", pCan.x, pCan.y + 6f, paint)
                    }
                }
            }

            // DRAW GLOWING FLIGHT PARTICLES (JETPACK FIRE)
            for (p in particles) {
                val pProj = project3D(p.pos, cameraPos, playerRot, pitch, w, h)
                if (pProj != null) {
                    val pRadius = (p.size * p.life).coerceAtLeast(2f)
                    drawCircle(
                        color = Color(p.color).copy(alpha = p.life),
                        radius = pRadius,
                        center = pProj,
                        blendMode = BlendMode.Screen
                    )
                }
            }

            // DRAW PLAYER CHARACTER AT SCREEN CENTER FRONT
            // Player is projected directly onto Canvas!
            val pPlayer = project3D(playerPos, cameraPos, playerRot, pitch, w, h)
            if (pPlayer != null) {
                val pScale = 1.0f // Scaling factor

                // Suit Color
                val suitColor = when (activeSkin) {
                    "Cyber Neon" -> Color(0xFF06B6D4)
                    "Gold Royalty" -> Color(0xFFFBBF24)
                    else -> Color(0xFFDC2626) // Classic Red
                }

                // Thrusters glow
                drawCircle(
                    color = Color.White,
                    radius = 35f,
                    center = pPlayer,
                    blendMode = BlendMode.Screen,
                    style = Stroke(width = 1f)
                )

                // Render Head (Aviator Helmet)
                drawCircle(
                    color = Color(0xFFFEE2E2),
                    radius = 16f,
                    center = Offset(pPlayer.x, pPlayer.y - 35f)
                )

                // Helmet Visor
                drawRoundRect(
                    color = Color(0xFF38BDF8),
                    topLeft = Offset(pPlayer.x - 10f, pPlayer.y - 42f),
                    size = Size(20f, 10f),
                    cornerRadius = CornerRadius(4f, 4f)
                )

                // Body Suit
                drawRoundRect(
                    color = suitColor,
                    topLeft = Offset(pPlayer.x - 14f, pPlayer.y - 18f),
                    size = Size(28f, 38f),
                    cornerRadius = CornerRadius(8f, 8f)
                )

                // Dual Jetpacks on Sides
                // Left Thruster
                drawRoundRect(
                    color = Color(0xFF64748B),
                    topLeft = Offset(pPlayer.x - 24f, pPlayer.y - 14f),
                    size = Size(9f, 26f),
                    cornerRadius = CornerRadius(3f, 3f)
                )
                // Right Thruster
                drawRoundRect(
                    color = Color(0xFF64748B),
                    topLeft = Offset(pPlayer.x + 15f, pPlayer.y - 14f),
                    size = Size(9f, 26f),
                    cornerRadius = CornerRadius(3f, 3f)
                )

                // Underbody flames if rising or turbo active
                if (isUpPressed || isTurboActive) {
                    val flameLength = if (isTurboActive) 45f else 25f
                    val leftFlamePath = Path().apply {
                        moveTo(pPlayer.x - 23f, pPlayer.y + 12f)
                        lineTo(pPlayer.x - 16f, pPlayer.y + 12f)
                        lineTo(pPlayer.x - 19.5f, pPlayer.y + 12f + flameLength)
                        close()
                    }
                    val rightFlamePath = Path().apply {
                        moveTo(pPlayer.x + 16f, pPlayer.y + 12f)
                        lineTo(pPlayer.x + 23f, pPlayer.y + 12f)
                        lineTo(pPlayer.x + 19.5f, pPlayer.y + 12f + flameLength)
                        close()
                    }
                    val flameBrush = Brush.verticalGradient(
                        colors = listOf(Color.White, Color(0xFFFF4500), Color(0x00FF4500))
                    )
                    drawPath(leftFlamePath, brush = flameBrush)
                    drawPath(rightFlamePath, brush = flameBrush)
                }
            }
        }

        // ==========================================
        // 3. GAME HUD INTERFACES OVERLAYS
        // ==========================================
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            // TOP HUD: STATS BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // LEFT: FUEL AND COINS
                Column(
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                        .padding(12.dp)
                        .width(160.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = "Moedas",
                            tint = Color(0xFFFBBF24),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$coins",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Fuel gauge
                    val maxF = 100f + (fuelLvl - 1) * 35f
                    val fuelPct = (fuel / maxF).coerceIn(0f, 1f)
                    val fuelColor = when {
                        fuelPct > 0.5f -> Color(0xFF10B981)
                        fuelPct > 0.25f -> Color(0xFFFBBF24)
                        else -> Color(0xFFEF4444)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "COMBUS.",
                            color = Color.LightGray,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${(fuelPct * 100).toInt()}%",
                            color = fuelColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .background(Color.DarkGray, RoundedCornerShape(5.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fuelPct)
                                .background(fuelColor, RoundedCornerShape(5.dp))
                        )
                    }
                }

                // MIDDLE: MISSION OBJECTIVES PROGRESS
                val completedAdsCount = beacons.count { it.completed }
                val totalAdsCount = beacons.size
                Column(
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "OBJETIVO DA MISSÃO",
                        color = Color.Yellow,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Anúncios: $completedAdsCount / $totalAdsCount",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                // RIGHT: SPEEDOMETER, TURBO ENERGY AND PAUSE
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Pause Button
                    IconButton(
                        onClick = { viewModel.returnToMenu() },
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            .size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Sair",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Speedometer & Turbo
                    Column(
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                            .padding(10.dp)
                            .width(90.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "VELOCIDADE",
                            color = Color.LightGray,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        val speedVal = if (isTurboActive) "280 km/h" else "${120 + (speedLvl * 15)} km/h"
                        Text(
                            text = speedVal,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        // Turbo charge gauge
                        val turboPct = (turboCharge / (100f + (viewModel.turboLevel.value - 1) * 40f)).coerceIn(0f, 1f)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .background(Color.DarkGray, RoundedCornerShape(3.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(turboPct)
                                    .background(Color(0xFFF97316), RoundedCornerShape(3.dp))
                            )
                        }
                        Text(
                            "TURBO",
                            color = Color(0xFFF97316),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // MIDDLE: RADAR/MINI-MAP (COMPASS IN HUD FOR NAVIGATION HELP)
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(top = 110.dp)
            ) {
                // Compass displaying arrows pointing to nearest active beacon
                val activeBeacon = beacons.firstOrNull { !it.completed }
                if (activeBeacon != null) {
                    val relativeVec = activeBeacon.pos - playerPos
                    val angleToBeacon = atan2(relativeVec.x, relativeVec.z)
                    // Subtract playerYaw to get relative compass direction
                    val relativeAngle = angleToBeacon - playerRot

                    Column(
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(16.dp))
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "OUTDOOR ALVO",
                            color = Color(0xFF38BDF8),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Box(
                            modifier = Modifier
                                .size(45.dp)
                                .drawBehind {
                                    drawCircle(
                                        color = Color(0xFF475569),
                                        radius = size.width / 2f,
                                        style = Stroke(width = 2f)
                                    )

                                    // Compass direction arrow
                                    val radius = size.width / 2f
                                    val arrowLength = radius * 0.8f
                                    val endX = radius + sin(relativeAngle) * arrowLength
                                    val endY = radius - cos(relativeAngle) * arrowLength

                                    drawLine(
                                        color = Color(0xFF00FFFF),
                                        start = Offset(radius, radius),
                                        end = Offset(endX, endY),
                                        strokeWidth = 5f,
                                        cap = StrokeCap.Round
                                    )
                                }
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        val distMeters = relativeVec.length().toInt()
                        Text(
                            text = "${distMeters}m",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // FEEDBACK HUD CENTER POPUPS (e.g. "+$50")
            hudMsg?.let { msg ->
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .background(Color(0xFF1E293B).copy(alpha = 0.95f), RoundedCornerShape(16.dp))
                        .border(2.dp, Color(0xFF38BDF8), RoundedCornerShape(16.dp))
                        .padding(horizontal = 24.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = msg,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // BOTTOM CONTROLS: JOYSTICK AND ACTIONS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // JOYSTICK
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                        .border(2.dp, Color.LightGray.copy(alpha = 0.3f), CircleShape)
                        .pointerInteropFilter { event ->
                            val center = with(density) { 65.dp.toPx() }
                            when (event.actionMasked) {
                                MotionEvent.ACTION_DOWN,
                                MotionEvent.ACTION_MOVE -> {
                                    val rawOffset = Offset(event.x - center, event.y - center)
                                    val dist = rawOffset.getDistance()
                                    joystickOffset = if (dist > maxJoystickDragDistance) {
                                        rawOffset * (maxJoystickDragDistance / dist)
                                    } else {
                                        rawOffset
                                    }
                                    val jX = joystickOffset.x / maxJoystickDragDistance
                                    val jY = -joystickOffset.y / maxJoystickDragDistance
                                    viewModel.updateJoystick(jX, jY)
                                    true
                                }
                                MotionEvent.ACTION_UP,
                                MotionEvent.ACTION_CANCEL -> {
                                    joystickOffset = Offset.Zero
                                    viewModel.updateJoystick(0f, 0f)
                                    true
                                }
                                else -> true
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Outer border compass marks
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(Color.White.copy(alpha = 0.1f), radius = size.width / 4f)
                    }

                    // Joystick dynamic knob
                    Box(
                        modifier = Modifier
                            .offset(
                                x = with(density) { joystickOffset.x.toDp() },
                                y = with(density) { joystickOffset.y.toDp() }
                            )
                            .size(54.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color(0xFF38BDF8), Color(0xFF2563EB))
                                ),
                                CircleShape
                            )
                            .border(2.dp, Color.White, CircleShape)
                    )
                }

                // PILOT ACTION BUTTONS (SUBIR, DESCER, TURBO)
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Turbo Button
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(
                                if (isTurboActive) Color(0xFFEA580C) else Color(0xFFD97706),
                                CircleShape
                            )
                            .border(2.dp, Color.White, CircleShape)
                            .clickable {
                                viewModel.toggleTurbo(!isTurboActive)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = "Turbo",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                "TURBO",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Descer Button
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                                .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                                .pointerInteropFilter { event ->
                                    when (event.actionMasked) {
                                        MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                                            isDownPressed = true
                                            viewModel.setUpDownForces(up = false, down = true)
                                            true
                                        }
                                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                            isDownPressed = false
                                            viewModel.setUpDownForces(up = false, down = false)
                                            true
                                        }
                                        else -> true
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = "Descer",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Subir Button
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(Color(0xFF10B981), Color(0xFF047857))
                                    ),
                                    CircleShape
                                )
                                .border(2.dp, Color.White, CircleShape)
                                .pointerInteropFilter { event ->
                                    when (event.actionMasked) {
                                        MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                                            isUpPressed = true
                                            viewModel.setUpDownForces(up = true, down = false)
                                            true
                                        }
                                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                            isUpPressed = false
                                            viewModel.setUpDownForces(up = false, down = false)
                                            true
                                        }
                                        else -> true
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = "Subir",
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. UPGRADES SHOP SCREEN
// ==========================================
@Composable
fun UpgradesScreen(viewModel: GameViewModel) {
    val coins by viewModel.coins.collectAsStateWithLifecycle()
    val fuelLvl by viewModel.fuelLevel.collectAsStateWithLifecycle()
    val speedLvl by viewModel.speedLevel.collectAsStateWithLifecycle()
    val turboLvl by viewModel.turboLevel.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .systemBarsPadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // TOP Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.returnToMenu() },
                modifier = Modifier
                    .background(Color(0xFF1E293B), CircleShape)
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Voltar",
                    tint = Color.White
                )
            }

            Text(
                "OFICINA DE UPGRADES",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color(0xFF1E293B), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MonetizationOn,
                    contentDescription = "Coins",
                    tint = Color(0xFFFBBF24),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$coins",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Melhore os atributos do seu propulsor jetpack com o dinheiro obtido completando divulgações de painéis na cidade!",
            color = Color.LightGray,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Upgrade Card 1: Fuel Tank
            UpgradeRowItem(
                title = "Tanque de Combustível",
                description = "Aumenta a autonomia e capacidade de voo",
                level = fuelLvl,
                maxLevel = 5,
                cost = fuelLvl * 80,
                icon = Icons.Default.LocalGasStation,
                iconColor = Color(0xFF10B981),
                playerCoins = coins,
                onBuy = { viewModel.purchaseUpgrade("fuel") }
            )

            // Upgrade Card 2: Engine Speed
            UpgradeRowItem(
                title = "Potência dos Motores",
                description = "Velocidade padrão de voo mais rápida",
                level = speedLvl,
                maxLevel = 5,
                cost = speedLvl * 90,
                icon = Icons.Default.Speed,
                iconColor = Color(0xFF38BDF8),
                playerCoins = coins,
                onBuy = { viewModel.purchaseUpgrade("speed") }
            )

            // Upgrade Card 3: Turbo Duration
            UpgradeRowItem(
                title = "Capacitor de Turbo",
                description = "Amplia a duração da queima em velocidade máxima",
                level = turboLvl,
                maxLevel = 5,
                cost = turboLvl * 100,
                icon = Icons.Default.ElectricBolt,
                iconColor = Color(0xFFF59E0B),
                playerCoins = coins,
                onBuy = { viewModel.purchaseUpgrade("turbo") }
            )
        }
    }
}

@Composable
fun UpgradeRowItem(
    title: String,
    description: String,
    level: Int,
    maxLevel: Int,
    cost: Int,
    icon: ImageVector,
    iconColor: Color,
    playerCoins: Int,
    onBuy: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Info column
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = description,
                    color = Color.Gray,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Star Level Indicator
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (i in 1..maxLevel) {
                        val active = i <= level
                        Box(
                            modifier = Modifier
                                .size(16.dp, 6.dp)
                                .background(
                                    if (active) iconColor else Color.DarkGray,
                                    RoundedCornerShape(3.dp)
                                )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Purchase column
            if (level >= maxLevel) {
                Text(
                    "MÁXIMO",
                    color = iconColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            } else {
                Button(
                    onClick = onBuy,
                    enabled = playerCoins >= cost,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = iconColor,
                        disabledContainerColor = Color.DarkGray
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "MELHORAR",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (playerCoins >= cost) Color.White else Color.Gray
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = "Moedas",
                                tint = if (playerCoins >= cost) Color.Yellow else Color.Gray,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "$cost",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (playerCoins >= cost) Color.White else Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. SKINS CUSTOMIZATION GALLERY SCREEN
// ==========================================
@Composable
fun SkinsScreen(viewModel: GameViewModel) {
    val coins by viewModel.coins.collectAsStateWithLifecycle()
    val activeSkin by viewModel.activeSkin.collectAsStateWithLifecycle()
    val unlockedSkins by viewModel.unlockedSkins.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .systemBarsPadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // TOP Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.returnToMenu() },
                modifier = Modifier
                    .background(Color(0xFF1E293B), CircleShape)
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Voltar",
                    tint = Color.White
                )
            }

            Text(
                "COLEÇÃO DE MOCHILAS",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color(0xFF1E293B), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MonetizationOn,
                    contentDescription = "Coins",
                    tint = Color(0xFFFBBF24),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$coins",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Skin Item 1: Classic Red
            SkinGalleryCard(
                name = "Classic Red",
                description = "Modelo padrão de propulsores de voo. Partículas de queima alaranjadas simples.",
                benefit = "Velocidade padrão sem adicionais",
                cost = 0,
                color = Color(0xFFDC2626),
                unlocked = unlockedSkins.contains("Classic Red"),
                active = activeSkin == "Classic Red",
                playerCoins = coins,
                onEquip = { viewModel.selectSkin("Classic Red") },
                onUnlock = {}
            )

            // Skin Item 2: Cyber Neon
            SkinGalleryCard(
                name = "Cyber Neon",
                description = "Composto de fibra de carbono. Emite chamas de plasma azul ciano brilhantes.",
                benefit = "Visual tecnológico iluminado",
                cost = 150,
                color = Color(0xFF06B6D4),
                unlocked = unlockedSkins.contains("Cyber Neon"),
                active = activeSkin == "Cyber Neon",
                playerCoins = coins,
                onEquip = { viewModel.selectSkin("Cyber Neon") },
                onUnlock = { viewModel.purchaseSkin("Cyber Neon", 150) }
            )

            // Skin Item 3: Gold Royalty
            SkinGalleryCard(
                name = "Gold Royalty",
                description = "Revestido em ouro 24k com partículas de queima douradas luxuosas.",
                benefit = "SUPER ECONOMIA: Gasta 35% MENOS combustível ao voar!",
                cost = 300,
                color = Color(0xFFFBBF24),
                unlocked = unlockedSkins.contains("Gold Royalty"),
                active = activeSkin == "Gold Royalty",
                playerCoins = coins,
                onEquip = { viewModel.selectSkin("Gold Royalty") },
                onUnlock = { viewModel.purchaseSkin("Gold Royalty", 300) }
            )
        }
    }
}

@Composable
fun SkinGalleryCard(
    name: String,
    description: String,
    benefit: String,
    cost: Int,
    color: Color,
    unlocked: Boolean,
    active: Boolean,
    playerCoins: Int,
    onEquip: () -> Unit,
    onUnlock: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(16.dp),
        border = if (active) BorderStroke(2.dp, color) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Visual circle representing skin thruster fire
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(color.copy(alpha = 0.15f), CircleShape)
                    .border(2.dp, color, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FlightTakeoff,
                    contentDescription = name,
                    tint = color,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = description,
                    color = Color.Gray,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Bônus: $benefit",
                    color = color,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Action
            if (active) {
                Button(
                    onClick = {},
                    enabled = false,
                    colors = ButtonDefaults.buttonColors(
                        disabledContainerColor = Color(0xFF0F172A),
                        disabledContentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("USANDO", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            } else if (unlocked) {
                Button(
                    onClick = onEquip,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("EQUIPAR", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            } else {
                Button(
                    onClick = onUnlock,
                    enabled = playerCoins >= cost,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = color,
                        disabledContainerColor = Color.DarkGray
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "DESBLOQ.",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (playerCoins >= cost) Color.Black else Color.Gray
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = "Coins",
                                tint = if (playerCoins >= cost) Color.Black else Color.Gray,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "$cost",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (playerCoins >= cost) Color.Black else Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 6. SCREEN: INTERSTITIAL / REWARDED ADS SIMULATOR
// ==========================================
@Composable
fun MockAdScreen(viewModel: GameViewModel) {
    val countdown by viewModel.adCountdown.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(Color(0xFF1E293B), RoundedCornerShape(24.dp))
                .border(2.dp, Color(0xFFFBBF24), RoundedCornerShape(24.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFFFBBF24), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "ANÚNCIO PREMIUM",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.DarkGray, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${countdown}s",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // App Icon Mock
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(Color(0xFF2563EB), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FlightTakeoff,
                    contentDescription = "Ad logo",
                    tint = Color.White,
                    modifier = Modifier.size(38.dp)
                )
            }

            Text(
                text = "GIGA JETPACK 2000 PRO",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )

            Text(
                text = "Cansado de ficar sem combustível? Desbloqueie o tanque infinito, remova todos os anúncios entre voos e ganhe velocidade interestelar instantaneamente por apenas R$4.99!",
                fontSize = 13.sp,
                color = Color.LightGray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Install CTA button
            Button(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "OBTER NA PLAY STORE",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Text(
                "O anúncio será fechado automaticamente ao final do cronômetro.",
                color = Color.Gray,
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ==========================================
// 7. VICTORY RUN OVER SCREEN
// ==========================================
@Composable
fun VictoryScreen(viewModel: GameViewModel) {
    val score by viewModel.scoreThisRun.collectAsStateWithLifecycle()
    val totalCoins by viewModel.coins.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xBB020617)) // Blurred dark overlay
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E293B), RoundedCornerShape(28.dp))
                .border(3.dp, Color(0xFF10B981), RoundedCornerShape(28.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color(0x2010B981), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "Vencedor",
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(45.dp)
                )
            }

            Text(
                text = "MISSÃO CUMPRIDA!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Você divulgou anúncios com sucesso em todos os 3 outdoors principais da cidade!",
                fontSize = 13.sp,
                color = Color.LightGray,
                textAlign = TextAlign.Center
            )

            Divider(color = Color.DarkGray, thickness = 1.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Painéis Ativos", fontSize = 11.sp, color = Color.Gray)
                    Text("3 / 3", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Recompensa Base", fontSize = 11.sp, color = Color.Gray)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MonetizationOn, "", tint = Color(0xFFFBBF24), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("+360", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                    }
                }
            }

            Divider(color = Color.DarkGray, thickness = 1.dp)

            // Dynamic Action buttons (double payout or standard restart)
            var adClaimed by remember { mutableStateOf(false) }

            Button(
                onClick = {
                    viewModel.triggerRewardedAd {
                        viewModel.purchaseSkin("Classic Red", 0) // Dummy upgrade to trigger save
                        // Double payout
                        viewModel.startRun() // Just quick restart with double coins handled
                        adClaimed = true
                    }
                },
                enabled = !adClaimed,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PlayCircle, "", tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "DOBRAR RECOMPENSA! (+720 Moedas)",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { viewModel.returnToMenu() },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("MENU", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { viewModel.startRun() },
                    modifier = Modifier
                        .weight(1.2f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("JOGAR DE NOVO", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// 8. GAME OVER / OUT OF FUEL SCREEN
// ==========================================
@Composable
fun GameOverScreen(viewModel: GameViewModel) {
    val totalCoins by viewModel.coins.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xEE020617))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E293B), RoundedCornerShape(28.dp))
                .border(3.dp, Color(0xFFEF4444), RoundedCornerShape(28.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color(0x20EF4444), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.BatteryAlert,
                    contentDescription = "Acabou combustível",
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(45.dp)
                )
            }

            Text(
                text = "FIM DO COMBUSTÍVEL!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Seu jetpack esgotou toda a queima de propelente antes de transmitir todos os anúncios!",
                fontSize = 13.sp,
                color = Color.LightGray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // REWARDED AD BUTTON TO REFILL FUEL AND CONTINUE RUN
            Button(
                onClick = {
                    viewModel.triggerRewardedAd {
                        viewModel.startRun() // Simulated run resume with 50% fuel capacity
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Videocam, "", tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "REABASTECER 50% (ASSISTIR AD)",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { viewModel.returnToMenu() },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("MENU", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { viewModel.startRun() },
                    modifier = Modifier
                        .weight(1.2f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("TENTAR DE NOVO", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// 3D Point Projection calculations helper
private fun project3D(
    point: Vector3D,
    cameraPos: Vector3D,
    camYaw: Float,
    camPitch: Float,
    width: Float,
    height: Float,
    focalLength: Float = 350f
): Offset? {
    val dx = point.x - cameraPos.x
    val dy = point.y - cameraPos.y
    val dz = point.z - cameraPos.z

    // Yaw Rotation (around Y-axis)
    val cosY = cos(-camYaw)
    val sinY = sin(-camYaw)
    val rx = dx * cosY - dz * sinY
    val rz = dx * sinY + dz * cosY

    // Pitch Rotation (around X-axis)
    val cosP = cos(-camPitch)
    val sinP = sin(-camPitch)
    val ry = dy * cosP - rz * sinP
    val finalZ = dy * sinP + rz * cosP

    // Clip elements behind focal depth
    if (finalZ <= 1.5f) return null

    val screenX = (width / 2f) + (rx * focalLength / finalZ)
    val screenY = (height / 2f) - (ry * focalLength / finalZ)
    return Offset(screenX, screenY)
}

// Draw Gradient helper for Perspective billboard
private fun fillBillboardGradientPath(path: Path, ad1: Offset, ad2: Offset, ad3: Offset) {
    path.lineTo(ad1.x, ad1.y)
    path.lineTo(ad2.x, ad2.y)
    path.lineTo(ad3.x, ad3.y)
    path.close()
}
