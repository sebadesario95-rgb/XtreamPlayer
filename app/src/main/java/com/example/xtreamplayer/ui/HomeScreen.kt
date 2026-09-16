package com.example.xtreamplayer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xtreamplayer.viewmodel.AppViewModel

private enum class HomeSection {
    HOME,

    LIVE,
    LIVE_CONTENT,

    MOVIES,
    MOVIE_CONTENT,

    SERIES,
    SERIES_CONTENT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    vm: AppViewModel,
    onPlay: (String) -> Unit
) {

    var currentSection by remember {
        mutableStateOf(HomeSection.HOME)
    }

    var selectedCategoryId by remember {
        mutableStateOf<String?>(null)
    }

    when (currentSection) {

        // =====================================================
        // HOME
        // =====================================================

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

        // =====================================================
        // LIVE - CATEGORIE
        // =====================================================

        HomeSection.LIVE -> {

            CategoryScreen(
                title = "LIVE TV",

                categories = vm.liveCategories,

                onCategoryClick = { categoryId ->

                    selectedCategoryId = categoryId

                    currentSection = HomeSection.LIVE_CONTENT
                },

                onBack = {

                    currentSection = HomeSection.HOME
                }
            )
        }

        // =====================================================
        // LIVE - CANALI
        // =====================================================

        HomeSection.LIVE_CONTENT -> {

            val categoryId = selectedCategoryId

            val channels = vm.live.filter {
                it.category_id == categoryId
            }

            LiveContentScreen(
                title = "LIVE TV",

                streams = channels,

                onPlay = { channel ->

                    channel.stream_id?.let { id ->

                        vm.streamUrl(
                            type = "live",
                            id = id
                        )?.let(onPlay)
                    }
                },

                onBack = {

                    currentSection = HomeSection.LIVE
                }
            )
        }

        // =====================================================
        // FILM - CATEGORIE
        // =====================================================

        HomeSection.MOVIES -> {

            CategoryScreen(
                title = "FILM",

                categories = vm.movieCategories,

                onCategoryClick = { categoryId ->

                    selectedCategoryId = categoryId

                    currentSection = HomeSection.MOVIE_CONTENT
                },

                onBack = {

                    currentSection = HomeSection.HOME
                }
            )
        }

        // =====================================================
        // FILM - LISTA
        // =====================================================

        HomeSection.MOVIE_CONTENT -> {

            val categoryId = selectedCategoryId

            val movies = vm.movies.filter {
                it.category_id == categoryId
            }

            MovieContentScreen(
                title = "FILM",

                movies = movies,

                onPlay = { movie ->

                    movie.stream_id?.let { id ->

                        vm.streamUrl(
                            type = "movie",
                            id = id,
                            extension = movie.container_extension
                        )?.let(onPlay)
                    }
                },

                onBack = {

                    currentSection = HomeSection.MOVIES
                }
            )
        }

        // =====================================================
        // SERIE TV - CATEGORIE
        // =====================================================

        HomeSection.SERIES -> {

            CategoryScreen(
                title = "SERIE TV",

                categories = vm.seriesCategories,

                onCategoryClick = { categoryId ->

                    selectedCategoryId = categoryId

                    currentSection = HomeSection.SERIES_CONTENT
                },

                onBack = {

                    currentSection = HomeSection.HOME
                }
            )
        }

        // =====================================================
        // SERIE TV - LISTA SERIE
        // =====================================================

        HomeSection.SERIES_CONTENT -> {

            val categoryId = selectedCategoryId

            val series = vm.series.filter {
                it.category_id == categoryId
            }

            SeriesContentScreen(
                title = "SERIE TV",

                series = series,

                vm = vm,

                onPlay = onPlay,

                onBack = {

                    currentSection = HomeSection.SERIES
                }
            )
        }
    }
}


// =============================================================
// HOME PRINCIPALE
// =============================================================

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

                    // -------------------------------------------------
                    // UPDATE
                    // -------------------------------------------------

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

                    // -------------------------------------------------
                    // LOGOUT
                    // -------------------------------------------------

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
                .background(
                    Color(0xFF090909)
                )
        ) {

            Column(

                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        horizontal = 24.dp
                    )
            ) {

                Spacer(
                    modifier = Modifier.height(35.dp)
                )

                // -------------------------------------------------
                // BENVENUTO
                // -------------------------------------------------

                Text(

                    text = "Benvenuto ${account?.username ?: ""}",

                    style = MaterialTheme.typography.headlineMedium,

                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(45.dp)
                )

                // -------------------------------------------------
                // LE 3 CATEGORIE PRINCIPALI
                // -------------------------------------------------

                Row(

                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(
                            rememberScrollState()
                        ),

                    horizontalArrangement =
                        Arrangement.spacedBy(20.dp)
                ) {

                    // LIVE TV

                    HomeCategoryCard(

                        title = "LIVE TV",

                        subtitle = "Canali televisivi",

                        onClick = onLiveClick
                    )

                    // FILM

                    HomeCategoryCard(

                        title = "FILM",

                        subtitle = "Film e cinema",

                        onClick = onMoviesClick
                    )

                    // SERIE TV

                    HomeCategoryCard(

                        title = "SERIE TV",

                        subtitle = "Serie e stagioni",

                        onClick = onSeriesClick
                    )
                }

                Spacer(
                    modifier = Modifier.weight(1f)
                )

                // -------------------------------------------------
                // SCADENZA
                // -------------------------------------------------

                account?.exp_date?.let { expiration ->

                    Text(

                        text = "SCADENZA ABBONAMENTO",

                        modifier = Modifier.fillMaxWidth(),

                        textAlign = TextAlign.Center,

                        color =
                            MaterialTheme.colorScheme.onSurfaceVariant,

                        fontSize = 13.sp
                    )

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    Text(

                        text = formatExpirationDate(
                            expiration
                        ),

                        modifier = Modifier.fillMaxWidth(),

                        textAlign = TextAlign.Center,

                        fontSize = 17.sp,

                        fontWeight = FontWeight.Bold,

                        color =
                            MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(
                    modifier = Modifier.height(20.dp)
                )
            }

            // -----------------------------------------------------
            // LOADING UPDATE
            // -----------------------------------------------------

            if (vm.loading) {

                Box(

                    modifier = Modifier.fillMaxSize(),

                    contentAlignment =
                        Alignment.Center
                ) {

                    Column(

                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {

                        CircularProgressIndicator()

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )

                        Text(
                            "Aggiornamento catalogo..."
                        )
                    }
                }
            }
        }
    }
}


// =============================================================
// CARD HOME
// =============================================================

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

        shape = RoundedCornerShape(18.dp),

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

            verticalArrangement =
                Arrangement.Center
        ) {

            Text(

                text = title,

                fontSize = 28.sp,

                fontWeight = FontWeight.ExtraBold,

                color =
                    MaterialTheme.colorScheme.primary
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Text(

                text = subtitle,

                fontSize = 15.sp,

                color =
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


// =============================================================
// DATA SCADENZA
// =============================================================

private fun formatExpirationDate(
    expiration: String
): String {

    return try {

        val timestamp =
            expiration.toLong()

        val date =
            java.text.SimpleDateFormat(
                "dd/MM/yyyy",
                java.util.Locale.getDefault()
            )

        date.format(
            java.util.Date(
                timestamp * 1000
            )
        )

    } catch (e: Exception) {

        expiration
    }
}
