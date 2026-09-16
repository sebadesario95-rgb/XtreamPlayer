package com.example.xtreamplayer.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

private val HomeBlue = Color(0xFF1677FF)
private val HomeBackground = Color(0xFF05070B)
private val HomeSurface = Color(0xFF0D121A)
private val HomeMuted = Color(0xFF94A3B8)

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
            LiveContentScreen(
                title = "LIVE TV",
                categories = vm.liveCategories,
                streams = vm.live,
                vm = vm,
                initialCategoryId = initialLive?.category_id,
                onInitialLiveConsumed = {
                    onInitialLiveConsumed()
                },
                onBack = {
                    currentSection = HomeSection.HOME
                },
                onPlay = { playData ->

                    val parts = playData.split(":")

                    if (parts.size >= 2) {

                        val id = parts[1].toIntOrNull()

                        if (id != null) {

                            val liveStream =
                                vm.live.firstOrNull {
                                    it.stream_id == id
                                }

                            if (liveStream != null) {

                                vm.streamUrl(
                                    type = "live",
                                    id = id
                                )?.let { url ->

                                    onLivePlay(
                                        url,
                                        liveStream
                                    )
                                }
                            }
                        }
                    }
                }
            )
        }

        HomeSection.MOVIES -> {
            MovieContentScreen(
                title = "FILM",
                categories = vm.movieCategories,
                movies = vm.movies,
                vm = vm,
                initialMovie = initialMovie,
                onInitialMovieConsumed =
                    onInitialMovieConsumed,
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
    Scaffold(
        containerColor = HomeBackground,
        topBar = {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .background(Color(0xFF080B10))
                    .padding(horizontal = 28.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            HomeBlue,
                            RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "▶",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(
                    modifier = Modifier.width(12.dp)
                )

                Text(
                    text = "XTREAM PLAYER",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.weight(1f)
                )

                TextButton(
                    onClick = {
                        vm.updateCatalog()
                    }
                ) {
                    Text(
                        text = "AGGIORNA",
                        color = HomeBlue,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(
                    modifier = Modifier.width(6.dp)
                )

                TextButton(
                    onClick = {
                        vm.logout()
                    }
                ) {
                    Text(
                        text = "ESCI",
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
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF071326),
                            HomeBackground,
                            HomeBackground
                        )
                    )
                )
        ) {

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 30.dp)
            ) {

                val wideLayout = maxWidth >= 760.dp

                Column(
                    modifier = Modifier.fillMaxSize()
                ) {

                    Spacer(
                        modifier = Modifier.height(42.dp)
                    )

                    Text(
                        text = "BENTORNATO",
                        color = HomeBlue,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )

                    Spacer(
                        modifier = Modifier.height(7.dp)
                    )

                    Text(
                        text =
                            vm.auth?.user_info?.username
                                ?: vm.credentials?.username
                                ?: "Utente",
                        color = Color.White,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = "Cosa vuoi guardare oggi?",
                        color = HomeMuted,
                        fontSize = 16.sp
                    )

                    Spacer(
                        modifier = Modifier.height(36.dp)
                    )

                    if (wideLayout) {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.spacedBy(18.dp)
                        ) {

                            StreamingCategoryCard(
                                title = "LIVE TV",
                                subtitle =
                                    "Guarda i tuoi canali in diretta",
                                symbol = "●",
                                modifier = Modifier.weight(1f),
                                onClick = onLiveClick
                            )

                            StreamingCategoryCard(
                                title = "FILM",
                                subtitle =
                                    "Esplora il catalogo dei film",
                                symbol = "▶",
                                modifier = Modifier.weight(1f),
                                onClick = onMoviesClick
                            )

                            StreamingCategoryCard(
                                title = "SERIE TV",
                                subtitle =
                                    "Continua con le tue serie",
                                symbol = "▣",
                                modifier = Modifier.weight(1f),
                                onClick = onSeriesClick
                            )
                        }

                    } else {

                        Column(
                            verticalArrangement =
                                Arrangement.spacedBy(14.dp)
                        ) {

                            StreamingCategoryCard(
                                title = "LIVE TV",
                                subtitle =
                                    "Guarda i tuoi canali in diretta",
                                symbol = "●",
                                modifier =
                                    Modifier.fillMaxWidth(),
                                onClick = onLiveClick
                            )

                            StreamingCategoryCard(
                                title = "FILM",
                                subtitle =
                                    "Esplora il catalogo dei film",
                                symbol = "▶",
                                modifier =
                                    Modifier.fillMaxWidth(),
                                onClick = onMoviesClick
                            )

                            StreamingCategoryCard(
                                title = "SERIE TV",
                                subtitle =
                                    "Continua con le tue serie",
                                symbol = "▣",
                                modifier =
                                    Modifier.fillMaxWidth(),
                                onClick = onSeriesClick
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier.weight(1f)
                    )

                    AccountInfoCard(
                        vm = vm
                    )

                    Spacer(
                        modifier = Modifier.height(22.dp)
                    )
                }
            }

            if (vm.loading) {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Color.Black.copy(
                                alpha = 0.68f
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {

                    Column(
                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {

                        CircularProgressIndicator(
                            color = HomeBlue
                        )

                        Spacer(
                            modifier = Modifier.height(14.dp)
                        )

                        Text(
                            text = "Aggiornamento catalogo...",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            vm.error?.let { error ->

                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(20.dp),
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
                            vertical = 12.dp
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun StreamingCategoryCard(
    title: String,
    subtitle: String,
    symbol: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(190.dp)
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = HomeSurface
        ),
        border = BorderStroke(
            1.dp,
            Color(0xFF1E2938)
        )
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF14243C),
                            HomeSurface
                        )
                    )
                )
                .padding(22.dp)
        ) {

            Column(
                modifier =
                    Modifier.align(Alignment.BottomStart)
            ) {

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(
                            HomeBlue.copy(alpha = 0.15f),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = symbol,
                        color = HomeBlue,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(
                    modifier = Modifier.height(5.dp)
                )

                Text(
                    text = subtitle,
                    color = HomeMuted,
                    fontSize = 13.sp
                )
            }

            Text(
                text = "›",
                color = HomeBlue,
                fontSize = 32.sp,
                fontWeight = FontWeight.Light,
                modifier =
                    Modifier.align(Alignment.CenterEnd)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .align(Alignment.BottomCenter)
                    .background(HomeBlue)
            )
        }
    }
}

@Composable
private fun AccountInfoCard(
    vm: AppViewModel
) {
    val expiration =
        vm.auth?.user_info?.exp_date

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = HomeSurface.copy(alpha = 0.8f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.dp,
            Color(0x222B8CFF)
        )
    ) {

        Row(
            modifier = Modifier.padding(
                horizontal = 20.dp,
                vertical = 14.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = "ACCOUNT",
                    color = HomeMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(3.dp)
                )

                Text(
                    text =
                        vm.auth?.user_info?.username
                            ?: vm.credentials?.username
                            ?: "",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (!expiration.isNullOrBlank()) {

                Column(
                    horizontalAlignment = Alignment.End
                ) {

                    Text(
                        text = "SCADENZA",
                        color = HomeMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(3.dp)
                    )

                    Text(
                        text =
                            formatExpirationDate(
                                expiration
                            ),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
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
            java.util.Date(
                timestamp * 1000L
            )
        )

    } catch (_: Exception) {
        value
    }
}
