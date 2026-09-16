package com.example.xtreamplayer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xtreamplayer.data.SeriesStream
import com.example.xtreamplayer.data.VodStream
import com.example.xtreamplayer.viewmodel.AppViewModel

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
    initialMovie: VodStream? = null,
    initialSeries: SeriesStream? = null,
    initialSeason: String? = null,
    onInitialMovieConsumed: () -> Unit = {},
    onInitialSeriesConsumed: () -> Unit = {}
) {
    var currentSection by remember {
        mutableStateOf(HomeSection.HOME)
    }

    LaunchedEffect(initialMovie, initialSeries) {
        when {
            initialMovie != null -> {
                currentSection = HomeSection.MOVIES
            }

            initialSeries != null -> {
                currentSection = HomeSection.SERIES
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

            LiveContentScreen(
                categories = vm.liveCategories,
                streams = vm.live,
                onBack = {
                    currentSection = HomeSection.HOME
                },
                onPlay = { playData ->

                    val parts = playData.split(":")

                    if (parts.size >= 2) {

                        val id = parts[1].toIntOrNull()

                        if (id != null) {

                            vm.streamUrl(
                                type = "live",
                                id = id
                            )?.let(onPlay)
                        }
                    }
                }
            )
        }

        HomeSection.MOVIES -> {

            MovieContentScreen(
                categories = vm.movieCategories,
                movies = vm.movies,
                initialMovie = initialMovie,
                onInitialMovieConsumed = onInitialMovieConsumed,
                onBack = {
                    currentSection = HomeSection.HOME
                },
                onPlay = { playData ->

                    val parts = playData.split(":")

                    if (parts.size >= 2) {

                        val id = parts[1].toIntOrNull()

                        if (id != null) {

                            val movie =
                                vm.movies.firstOrNull {
                                    it.stream_id == id
                                }

                            if (movie != null) {

                                val extension =
                                    parts.getOrNull(2)
                                        ?: movie.container_extension

                                vm.streamUrl(
                                    type = "movie",
                                    id = id,
                                    extension = extension
                                )?.let { url ->

                                    onMoviePlay(
                                        url,
                                        movie
                                    )
                                }
                            }
                        }
                    }
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

                onSeriesPlay = { url, series, season ->
                    onSeriesPlay(
                        url,
                        series,
                        season
                    )
                },

                initialSeries = initialSeries,
                initialSeason = initialSeason,

                onInitialSeriesConsumed = onInitialSeriesConsumed,

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
    val accentColor = Color(0xFFCAEA00)
    val backgroundColor = Color(0xFF090909)
    val surfaceColor = Color(0xFF151515)

    Scaffold(
        containerColor = backgroundColor,

        topBar = {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(surfaceColor)
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "XTREAM PLAYER",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                TextButton(
                    onClick = {
                        vm.updateCatalog()
                    }
                ) {

                    Text(
                        text = "UPDATE",
                        color = accentColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                TextButton(
                    onClick = {
                        vm.logout()
                    }
                ) {

                    Text(
                        text = "LOGOUT",
                        color = Color.LightGray
                    )
                }
            }
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(backgroundColor)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(
                    modifier = Modifier.height(40.dp)
                )

                Text(
                    text = "BENVENUTO",
                    color = Color.Gray,
                    fontSize = 14.sp
                )

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                Text(
                    text = vm.auth?.user_info?.username
                        ?: vm.credentials?.username
                        ?: "",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(50.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth(),
                    horizontalArrangement = Arrangement.spacedBy(22.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    HomeCategoryCard(
                        title = "LIVE TV",
                        modifier = Modifier.width(210.dp),
                        onClick = onLiveClick
                    )

                    HomeCategoryCard(
                        title = "FILM",
                        modifier = Modifier.width(210.dp),
                        onClick = onMoviesClick
                    )

                    HomeCategoryCard(
                        title = "SERIE TV",
                        modifier = Modifier.width(210.dp),
                        onClick = onSeriesClick
                    )
                }

                Spacer(
                    modifier = Modifier.weight(1f)
                )

                val expiration =
                    vm.auth?.user_info?.exp_date

                if (!expiration.isNullOrBlank()) {

                    Text(
                        text = "SCADENZA: ${formatExpirationDate(expiration)}",
                        color = Color.Gray,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(
                            bottom = 18.dp
                        )
                    )
                }
            }

            if (vm.loading) {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Color.Black.copy(alpha = 0.55f)
                        ),
                    contentAlignment = Alignment.Center
                ) {

                    CircularProgressIndicator(
                        color = accentColor
                    )
                }
            }

            vm.error?.let { error ->

                Text(
                    text = error,
                    color = Color.Red,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(20.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun HomeCategoryCard(
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val accentColor = Color(0xFFCAEA00)

    Card(
        modifier = modifier
            .height(170.dp)
            .clickable {
                onClick()
            },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF151515)
        )
    ) {

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = title,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .align(Alignment.BottomCenter)
                    .background(accentColor)
            )
        }
    }
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
            java.text.SimpleDateFormat(
                "dd/MM/yyyy",
                java.util.Locale.getDefault()
            )

        date.format(
            java.util.Date(timestamp * 1000L)
        )

    } catch (_: Exception) {

        value
    }
}
