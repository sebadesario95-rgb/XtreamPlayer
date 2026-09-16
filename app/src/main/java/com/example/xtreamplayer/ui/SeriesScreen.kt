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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.xtreamplayer.data.Category
import com.example.xtreamplayer.data.SeriesEpisode
import com.example.xtreamplayer.data.SeriesStream
import com.example.xtreamplayer.viewmodel.AppViewModel

@Composable
fun SeriesContentScreen(
    title: String,
    categories: List<Category>,
    series: List<SeriesStream>,
    vm: AppViewModel,
    onPlay: (String) -> Unit,
    onSeriesPlay: (String, SeriesStream, String) -> Unit = { url, _, _ ->
        onPlay(url)
    },
    initialSeries: SeriesStream? = null,
    initialSeason: String? = null,
    onInitialSeriesConsumed: () -> Unit = {},
    onBack: () -> Unit
) {
    var selectedCategoryId by remember {
        mutableStateOf<String?>(null)
    }

    var selectedSeries by remember {
        mutableStateOf<SeriesStream?>(initialSeries)
    }

    var searchOpen by remember {
        mutableStateOf(false)
    }

    var searchQuery by remember {
        mutableStateOf("")
    }

    LaunchedEffect(initialSeries) {
        if (initialSeries != null) {
            selectedSeries = initialSeries
            initialSeries.series_id?.let {
                vm.loadSeriesInfo(it)
            }
            onInitialSeriesConsumed()
        }
    }

    val filteredSeries = remember(
        series,
        selectedCategoryId,
        searchQuery
    ) {
        if (searchQuery.isNotBlank()) {
            series.filter {
                it.name?.contains(
                    searchQuery,
                    ignoreCase = true
                ) == true
            }
        } else if (selectedCategoryId == null) {
            series
        } else {
            series.filter {
                it.category_id == selectedCategoryId
            }
        }
    }

    if (selectedSeries != null) {
        SeriesDetailScreen(
            series = selectedSeries!!,
            vm = vm,
            initialSeason = initialSeason,
            onPlay = onPlay,
            onSeriesPlay = onSeriesPlay,
            onBack = {
                selectedSeries = null
                vm.clearSeriesInfo()
            }
        )
    } else {
        SeriesWithSidebar(
            title = title,
            categories = categories,
            selectedCategoryId = selectedCategoryId,
            onCategorySelected = {
                selectedCategoryId = it
            },
            searchOpen = searchOpen,
            searchQuery = searchQuery,
            onSearchOpen = {
                searchOpen = true
            },
            onSearchQueryChange = {
                searchQuery = it
            },
            onSearchClose = {
                searchOpen = false
                searchQuery = ""
            },
            onBack = onBack
        ) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(150.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                items(filteredSeries) { item ->
                    SeriesCard(
                        series = item,
                        onClick = {
                            selectedSeries = item
                            item.series_id?.let {
                                vm.loadSeriesInfo(it)
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SeriesWithSidebar(
    title: String,
    categories: List<Category>,
    selectedCategoryId: String?,
    onCategorySelected: (String?) -> Unit,
    searchOpen: Boolean,
    searchQuery: String,
    onSearchOpen: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSearchClose: () -> Unit,
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (searchOpen) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 8.dp),
                            placeholder = {
                                Text("Cerca serie TV...")
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFCAEA00),
                                unfocusedBorderColor = Color.Gray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedPlaceholderColor = Color.Gray,
                                unfocusedPlaceholderColor = Color.Gray,
                                cursorColor = Color(0xFFCAEA00)
                            )
                        )
                    } else {
                        Text(
                            title,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    TextButton(
                        onClick = onBack
                    ) {
                        Text(
                            "← INDIETRO",
                            color = Color(0xFFCAEA00)
                        )
                    }
                },
                actions = {
                    if (searchOpen) {
                        TextButton(
                            onClick = onSearchClose
                        ) {
                            Text(
                                "CHIUDI",
                                color = Color(0xFFCAEA00),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        TextButton(
                            onClick = onSearchOpen
                        ) {
                            Text(
                                "🔍 CERCA",
                                color = Color(0xFFCAEA00),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF090909))
        ) {
            SeriesCategorySidebar(
                categories = categories,
                selectedCategoryId = selectedCategoryId,
                onCategorySelected = onCategorySelected
            )

            VerticalDivider(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp),
                color = Color(0xFF292929)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun SeriesCategorySidebar(
    categories: List<Category>,
    selectedCategoryId: String?,
    onCategorySelected: (String?) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .width(220.dp)
            .fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        contentPadding = PaddingValues(
            start = 12.dp,
            end = 12.dp,
            top = 16.dp,
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
                name = category.category_name ?: "Categoria",
                selected = selectedCategoryId == id,
                onClick = {
                    onCategorySelected(id)
                }
            )
        }
    }
}

@Composable
private fun SeriesSidebarItem(
    name: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor =
        if (selected) {
            Color(0xFFCAEA00)
        } else {
            Color(0xFF151515)
        }

    val textColor =
        if (selected) {
            Color.Black
        } else {
            Color.White
        }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable {
                onClick()
            }
            .padding(
                horizontal = 14.dp,
                vertical = 12.dp
            )
    ) {
        Text(
            text = name,
            color = textColor,
            fontSize = 14.sp,
            fontWeight =
                if (selected) {
                    FontWeight.Bold
                } else {
                    FontWeight.Normal
                },
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
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
            .height(260.dp)
            .clickable {
                onClick()
            },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF151515)
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            AsyncImage(
                model = series.cover,
                contentDescription = series.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentScale = ContentScale.Crop
            )

            Text(
                text = series.name ?: "Serie TV",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SeriesDetailScreen(
    series: SeriesStream,
    vm: AppViewModel,
    initialSeason: String?,
    onPlay: (String) -> Unit,
    onSeriesPlay: (String, SeriesStream, String) -> Unit,
    onBack: () -> Unit
) {
    val info = vm.selectedSeriesInfo

    var selectedSeason by remember {
        mutableStateOf<String?>(initialSeason)
    }

    val seasonNumbers = remember(info?.episodes) {
        info?.episodes
            ?.keys
            ?.sortedWith(
                compareBy {
                    it.toIntOrNull() ?: 0
                }
            )
            ?: emptyList()
    }

    LaunchedEffect(seasonNumbers, initialSeason) {
        if (seasonNumbers.isNotEmpty()) {
            val wanted = initialSeason

            selectedSeason =
                if (wanted != null && wanted in seasonNumbers) {
                    wanted
                } else {
                    seasonNumbers.first()
                }
        }
    }

    val selectedEpisodes =
        if (selectedSeason != null) {
            info?.episodes?.get(selectedSeason)
                ?: emptyList()
        } else {
            emptyList()
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        series.name ?: "Serie TV",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    TextButton(
                        onClick = onBack
                    ) {
                        Text(
                            "← INDIETRO",
                            color = Color(0xFFCAEA00)
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (vm.loadingSeriesInfo) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFF090909)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFF090909)),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.spacedBy(24.dp)
                    ) {
                        AsyncImage(
                            model =
                                info?.info?.cover
                                    ?: series.cover,
                            contentDescription = series.name,
                            modifier = Modifier
                                .width(220.dp)
                                .height(320.dp)
                                .clip(
                                    RoundedCornerShape(12.dp)
                                ),
                            contentScale = ContentScale.Crop
                        )

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text =
                                    info?.info?.name
                                        ?: series.name
                                        ?: "Serie TV",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Spacer(
                                modifier = Modifier.height(12.dp)
                            )

                            info?.info?.genre?.let {
                                Text(
                                    text = it,
                                    color = Color(0xFFCAEA00),
                                    fontSize = 15.sp
                                )

                                Spacer(
                                    modifier = Modifier.height(8.dp)
                                )
                            }

                            info?.info?.plot?.let {
                                Text(
                                    text = it,
                                    color = Color.LightGray,
                                    fontSize = 15.sp,
                                    lineHeight = 21.sp
                                )
                            }
                        }
                    }
                }

                if (seasonNumbers.isNotEmpty()) {
                    item {
                        Text(
                            text = "STAGIONI",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(
                            modifier = Modifier.height(10.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp),
                            horizontalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {
                            seasonNumbers.forEach { season ->
                                val selected =
                                    selectedSeason == season

                                Button(
                                    onClick = {
                                        selectedSeason = season
                                    },
                                    colors =
                                        ButtonDefaults.buttonColors(
                                            containerColor =
                                                if (selected) {
                                                    Color(0xFFCAEA00)
                                                } else {
                                                    Color(0xFF151515)
                                                },
                                            contentColor =
                                                if (selected) {
                                                    Color.Black
                                                } else {
                                                    Color.White
                                                }
                                        ),
                                    shape =
                                        RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "STAGIONE $season",
                                        fontWeight =
                                            if (selected) {
                                                FontWeight.Bold
                                            } else {
                                                FontWeight.Normal
                                            }
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text =
                            if (selectedSeason != null) {
                                "STAGIONE $selectedSeason"
                            } else {
                                "EPISODI"
                            },
                        color = Color.White,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(selectedEpisodes) { episode ->
                    EpisodeCard(
                        episode = episode,
                        onClick = {
                            val id =
                                episode.id?.toIntOrNull()

                            if (
                                id != null &&
                                selectedSeason != null
                            ) {
                                vm.streamUrl(
                                    type = "series",
                                    id = id,
                                    extension =
                                        episode.container_extension
                                )?.let { url ->
                                    onSeriesPlay(
                                        url,
                                        series,
                                        selectedSeason!!
                                    )
                                }
                            }
                        }
                    )
                }
            }
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
            .padding(bottom = 8.dp)
            .clickable {
                onClick()
            },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF151515)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = episode.info?.movie_image,
                contentDescription = episode.title,
                modifier = Modifier
                    .width(120.dp)
                    .height(70.dp)
                    .clip(
                        RoundedCornerShape(6.dp)
                    ),
                contentScale = ContentScale.Crop
            )

            Spacer(
                modifier = Modifier.width(14.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text =
                        episode.title
                            ?: episode.info?.name
                            ?: "Episodio",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                episode.info?.plot?.let {
                    Spacer(
                        modifier = Modifier.height(5.dp)
                    )

                    Text(
                        text = it,
                        color = Color.Gray,
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
