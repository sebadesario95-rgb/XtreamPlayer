package com.example.xtreamplayer.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xtreamplayer.data.LiveStream
import com.example.xtreamplayer.data.SeriesStream
import com.example.xtreamplayer.data.VodStream
import com.example.xtreamplayer.viewmodel.AppViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val HomeBlue = Color(0xFF1677FF)
private val HomeBlueLight = Color(0xFF20B7FF)
private val HomeBackground = Color(0xFF02060B)
private val HomeCard = Color(0xE60A1420)
private val HomeBorder = Color(0xFF1A3047)
private val HomeMuted = Color(0xFF9AA8B8)

private enum class HomeSection {
    HOME,
    LIVE,
    MOVIES,
    SERIES
}

@Composable
fun HomeScreen(
    vm: AppViewModel,
    onPlay: (String) -> Unit,
    onMoviePlay: (String, VodStream) -> Unit,
    onSeriesPlay: (String, SeriesStream, String) -> Unit,
    onLivePlay: (String, LiveStream) -> Unit,
    initialMovie: VodStream? = null,
    initialSeries: SeriesStream? = null,
    initialSeason: String? = null,
    initialLive: LiveStream? = null,
    onInitialMovieConsumed: () -> Unit = {},
    onInitialSeriesConsumed: () -> Unit = {},
    onInitialLiveConsumed: () -> Unit = {}
) {

    var currentSection by remember {
        mutableStateOf(HomeSection.HOME)
    }

    LaunchedEffect(
        initialMovie,
        initialSeries,
        initialLive
    ) {
        when {
            initialMovie != null -> {
                currentSection = HomeSection.MOVIES
            }

            initialSeries != null -> {
                currentSection = HomeSection.SERIES
            }

            initialLive != null -> {
                currentSection = HomeSection.LIVE
            }
        }
    }

    when (currentSection) {

        HomeSection.HOME -> {
            HomeMainScreen(
                vm = vm,
                onLiveClick = {
                    currentSection = HomeSection.LIVE
                },
                onMoviesClick = {
                    currentSection = HomeSection.MOVIES
                },
                onSeriesClick = {
                    currentSection = HomeSection.SERIES
                }
            )
        }

        HomeSection.LIVE -> {
            LiveTvScreen(
                categories = vm.liveCategories,
                streams = vm.live,
                vm = vm,
                initialLive = initialLive,
                onLivePlay = { url, liveStream ->
                    onInitialLiveConsumed()
                    onLivePlay(url, liveStream)
                },
                onBack = {
                    onInitialLiveConsumed()
                    currentSection = HomeSection.HOME
                }
            )
        }

        HomeSection.MOVIES -> {
            MovieContentScreen(
                title = "FILM",
                categories = vm.movieCategories,
                movies = vm.movies,
                vm = vm,

                onPlay = { url ->
                    onPlay(url)
                },

                onMoviePlay = { url, movie ->
                    onMoviePlay(
                        url,
                        movie
                    )
                },

                initialMovie = initialMovie,

                onInitialMovieConsumed = {
                    onInitialMovieConsumed()
                },

                onBack = {
                    currentSection = HomeSection.HOME
                }
            )
        }

        HomeSection.SERIES -> {
            SeriesContentScreen(
                title = "SERIE TV",
                categories = vm.seriesCategories,
                series = vm.series,
                vm = vm,
                onPlay = onPlay,
                onSeriesPlay = {
                        url,
                        series,
                        season ->

                    onSeriesPlay(
                        url,
                        series,
                        season
                    )
                },
                initialSeries = initialSeries,
                initialSeason = initialSeason,
                onInitialSeriesConsumed =
                    onInitialSeriesConsumed,
                onBack = {
                    currentSection = HomeSection.HOME
                }
            )
        }
    }
}

@Composable
private fun HomeMainScreen(
    vm: AppViewModel,
    onLiveClick: () -> Unit,
    onMoviesClick: () -> Unit,
    onSeriesClick: () -> Unit
) {

    var currentTime by remember {
        mutableStateOf(getCurrentTime())
    }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = getCurrentTime()
            delay(1000L)
        }
    }

    val expiration =
        vm.auth?.user_info?.exp_date

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HomeBackground)
    ) {

        HomeBackgroundDecoration()

        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(
                    start = 42.dp,
                    top = 28.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = "X",
                color = HomeBlue,
                fontSize = 46.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(
                modifier = Modifier.width(6.dp)
            )

            Column {

                Text(
                    text = "TREAM",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "P L A Y E R",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 3.sp
                )
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(
                    end = 42.dp,
                    top = 30.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Surface(
                color = Color(0x66061321),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(
                    1.dp,
                    Color(0xAA0B4B8F)
                )
            ) {

                Row(
                    modifier = Modifier.padding(
                        horizontal = 14.dp,
                        vertical = 8.dp
                    ),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Text(
                        text = "◇",
                        color = HomeBlue,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )

                    Column {

                        Text(
                            text = "VPN",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "ATTIVA",
                            color = HomeBlueLight,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            Spacer(
                modifier = Modifier.width(20.dp)
            )

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(30.dp)
                    .background(
                        Color.White.copy(alpha = 0.35f)
                    )
            )

            Spacer(
                modifier = Modifier.width(20.dp)
            )

            Text(
                text = currentTime,
                color = Color.White,
                fontSize = 21.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 72.dp),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Text(
                text = "B E N V E N U T O   S U",
                color = HomeMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(
                modifier = Modifier.height(7.dp)
            )

            Text(
                text = "X T R E A M   P L A Y E R",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 2.sp
            )

            Spacer(
                modifier = Modifier.height(7.dp)
            )

            Text(
                text = "IL TUO MONDO IN UN'UNICA APP",
                color = HomeMuted,
                fontSize = 10.sp,
                letterSpacing = 2.sp
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 105.dp),
            horizontalArrangement =
                Arrangement.spacedBy(18.dp),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            MinimalHomeCard(
                title = "LIVE TV",
                icon = "▣",
                modifier = Modifier.weight(1f),
                onClick = onLiveClick
            )

            MinimalHomeCard(
                title = "FILM",
                icon = "▶",
                modifier = Modifier.weight(1f),
                onClick = onMoviesClick
            )

            MinimalHomeCard(
                title = "SERIE TV",
                icon = "▤",
                modifier = Modifier.weight(1f),
                onClick = onSeriesClick
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(
                    start = 42.dp,
                    bottom = 30.dp
                )
        ) {

            Text(
                text =
                    if (!expiration.isNullOrBlank()) {
                        "SCADENZA: ${
                            formatExpirationDate(
                                expiration
                            )
                        }"
                    } else {
                        "SCADENZA: --/--/----"
                    },
                color = Color(0xFFD3D9E0),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Box(
                modifier = Modifier
                    .width(38.dp)
                    .height(3.dp)
                    .background(
                        HomeBlue,
                        RoundedCornerShape(50)
                    )
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = 34.dp,
                    bottom = 22.dp
                ),
            horizontalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            BottomActionButton(
                title = "IMPOSTAZIONI",
                icon = "⚙",
                onClick = {
                    // TODO:
                    // collegheremo qui la schermata
                    // Impostazioni.
                }
            )

            BottomActionButton(
                title = "AGGIORNA",
                icon = "↻",
                highlighted = true,
                onClick = {
                    vm.updateCatalog()
                }
            )
        }

        if (vm.loading) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color.Black.copy(alpha = 0.65f)
                    ),
                contentAlignment = Alignment.Center
            ) {

                Column(
                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    CircularProgressIndicator(
                        color = HomeBlue,
                        strokeWidth = 3.dp
                    )

                    Spacer(
                        modifier = Modifier.height(14.dp)
                    )

                    Text(
                        text = "Aggiornamento catalogo...",
                        color = Color.White,
                        fontSize = 13.sp
                    )
                }
            }
        }

        vm.error?.let { error ->

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 22.dp),
                color =
                    MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(12.dp)
            ) {

                Text(
                    text = error,
                    color =
                        MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(
                        horizontal = 18.dp,
                        vertical = 10.dp
                    ),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun HomeBackgroundDecoration() {

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        Box(
            modifier = Modifier
                .size(520.dp)
                .offset(
                    x = (-250).dp,
                    y = 100.dp
                )
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x551677FF),
                            Color.Transparent
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .size(560.dp)
                .align(Alignment.CenterEnd)
                .offset(x = 260.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x441677FF),
                            Color.Transparent
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0x220066FF),
                            Color(0x33001435),
                            Color.Transparent
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxWidth(0.72f)
                .height(2.dp)
                .align(Alignment.BottomCenter)
                .offset(y = (-105).dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            HomeBlue.copy(alpha = 0.15f),
                            HomeBlueLight.copy(alpha = 0.75f),
                            HomeBlue.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@Composable
private fun MinimalHomeCard(
    title: String,
    icon: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {

    var isFocused by remember {
        mutableStateOf(false)
    }

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.035f else 1f,
        label = "homeCardFocusScale"
    )

    Card(
        modifier = modifier
            .height(170.dp)
            .scale(scale)
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
            }
            .focusable()
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = HomeCard
        ),
        border = BorderStroke(
            width = if (isFocused) 3.dp else 1.dp,
            color = if (isFocused) {
                HomeBlueLight
            } else {
                HomeBorder
            }
        )
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = if (isFocused) {
                            listOf(
                                Color(0xFF153454),
                                Color(0xFF081522)
                            )
                        } else {
                            listOf(
                                Color(0xFF102033),
                                Color(0xFF07101A)
                            )
                        }
                    )
                )
        ) {

            Column(
                modifier =
                    Modifier.align(Alignment.Center),
                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {

                Text(
                    text = icon,
                    color = if (isFocused) {
                        HomeBlueLight
                    } else {
                        Color.White
                    },
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Light
                )

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(
                    modifier = Modifier.height(14.dp)
                )

                Box(
                    modifier = Modifier
                        .width(
                            if (isFocused) 54.dp else 36.dp
                        )
                        .height(
                            if (isFocused) 4.dp else 3.dp
                        )
                        .background(
                            if (isFocused) {
                                HomeBlueLight
                            } else {
                                HomeBlue.copy(alpha = 0.65f)
                            },
                            RoundedCornerShape(50)
                        )
                )
            }
        }
    }
}

@Composable
private fun BottomActionButton(
    title: String,
    icon: String,
    highlighted: Boolean = false,
    onClick: () -> Unit
) {

    var isFocused by remember {
        mutableStateOf(false)
    }

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.05f else 1f,
        label = "bottomActionFocusScale"
    )

    Surface(
        modifier = Modifier
            .width(118.dp)
            .height(72.dp)
            .scale(scale)
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
            }
            .focusable()
            .clickable {
                onClick()
            },
        color = if (isFocused) {
            Color(0xE6102942)
        } else {
            Color(0xB207101A)
        },
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            width = if (isFocused) 3.dp else 1.dp,
            color = when {
                isFocused -> HomeBlueLight
                highlighted -> HomeBlue.copy(alpha = 0.35f)
                else -> HomeBorder
            }
        )
    ) {

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Text(
                text = icon,
                color = when {
                    isFocused -> HomeBlueLight
                    highlighted -> HomeBlue
                    else -> Color(0xFFB7C4D4)
                },
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = title,
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = if (isFocused) {
                    FontWeight.Bold
                } else {
                    FontWeight.Medium
                },
                letterSpacing = 0.7.sp
            )
        }
    }
}

private fun getCurrentTime(): String {

    return SimpleDateFormat(
        "HH:mm",
        Locale.getDefault()
    ).format(
        Date()
    )
}

private fun formatExpirationDate(
    value: String
): String {

    if (value.length != 10) {
        return value
    }

    return try {

        val timestamp =
            value.toLong()

        val date =
            SimpleDateFormat(
                "dd/MM/yyyy",
                Locale.getDefault()
            )

        date.format(
            Date(
                timestamp * 1000L
            )
        )

    } catch (_: Exception) {
        value
    }
}
