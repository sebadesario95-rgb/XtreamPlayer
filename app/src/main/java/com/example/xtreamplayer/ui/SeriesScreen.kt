package com.example.xtreamplayer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.xtreamplayer.data.Category
import com.example.xtreamplayer.data.SeriesEpisode
import com.example.xtreamplayer.data.SeriesInfoResponse
import com.example.xtreamplayer.data.SeriesSeason
import com.example.xtreamplayer.data.SeriesStream
import com.example.xtreamplayer.viewmodel.AppViewModel

private val SeriesAccent = Color(0xFFCAEA00)

@Composable
fun SeriesContentScreen(
title: String,
categories: List<Category>,
series: List<SeriesStream>,
vm: AppViewModel,
onPlay: (String) -> Unit,
onBack: () -> Unit
) {
var selectedCategoryId by remember {
mutableStateOf<String?>(null)
}

```
var selectedSeries by remember {
    mutableStateOf<SeriesStream?>(null)
}

val filteredSeries =
    if (selectedCategoryId == null) {
        series
    } else {
        series.filter {
            it.category_id == selectedCategoryId
        }
    }

if (selectedSeries == null) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090909))
            .padding(20.dp)
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
                modifier = Modifier.width(18.dp)
            )

            Text(
                text = title,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(
            modifier = Modifier.height(15.dp)
        )

        Row(
            modifier = Modifier.fillMaxSize()
        ) {

            SeriesCategorySidebar(
                categories = categories,
                selectedCategoryId = selectedCategoryId,
                onCategorySelected = {
                    selectedCategoryId = it
                }
            )

            Spacer(
                modifier = Modifier.width(22.dp)
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {

                if (filteredSeries.isEmpty()) {

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {

                        Text(
                            "Nessuna serie disponibile.",
                            color = MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                        )
                    }

                } else {

                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(
                            minSize = 170.dp
                        ),
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement =
                            Arrangement.spacedBy(20.dp),
                        horizontalArrangement =
                            Arrangement.spacedBy(18.dp),
                        contentPadding =
                            PaddingValues(
                                bottom = 30.dp
                            )
                    ) {

                        items(filteredSeries) { show ->

                            SeriesPosterCard(
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
            }
        }
    }

} else {

    SeriesDetailsScreen(
        series = selectedSeries!!,
        info = vm.selectedSeriesInfo,
        loading = vm.loadingSeriesInfo,
        vm = vm,
        onPlay = onPlay,
        onBack = {
            vm.clearSeriesInfo()
            selectedSeries = null
        }
    )
}
```

}

@Composable
private fun SeriesCategorySidebar(
categories: List<Category>,
selectedCategoryId: String?,
onCategorySelected: (String?) -> Unit
) {

```
LazyColumn(
    modifier = Modifier
        .width(220.dp)
        .fillMaxHeight(),
    verticalArrangement = Arrangement.spacedBy(6.dp),
    contentPadding = PaddingValues(
        bottom = 20.dp
    )
) {

    item {

        SeriesSidebarItem(
            name = "TUTTI",
            selected = selectedCategoryId == null,
            onClick = {
                onCategorySelected(null)
            }
        )
    }

    items(categories) { category ->

        val id = category.category_id
            ?: return@items

        SeriesSidebarItem(
            name = category.category_name
                ?: "Categoria",
            selected = selectedCategoryId == id,
            onClick = {
                onCategorySelected(id)
            }
        )
    }
}
```

}

@Composable
private fun SeriesSidebarItem(
name: String,
selected: Boolean,
onClick: () -> Unit
) {

```
Box(
    modifier = Modifier
        .fillMaxWidth()
        .height(52.dp)
        .clip(
            RoundedCornerShape(10.dp)
        )
        .background(
            if (selected) {
                SeriesAccent
            } else {
                Color(0xFF151515)
            }
        )
        .clickable {
            onClick()
        }
        .padding(horizontal = 16.dp),
    contentAlignment = Alignment.CenterStart
) {

    Text(
        text = name,
        fontSize = 14.sp,
        fontWeight = if (selected) {
            FontWeight.ExtraBold
        } else {
            FontWeight.Medium
        },
        color = if (selected) {
            Color.Black
        } else {
            Color.White
        },
        maxLines = 1
    )
}
```

}

@Composable
private fun SeriesPosterCard(
series: SeriesStream,
onClick: () -> Unit
) {

```
Card(
    modifier = Modifier
        .fillMaxWidth()
        .clickable {
            onClick()
        },
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(
        containerColor = Color(0xFF151515)
    ),
    elevation = CardDefaults.cardElevation(
        defaultElevation = 6.dp
    )
) {

    Column {

        if (!series.cover.isNullOrBlank()) {

            AsyncImage(
                model = series.cover,
                contentDescription = series.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(245.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 12.dp,
                            topEnd = 12.dp
                        )
                    ),
                contentScale = ContentScale.Crop
            )

        } else {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(245.dp)
                    .background(
                        Color(0xFF202020)
                    ),
                contentAlignment = Alignment.Center
            ) {

                Text(
                    "NESSUNA IMMAGINE",
                    color = MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
                )
            }
        }

        Column(
            modifier = Modifier.padding(14.dp)
        ) {

            Text(
                text = series.name ?: "Serie",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2
            )

            if (!series.rating.isNullOrBlank()) {

                Spacer(
                    modifier = Modifier.height(5.dp)
                )

                Text(
                    text = "★ ${series.rating}",
                    fontSize = 13.sp,
                    color = SeriesAccent
                )
            }
        }
    }
}
```

}

@Composable
private fun SeriesDetailsScreen(
series: SeriesStream,
info: SeriesInfoResponse?,
loading: Boolean,
vm: AppViewModel,
onPlay: (String) -> Unit,
onBack: () -> Unit
) {

```
var selectedSeason by remember {
    mutableStateOf<Int?>(null)
}

Column(
    modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFF090909))
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
            horizontalAlignment =
                Alignment.CenterHorizontally,
            verticalArrangement =
                Arrangement.Center
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
            color = MaterialTheme
                .colorScheme
                .onSurfaceVariant
        )

    } else {

        if (!info.info?.plot.isNullOrBlank()) {

            Text(
                text = info.info?.plot ?: "",
                fontSize = 14.sp,
                color = MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )
        }

        val seasons =
            info.seasons ?: emptyList()

        if (selectedSeason == null) {

            Text(
                text = "STAGIONI",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(15.dp)
            )

            if (seasons.isEmpty()) {

                val seasonNumbers =
                    info.episodes
                        ?.keys
                        ?.mapNotNull {
                            it.toIntOrNull()
                        }
                        ?.sorted()
                        ?: emptyList()

                if (seasonNumbers.isEmpty()) {

                    Text(
                        text =
                            "Nessuna stagione disponibile.",
                        color = MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                    )

                } else {

                    LazyColumn(
                        verticalArrangement =
                            Arrangement.spacedBy(12.dp),
                        contentPadding =
                            PaddingValues(
                                bottom = 20.dp
                            )
                    ) {

                        items(seasonNumbers) {
                            seasonNumber ->

                            SeasonNumberCard(
                                seasonNumber =
                                    seasonNumber,
                                onClick = {
                                    selectedSeason =
                                        seasonNumber
                                }
                            )
                        }
                    }
                }

            } else {

                LazyColumn(
                    verticalArrangement =
                        Arrangement.spacedBy(12.dp),
                    contentPadding =
                        PaddingValues(
                            bottom = 20.dp
                        )
                ) {

                    items(seasons) { season ->

                        SeasonCard(
                            season = season,
                            onClick = {
                                selectedSeason =
                                    season.season_number
                            }
                        )
                    }
                }
            }

        } else {

            val episodes =
                info.episodes
                    ?.get(
                        selectedSeason.toString()
                    )
                    ?: emptyList()

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                TextButton(
                    onClick = {
                        selectedSeason = null
                    }
                ) {
                    Text("← STAGIONI")
                }

                Text(
                    text =
                        "STAGIONE $selectedSeason",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(
                modifier = Modifier.height(15.dp)
            )

            if (episodes.isEmpty()) {

                Text(
                    text =
                        "Nessun episodio disponibile.",
                    color = MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
                )

            } else {

                LazyColumn(
                    verticalArrangement =
                        Arrangement.spacedBy(12.dp),
                    contentPadding =
                        PaddingValues(
                            bottom = 20.dp
                        )
                ) {

                    items(episodes) { episode ->

                        EpisodeCard(
                            episode = episode,
                            onClick = {

                                val episodeId =
                                    episode.id
                                        ?.toIntOrNull()

                                if (episodeId != null) {

                                    vm.streamUrl(
                                        type = "series",
                                        id = episodeId,
                                        extension =
                                            episode.container_extension
                                    )?.let(onPlay)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
```

}

@Composable
private fun SeasonCard(
season: SeriesSeason,
onClick: () -> Unit
) {

```
Card(
    modifier = Modifier
        .fillMaxWidth()
        .height(80.dp)
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
            .padding(18.dp),
        verticalArrangement =
            Arrangement.Center
    ) {

        Text(
            text = season.name
                ?: "Stagione ${
                    season.season_number ?: ""
                }",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
```

}

@Composable
private fun SeasonNumberCard(
seasonNumber: Int,
onClick: () -> Unit
) {

```
Card(
    modifier = Modifier
        .fillMaxWidth()
        .height(80.dp)
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
            .padding(18.dp),
        verticalArrangement =
            Arrangement.Center
    ) {

        Text(
            text = "STAGIONE $seasonNumber",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
```

}

@Composable
private fun EpisodeCard(
episode: SeriesEpisode,
onClick: () -> Unit
) {

```
Card(
    modifier = Modifier
        .fillMaxWidth()
        .height(85.dp)
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
        verticalArrangement =
            Arrangement.Center
    ) {

        Text(
            text =
                "EPISODIO ${
                    episode.episode_num ?: ""
                }",
            fontSize = 13.sp,
            color = SeriesAccent,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        Text(
            text =
                episode.title
                    ?: episode.info?.name
                    ?: "Episodio",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
```

}
