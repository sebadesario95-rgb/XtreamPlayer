package com.example.xtreamplayer.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xtreamplayer.data.SeriesEpisode
import com.example.xtreamplayer.data.SeriesInfoResponse
import com.example.xtreamplayer.data.SeriesStream
import com.example.xtreamplayer.data.SeriesSeason
import com.example.xtreamplayer.viewmodel.AppViewModel

@Composable
fun SeriesContentScreen(
    title: String,
    series: List<SeriesStream>,
    vm: AppViewModel,
    onPlay: (String) -> Unit,
    onBack: () -> Unit
) {

    var selectedSeries by remember {
        mutableStateOf<SeriesStream?>(null)
    }

    if (selectedSeries == null) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                TextButton(
                    onClick = onBack
                ) {
                    Text("← INDIETRO")
                }

                Spacer(
                    modifier = Modifier.width(20.dp)
                )

                Text(
                    text = title,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(
                modifier = Modifier.height(25.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {

                items(series) { show ->

                    SeriesCard(
                        series = show,
                        onClick = {

                            show.series_id?.let { id ->
                                vm.loadSeriesInfo(id)
                                selectedSeries = show
                            }
                        }
                    )
                }
            }
        }

    } else {

        SeriesDetailsScreen(
            series = selectedSeries!!,
            info = vm.selectedSeriesInfo,
            loading = vm.loadingSeriesInfo,
            onPlay = onPlay,
            onBack = {

                vm.clearSeriesInfo()
                selectedSeries = null
            }
        )
    }
}

@Composable
private fun SeriesCard(
    series: SeriesStream,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .clickable {
                onClick()
            },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF151515)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 5.dp
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = 20.dp,
                    vertical = 14.dp
                ),
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = series.name ?: "Serie",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            if (!series.genre.isNullOrBlank()) {

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = series.genre,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SeriesDetailsScreen(
    series: SeriesStream,
    info: SeriesInfoResponse?,
    loading: Boolean,
    onPlay: (String) -> Unit,
    onBack: () -> Unit
) {

    var selectedSeason by remember {
        mutableStateOf<Int?>(null)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            TextButton(
                onClick = onBack
            ) {
                Text("← INDIETRO")
            }

            Spacer(
                modifier = Modifier.width(20.dp)
            )

            Text(
                text = series.name ?: "Serie",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        if (loading) {

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                CircularProgressIndicator()

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Text("Caricamento serie...")
            }

        } else if (info == null) {

            Text(
                text = "Nessun dettaglio disponibile.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

        } else {

            if (!info.info?.plot.isNullOrBlank()) {

                Text(
                    text = info.info?.plot ?: "",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(
                    modifier = Modifier.height(20.dp)
                )
            }

            val seasons = info.seasons ?: emptyList()

            if (selectedSeason == null) {

                Text(
                    text = "STAGIONI",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(15.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {

                    items(seasons) { season ->

                        SeasonCard(
                            season = season,
                            onClick = {
                                selectedSeason = season.season_number
                            }
                        )
                    }
                }

            } else {

                val episodes = info.episodes
                    ?.get(selectedSeason.toString())
                    ?: emptyList()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    TextButton(
                        onClick = {
                            selectedSeason = null
                        }
                    ) {
                        Text("← STAGIONI")
                    }

                    Text(
                        text = "STAGIONE $selectedSeason",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(
                    modifier = Modifier.height(15.dp)
                )

                if (episodes.isEmpty()) {

                    Text(
                        text = "Nessun episodio disponibile.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                } else {

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 20.dp)
                    ) {

                        items(episodes) { episode ->

                            EpisodeCard(
                                episode = episode,
                                onClick = {

                                    val episodeId =
                                        episode.id?.toIntOrNull()

                                    if (episodeId != null) {

                                        val url = buildSeriesEpisodeUrl(
                                            episodeId = episodeId,
                                            extension = episode.container_extension
                                        )

                                        if (url != null) {
                                            onPlay(url)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SeasonCard(
    season: SeriesSeason,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable {
                onClick()
            },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF151515)
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = season.name
                    ?: "Stagione ${season.season_number ?: ""}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun EpisodeCard(
    episode: SeriesEpisode,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(85.dp)
            .clickable {
                onClick()
            },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF151515)
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = 20.dp,
                    vertical = 14.dp
                ),
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = "EPISODIO ${episode.episode_num ?: ""}",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = episode.title
                    ?: episode.info?.name
                    ?: "Episodio",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
