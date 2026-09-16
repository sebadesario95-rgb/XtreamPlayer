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
import com.example.xtreamplayer.data.VodStream
import com.example.xtreamplayer.viewmodel.AppViewModel

private enum class HomeSection {
    HOME,
    LIVE,
    MOVIES,
    SERIES
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    vm: AppViewModel,
    onPlay: (String) -> Unit,
    onMoviePlay: (String, VodStream) -> Unit,
    initialMovie: VodStream? = null,
    onInitialMovieConsumed: () -> Unit = {}
) {
    var currentSection by remember {
        mutableStateOf(HomeSection.HOME)
    }

    /*
     * Se arriviamo dal player con un film salvato,
     * partiamo direttamente dalla sezione FILM.
     */
    if (initialMovie != null && currentSection == HomeSection.HOME) {
        currentSection = HomeSection.MOVIES
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

                            val movie = vm.movies.firstOrNull {
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
                onBack = {
                    currentSection = HomeSection.HOME
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeMainScreen(
    vm: AppViewModel,
    onLiveClick: () -> Unit,
    onMoviesClick: () -> Unit,
    onSeriesClick: () -> Unit
) {
    val account = vm.auth?.user_info

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "XTREAM PLAYER",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {

                    TextButton(
                        onClick = {
                            vm.updateCatalog()
                        },
                        enabled = !vm.loading
                    ) {
                        Text(
                            "UPDATE",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    TextButton(
                        onClick = {
                            vm.logout()
                        }
                    ) {
                        Text("LOGOUT")
                    }
                }
            )
        }
    ) { pad ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .background(Color(0xFF090909))
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {

                Spacer(
                    modifier = Modifier.height(35.dp)
                )

                Text(
                    text = "Benvenuto ${account?.username ?: ""}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.weight(1f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    HomeCategoryCard(
                        title = "LIVE TV",
                        subtitle = "Canali televisivi",
                        onClick = onLiveClick
                    )

                    Spacer(
                        modifier = Modifier.width(20.dp)
                    )

                    HomeCategoryCard(
                        title = "FILM",
                        subtitle = "Film e cinema",
                        onClick = onMoviesClick
                    )

                    Spacer(
                        modifier = Modifier.width(20.dp)
                    )

                    HomeCategoryCard(
                        title = "SERIE TV",
                        subtitle = "Serie e stagioni",
                        onClick = onSeriesClick
                    )
                }

                Spacer(
                    modifier = Modifier.weight(1f)
                )

                account?.exp_date?.let { expiration ->

                    Text(
                        text = "SCADENZA ABBONAMENTO",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    Text(
                        text = formatExpirationDate(expiration),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(
                    modifier = Modifier.height(20.dp)
                )
            }

            if (vm.loading) {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        CircularProgressIndicator()

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )

                        Text("Aggiornamento catalogo...")
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeCategoryCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .height(190.dp)
            .clickable {
                onClick()
            },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF151515)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = title,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Text(
                text = subtitle,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatExpirationDate(
    expiration: String
): String {
    return try {

        val timestamp = expiration.toLong()

        val date = java.text.SimpleDateFormat(
            "dd/MM/yyyy",
            java.util.Locale.getDefault()
        )

        date.format(
            java.util.Date(timestamp * 1000)
        )

    } catch (e: Exception) {
        expiration
    }
}
