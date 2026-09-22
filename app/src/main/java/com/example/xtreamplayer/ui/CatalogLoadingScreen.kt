package com.example.xtreamplayer.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xtreamplayer.R
import com.example.xtreamplayer.viewmodel.AppViewModel

private val SyncBlue = Color(0xFF1677FF)
private val SyncCyan = Color(0xFF20D7FF)
private val SyncMuted = Color(0xFFB6C3D1)

@Composable
fun CatalogLoadingScreen(
    vm: AppViewModel
) {
    val progress = vm.catalogSyncProgress.coerceIn(0, 100)
    val currentSection = vm.catalogSyncSection

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF02060B))
    ) {
        Image(
            painter = painterResource(
                id = R.drawable.future_smart_loading_background
            ),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Leggero velo per mantenere il testo leggibile senza coprire lo sfondo.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.05f),
                            Color.Black.copy(alpha = 0.12f),
                            Color.Black.copy(alpha = 0.34f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .widthIn(max = 790.dp)
                .fillMaxWidth()
                .padding(horizontal = 36.dp)
                .offset(y = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "P R E P A R I A M O   I L   T U O",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp
            )

            Spacer(Modifier.height(5.dp))

            Text(
                text = "I N T R A T T E N I M E N T O",
                color = SyncCyan,
                fontSize = 25.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.3.sp
            )

            Spacer(Modifier.height(28.dp))

            CatalogSyncRow(
                title = "LIVE TV",
                imageRes = R.drawable.home_live,
                state = syncStateFor(
                    section = "live",
                    currentSection = currentSection,
                    progress = progress
                )
            )

            Spacer(Modifier.height(10.dp))

            CatalogSyncRow(
                title = "FILM",
                imageRes = R.drawable.home_film,
                state = syncStateFor(
                    section = "movies",
                    currentSection = currentSection,
                    progress = progress
                )
            )

            Spacer(Modifier.height(10.dp))

            CatalogSyncRow(
                title = "SERIE TV",
                imageRes = R.drawable.home_series,
                state = syncStateFor(
                    section = "series",
                    currentSection = currentSection,
                    progress = progress
                )
            )

            Spacer(Modifier.height(26.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = { progress / 100f },
                    modifier = Modifier
                        .weight(1f)
                        .height(9.dp),
                    color = SyncCyan,
                    trackColor = Color(0xFF07131E)
                )

                Spacer(Modifier.width(18.dp))

                Text(
                    text = "$progress%",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private enum class CatalogRowState {
    WAITING,
    DOWNLOADING,
    COMPLETED
}

private fun syncStateFor(
    section: String,
    currentSection: String?,
    progress: Int
): CatalogRowState {
    val completedThreshold = when (section) {
        "live" -> 33
        "movies" -> 66
        "series" -> 100
        else -> 101
    }

    return when {
        progress >= completedThreshold ->
            CatalogRowState.COMPLETED

        currentSection == section ->
            CatalogRowState.DOWNLOADING

        else ->
            CatalogRowState.WAITING
    }
}

@Composable
private fun CatalogSyncRow(
    title: String,
    imageRes: Int,
    state: CatalogRowState
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(82.dp),
        color = Color(0xC907121D),
        shape = RoundedCornerShape(15.dp),
        border = BorderStroke(
            width = if (state == CatalogRowState.DOWNLOADING) {
                1.5.dp
            } else {
                1.dp
            },
            color = if (state == CatalogRowState.DOWNLOADING) {
                SyncCyan
            } else {
                SyncBlue.copy(alpha = 0.62f)
            }
        )
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                modifier = Modifier
                    .width(168.dp)
                    .fillMaxHeight(),
                contentScale = ContentScale.Crop
            )

            Text(
                text = title,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 24.dp),
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(44.dp)
                    .background(Color.White.copy(alpha = 0.20f))
            )

            Row(
                modifier = Modifier
                    .width(205.dp)
                    .padding(start = 23.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CatalogStateIcon(state)

                Spacer(Modifier.width(11.dp))

                Text(
                    text = when (state) {
                        CatalogRowState.WAITING -> "In attesa..."
                        CatalogRowState.DOWNLOADING -> "Download..."
                        CatalogRowState.COMPLETED -> "Completato"
                    },
                    color = when (state) {
                        CatalogRowState.WAITING -> SyncMuted
                        CatalogRowState.DOWNLOADING -> SyncCyan
                        CatalogRowState.COMPLETED -> Color(0xFF54E7A0)
                    },
                    fontSize = 14.sp,
                    fontWeight = if (
                        state == CatalogRowState.WAITING
                    ) {
                        FontWeight.Normal
                    } else {
                        FontWeight.SemiBold
                    }
                )
            }
        }
    }
}

@Composable
private fun CatalogStateIcon(
    state: CatalogRowState
) {
    when (state) {
        CatalogRowState.WAITING -> {
            Text(
                text = "◷",
                color = SyncMuted,
                fontSize = 24.sp
            )
        }

        CatalogRowState.COMPLETED -> {
            Text(
                text = "✓",
                color = Color(0xFF54E7A0),
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold
            )
        }

        CatalogRowState.DOWNLOADING -> {
            val transition =
                rememberInfiniteTransition(
                    label = "catalogSpinner"
                )

            val rotation by transition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 900,
                        easing = LinearEasing
                    ),
                    repeatMode = RepeatMode.Restart
                ),
                label = "catalogSpinnerRotation"
            )

            Text(
                text = "◌",
                modifier = Modifier.rotate(rotation),
                color = SyncCyan,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
