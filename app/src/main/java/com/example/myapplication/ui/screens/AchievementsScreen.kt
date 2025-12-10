package com.example.myapplication.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.model.Badge
import com.example.myapplication.viewmodel.EventViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// Immutable Particle Data
data class ParticleData(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    val size: Float
)

// Explosion Event Holder
data class Explosion(
    val startTime: Long,
    val particles: List<ParticleData>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    viewModel: EventViewModel,
    onNavigateBack: () -> Unit
) {
    val badges by viewModel.getBadges().collectAsState(initial = emptyList())
    
    // Valid Explosions List (Stable List of Immutable Data)
    // We use a mutable state list just to add/remove explosion events.
    val explosions = remember { mutableStateListOf<Explosion>() }

    fun triggerConfetti(center: Offset) {
        val particles = List(30) {
            val angle = Random.nextFloat() * 6.28f
            val speed = Random.nextFloat() * 10f + 5f
            ParticleData(
                x = center.x,
                y = center.y,
                vx = cos(angle) * speed, // Initial velocity
                vy = sin(angle) * speed - 15f, // Initial velocity + upward burst
                color = Color(
                    red = Random.nextFloat(),
                    green = Random.nextFloat(),
                    blue = Random.nextFloat(),
                    alpha = 1f
                ),
                size = Random.nextFloat() * 15f + 5f // Slightly larger particles
            )
        }
        explosions.add(Explosion(System.nanoTime(), particles))
        
        // Cleanup old explosions after 3 seconds to keep list small
        if (explosions.size > 5) {
            explosions.removeAt(0)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1C1E), 
                        Color(0xFF0D0E10)
                    )
                )
            )
    ) {
        Scaffold(
            topBar = {
                LargeTopAppBar(
                    title = { Text("荣誉殿堂", color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack, 
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent
                    )
                )
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                // Progress Card
                val unlockedCount = badges.count { it.isUnlocked }
                val totalCount = badges.size
                val progress = if (totalCount > 0) unlockedCount.toFloat() / totalCount else 0f

                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "我的勋章",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "$unlockedCount / $totalCount",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.size(60.dp),
                                color = Color(0xFFFFD700), // Gold
                                trackColor = Color.White.copy(alpha = 0.1f),
                                strokeWidth = 6.dp,
                                strokeCap = StrokeCap.Round
                            )
                            Text(
                                "${(progress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                    }
                }

                // Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(badges) { badge ->
                        PremiumBadgeCard(
                            badge = badge,
                            onClick = { offset ->
                                if (badge.isUnlocked) {
                                    triggerConfetti(offset)
                                }
                            }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(32.dp)) }
                }
            }
        }
        
        // Optimized Confetti Overlay
        if (explosions.isNotEmpty()) {
            ConfettiSystem(explosions = explosions)
        }
    }
}

@Composable
fun ConfettiSystem(explosions: List<Explosion>) {
    // Single state to drive animation frame
    var frameTime by remember { mutableLongStateOf(0L) }
    
    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { 
                frameTime = it 
            }
        }
    }
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        // IMPORTANT: Read the state to subscribe to updates!
        val tick = frameTime 
        val currentFrameTime = System.nanoTime()
        
        explosions.forEach { explosion ->
            // elapsed seconds
            val t = (currentFrameTime - explosion.startTime) / 1_000_000_000f
            
            // Render only if within 2 seconds
            if (t in 0f..2f) {
                explosion.particles.forEach { p ->
                    // Physics: x = x0 + vx*t, y = y0 + vy*t + 0.5*g*t^2
                    // Scale factor: assume pixels/sec. 
                    // Let's add gravity constant approx 800 px/s^2
                    
                    val x = p.x + p.vx * t * 30f // multiplier to adjust speed feel
                    val y = p.y + p.vy * t * 30f + 0.5f * 1000f * t * t 
                    
                    val alpha = (1f - t / 2f).coerceIn(0f, 1f)
                    val radius = p.size * (1f - t/3f) // shrink slightly
                    
                    drawCircle(
                        color = p.color.copy(alpha = alpha),
                        center = Offset(x, y),
                        radius = radius
                    )
                }
            }
        }
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp)),
        content = content
    )
}

@Composable
fun PremiumBadgeCard(
    badge: Badge,
    onClick: (Offset) -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f, label = "scale")
    
    // Tilt effect state
    val rotationX = remember { Animatable(0f) }
    val rotationY = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var cardPosition by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .scale(scale)
            .onGloballyPositioned { coordinates ->
                cardPosition = coordinates.positionInRoot()
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        isPressed = true
                        val centerX = size.width / 2
                        val centerY = size.height / 2
                        val tiltStrength = 10f
                        val rx = ((offset.y - centerY) / centerY) * -tiltStrength
                        val ry = ((offset.x - centerX) / centerX) * tiltStrength
                        
                        scope.launch {
                            rotationX.animateTo(rx, spring(stiffness = Spring.StiffnessLow))
                        }
                        scope.launch {
                            rotationY.animateTo(ry, spring(stiffness = Spring.StiffnessLow))
                        }

                        tryAwaitRelease()
                        isPressed = false
                        onClick(cardPosition + offset)
                        
                        scope.launch { rotationX.animateTo(0f) }
                        scope.launch { rotationY.animateTo(0f) }
                    }
                )
            }
            .graphicsLayer {
                this.rotationX = rotationX.value
                this.rotationY = rotationY.value
                this.cameraDistance = 12 * density 
            }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // The Badge Medal
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                if (badge.isUnlocked) {
                    // Glow
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(badge.color.copy(alpha = 0.5f), Color.Transparent)
                                )
                            )
                    )
                }
                
                // 3D Medal Drawing
                Canvas(modifier = Modifier.size(100.dp)) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val radius = size.width / 2

                    if (badge.isUnlocked) {
                        drawCircle(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    badge.color.copy(alpha = 0.8f),
                                    Color.White.copy(alpha = 0.9f),
                                    badge.color.copy(alpha = 0.8f),
                                    Color.Black.copy(alpha = 0.3f),
                                    badge.color.copy(alpha = 0.8f)
                                )
                            ),
                            radius = radius,
                            center = center,
                            style = Stroke(width = 8.dp.toPx())
                        )
                        drawCircle(
                            color = badge.color.copy(alpha = 0.2f),
                            radius = radius - 4.dp.toPx(),
                            center = center
                        )
                        drawArc(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.White.copy(alpha = 0.4f), Color.Transparent)
                            ),
                            startAngle = 180f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = Offset(center.x - radius + 4.dp.toPx(), center.y - radius + 4.dp.toPx()),
                            size = Size((radius - 4.dp.toPx()) * 2, (radius - 4.dp.toPx()) * 2)
                        )
                    } else {
                        drawCircle(
                            color = Color.Gray.copy(alpha = 0.3f),
                            radius = radius,
                            center = center,
                            style = Stroke(width = 4.dp.toPx())
                        )
                        drawCircle(
                            color = Color.Black.copy(alpha = 0.5f),
                            radius = radius - 2.dp.toPx(),
                            center = center
                        )
                    }
                }

                Icon(
                    imageVector = badge.icon,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = if (badge.isUnlocked) Color.White else Color.White.copy(alpha = 0.2f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                badge.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (badge.isUnlocked) Color.White else Color.White.copy(alpha = 0.5f)
            )
            Text(
                badge.description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                maxLines = 2,
                minLines = 2,
                modifier = Modifier.height(32.dp)
            )
        }
    }
}
