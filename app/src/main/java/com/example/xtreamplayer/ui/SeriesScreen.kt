package com.example.xtreamplayer.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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

private val SeriesAccentGreen = Color(0xFFCAEA00)
private val SeriesDarkBackground = Color(0xFF090909)
private val SeriesSidebarBackground = Color(0xFF111111)
private val SeriesCardBackground = Color(0xFF181818)

private const val SERIES_FAVORITES_CATEGORY = "__FAVORITES__"

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

    var showFavorites by remember {
        mutableStateOf(false)
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

    if (selectedSeries != null) {
        SeriesDetailScreen(
            series = selectedSeries!!,
            vm = vm,
            initialSeason = initialSeason,
            onPlay = { url, season ->
                onSeriesPlay(
                    url,
                    selectedSeries!!,
                    season
                )
            },
            onBack = {
                selectedSeries = null
                vm.clearSeriesInfo()
            }
        )

        return
    }

    val filteredSeries = remember(
        series,
        selectedCategoryId,
        showFavorites,
        searchQuery,
        vm.favoriteSeriesIds
    ) {
        when {

            searchQuery.isNotBlank() -> {
                series.filter {
                    it.name?.contains(
                        searchQuery,
                        ignoreCase = true
                    ) == true
                }
            }

            showFavorites -> {
                series.filter {
                    val id = it.series_id
                    id != null &&
                        vm.isFavoriteSeries(id)
                }
            }

            selectedCategoryId == null -> {
                series
            }

            else -> {
                series.filter {
                    it.category_id == selectedCategoryId
                }
            }
        }
    }

    SeriesWithSidebar(
        title = title,
        categories = categories,
        selectedCategoryId = selectedCategoryId,
        showFavorites = showFavorites,

        onCategorySelected = {
            showFavorites = false
            selectedCategoryId = it
        },

        onFavoritesSelected = {
            showFavorites = true
            selectedCategoryId = null
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

        if (
            filteredSeries.isEmpty() &&
            showFavorites &&
            searchQuery.isBlank()
        ) {
            SeriesEmptyFavoritesMessage()
        } else {

            LazyVerticalGrid(
                columns = GridCells.Adaptive(180.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(20.dp),
                horizontalArrangement =
                    Arrangement.spacedBy(16.dp),
                verticalArrangement =
                    Arrangement.spacedBy(18.dp)
            ) {

                items(
                    items = filteredSeries,
                    key = {
                        it.series_id
                            ?: it.num
                            ?: 0
                    }
                ) { item ->

                    SeriesCard(
                        series = item,

                        favorite =
                            item.series_id?.let {
                                vm.isFavoriteSeries(it)
                            } == true,

                        onClick = {
                            selectedSeries = item

                            item.series_id?.let {
                                vm.loadSeriesInfo(it)
                            }
                        },

                        onLongClick = {
                            item.series_id?.let {
                                vm.toggleFavoriteSeries(it)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SeriesEmptyFavoritesMessage() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Text(
                text = "⭐",
                fontSize = 42.sp
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Text(
                text = "Nessun preferito aggiunto",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                text =
                    "Tieni premuto su una serie per aggiungerla.",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun SeriesWithSidebar(
    title: String,
    categories: List<Category>,
    selectedCategoryId: String?,
    showFavorites: Boolean,
    onCategorySelected: (String?) -> Unit,
    onFavoritesSelected: () -> Unit,
    searchOpen: Boolean,
    searchQuery: String,
    onSearchOpen: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSearchClose: () -> Unit,
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SeriesDarkBackground)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(Color(0xFF101010)),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            TextButton(
                onClick = onBack
            ) {

                Text(
                    text = "‹",
                    color = SeriesAccentGreen,
                    fontSize = 34.sp
                )
            }

            if (searchOpen) {

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange =
                        onSearchQueryChange,

                    modifier = Modifier
                        .weight(1f)
                        .padding(
                            vertical = 8.dp,
                            horizontal = 12.dp
                        ),

                    singleLine = true,

                    placeholder = {
                        Text(
                            text = "Cerca...",
                            color = Color.Gray
                        )
                    },

                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor =
                                SeriesAccentGreen,
                            unfocusedBorderColor =
                                Color.DarkGray,
                            focusedTextColor =
                                Color.White,
                            unfocusedTextColor =
                                Color.White,
                            cursorColor =
                                SeriesAccentGreen
                        )
                )

                TextButton(
                    onClick = onSearchClose
                ) {

                    Text(
                        text = "✕",
                        color = Color.White,
                        fontSize = 20.sp
                    )
                }

            } else {

                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                TextButton(
                    onClick = onSearchOpen
                ) {

                    Text(
                        text = "⌕",
                        color = Color.White,
                        fontSize = 28.sp
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxSize()
        ) {

            SeriesCategorySidebar(
                categories = categories,
                selectedCategoryId =
                    selectedCategoryId,
                showFavorites = showFavorites,

                onCategorySelected =
                    onCategorySelected,

                onFavoritesSelected =
                    onFavoritesSelected
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
    showFavorites: Boolean,
    onCategorySelected: (String?) -> Unit,
    onFavoritesSelected: () -> Unit
) {

    androidx.compose.foundation.lazy.LazyColumn(
        modifier = Modifier
            .width(230.dp)
            .fillMaxHeight()
            .background(
                SeriesSidebarBackground
            ),

        contentPadding = PaddingValues(
            vertical = 14.dp,
            horizontal = 10.dp
        )
    ) {

        item {

            SeriesSidebarItem(
                text = "TUTTI",

                selected =
                    !showFavorites &&
                        selectedCategoryId == null,

                onClick = {
                    onCategorySelected(null)
                }
            )
        }

        item {

            SeriesSidebarItem(
                text = "⭐ PREFERITI",

                selected = showFavorites,

                onClick =
                    onFavoritesSelected
            )
        }

        items(
            items = categories,
            key = {
                it.category_id
                    ?: it.category_name.orEmpty()
            }
        ) { category ->

            SeriesSidebarItem(
                text =
                    category.category_name
                        ?: "Categoria",

                selected =
                    !showFavorites &&
                        selectedCategoryId ==
                            category.category_id,

                onClick = {
                    onCategorySelected(
                        category.category_id
                    )
                }
            )
        }
    }
}

@Composable
private fun SeriesSidebarItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {

    val background =
        if (selected) {
            SeriesAccentGreen
        } else {
            Color.Transparent
        }

    val textColor =
        if (selected) {
            Color.Black
        } else {
            Color.White
        }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clip(
                RoundedCornerShape(10.dp)
            ),

        color = background,
        onClick = onClick
    ) {

        Text(
            text = text,
            color = textColor,
            fontSize = 14.sp,

            fontWeight =
                if (selected) {
                    FontWeight.Bold
                } else {
                    FontWeight.Normal
                },

            modifier = Modifier.padding(
                horizontal = 14.dp,
                vertical = 12.dp
            ),

            maxLines = 1,
            overflow =
                TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SeriesCard(
    series: SeriesStream,
    favorite: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(270.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),

        colors = CardDefaults.cardColors(
            containerColor =
                SeriesCardBackground
        ),

        shape =
            RoundedCornerShape(12.dp)
    ) {

        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            AsyncImage(
                model = series.cover,
                contentDescription =
                    series.name,

                modifier =
                    Modifier.fillMaxSize(),

                contentScale =
                    ContentScale.Crop
            )

            if (favorite) {

                Text(
                    text = "★",
                    color = SeriesAccentGreen,
                    fontSize = 22.sp,

                    modifier = Modifier
                        .align(
                            Alignment.TopEnd
                        )
                        .padding(8.dp)
                )
            }

            Text(
                text =
                    series.name
                        ?: "Serie TV",

                color = Color.White,
                fontSize = 14.sp,
                fontWeight =
                    FontWeight.SemiBold,

                maxLines = 2,

                overflow =
                    TextOverflow.Ellipsis,

                modifier = Modifier
                    .align(
                        Alignment.BottomStart
                    )
                    .fillMaxWidth()
                    .background(
                        Color.Black.copy(
                            alpha = 0.78f
                        )
                    )
                    .padding(
                        horizontal = 10.dp,
                        vertical = 8.dp
                    )
            )
        }
    }
}

@Composable
private fun SeriesDetailScreen(
    series: SeriesStream,
    vm: AppViewModel,
    initialSeason: String?,
    onPlay: (String, String) -> Unit,
    onBack: () -> Unit
) {

    val info = vm.selectedSeriesInfo

    var selectedSeason by remember(
        info,
        initialSeason
    ) {
        mutableStateOf(
            initialSeason
                ?: info?.episodes?.keys
                    ?.firstOrNull()
        )
    }

    val seasonNumbers =
        info?.episodes?.keys
            ?.sortedWith(
                compareBy {
                    it.toIntOrNull()
                        ?: Int.MAX_VALUE
                }
            )
            ?: emptyList()

    LaunchedEffect(
        info,
        initialSeason
    ) {
        if (
            initialSeason != null &&
            initialSeason in seasonNumbers
        ) {
            selectedSeason =
                initialSeason
        } else if (
            selectedSeason == null &&
            seasonNumbers.isNotEmpty()
        ) {
            selectedSeason =
                seasonNumbers.first()
        }
    }

    val episodes =
        selectedSeason
            ?.let {
                info?.episodes?.get(it)
            }
            ?: emptyList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                SeriesDarkBackground
            )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(
                    Color(0xFF101010)
                ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            TextButton(
                onClick = onBack
            ) {

                Text(
                    text = "‹",
                    color = SeriesAccentGreen,
                    fontSize = 34.sp
                )
            }

            Text(
                text =
                    series.name
                        ?: "Serie TV",

                color = Color.White,
                fontSize = 21.sp,
                fontWeight =
                    FontWeight.Bold,

                maxLines = 1,

                overflow =
                    TextOverflow.Ellipsis
            )
        }

        if (vm.loadingSeriesInfo) {

            Box(
                modifier =
                    Modifier.fillMaxSize(),
                contentAlignment =
                    Alignment.Center
            ) {

                CircularProgressIndicator(
                    color =
                        SeriesAccentGreen
                )
            }

        } else {

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(28.dp),
                horizontalArrangement =
                    Arrangement.spacedBy(28.dp)
            ) {

                AsyncImage(
                    model = series.cover,
                    contentDescription =
                        series.name,

                    modifier = Modifier
                        .width(260.dp)
                        .fillMaxHeight()
                        .clip(
                            RoundedCornerShape(14.dp)
                        ),

                    contentScale =
                        ContentScale.Crop
                )

                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                ) {

                    Text(
                        text =
                            info?.info?.name
                                ?: series.name
                                ?: "Serie TV",

                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight =
                            FontWeight.Bold
                    )

                    Spacer(
                        modifier =
                            Modifier.height(12.dp)
                    )

                    info?.info?.plot
                        ?.takeIf {
                            it.isNotBlank()
                        }
                        ?.let {

                            Text(
                                text = it,
                                color = Color.LightGray,
                                fontSize = 14.sp,
                                lineHeight =
                                    20.sp,

                                maxLines = 6,

                                overflow =
                                    TextOverflow.Ellipsis
                            )
                        }

                    Spacer(
                        modifier =
                            Modifier.height(22.dp)
                    )

                    if (seasonNumbers.isNotEmpty()) {

                        Text(
                            text = "STAGIONE",
                            color =
                                Color.Gray,
                            fontSize = 12.sp,
                            fontWeight =
                                FontWeight.Bold
                        )

                        Spacer(
                            modifier =
                                Modifier.height(8.dp)
                        )

                        Row(
                            horizontalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {

                            seasonNumbers.forEach {
                                season ->

                                FilterChip(
                                    selected =
                                        selectedSeason ==
                                            season,

                                    onClick = {
                                        selectedSeason =
                                            season
                                    },

                                    label = {
                                        Text(
                                            season
                                        )
                                    },

                                    colors =
                                        FilterChipDefaults
                                            .filterChipColors(
                                                selectedContainerColor =
                                                    SeriesAccentGreen,
                                                selectedLabelColor =
                                                    Color.Black
                                            )
                                )
                            }
                        }

                        Spacer(
                            modifier =
                                Modifier.height(18.dp)
                        )
                    }

                    if (episodes.isEmpty()) {

                        Text(
                            text =
                                "Nessun episodio disponibile.",
                            color =
                                Color.Gray
                        )

                    } else {

                        androidx.compose.foundation.lazy.LazyColumn(
                            verticalArrangement =
                                Arrangement.spacedBy(
                                    10.dp
                                )
                        ) {

                            items(
                                items = episodes,
                                key = {
                                    it.id
                                        ?: it.episode_num
                                            ?: 0
                                }
                            ) { episode ->

                                EpisodeCard(
                                    episode = episode,
                                    onClick = {

                                        val id =
                                            episode.id
                                                ?.toIntOrNull()
                                                ?: return@EpisodeCard

                                        val url =
                                            vm.streamUrl(
                                                type = "series",
                                                id = id,
                                                extension =
                                                    episode.container_extension
                                            )
                                                ?: return@EpisodeCard

                                        onPlay(
                                            url,
                                            selectedSeason
                                                ?: ""
                                        )
                                    }
                                )
                            }
                        }
                    }
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
            .height(82.dp),

        colors = CardDefaults.cardColors(
            containerColor =
                SeriesCardBackground
        ),

        onClick = onClick,

        shape =
            RoundedCornerShape(10.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            AsyncImage(
                model =
                    episode.info?.movie_image,

                contentDescription =
                    episode.title,

                modifier = Modifier
                    .width(105.dp)
                    .fillMaxHeight()
                    .clip(
                        RoundedCornerShape(7.dp)
                    ),

                contentScale =
                    ContentScale.Crop
            )

            Spacer(
                modifier =
                    Modifier.width(12.dp)
            )

            Column(
                modifier =
                    Modifier.weight(1f)
            ) {

                Text(
                    text =
                        "Episodio ${
                            episode.episode_num
                                ?: ""
                        }",

                    color =
                        SeriesAccentGreen,

                    fontSize = 12.sp,
                    fontWeight =
                        FontWeight.Bold
                )

                Text(
                    text =
                        episode.title
                            ?: episode.info?.name
                            ?: "Episodio",

                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight =
                        FontWeight.SemiBold,

                    maxLines = 2,

                    overflow =
                        TextOverflow.Ellipsis
                )
            }

            Text(
                text = "▶",
                color =
                    SeriesAccentGreen,
                fontSize = 20.sp
            )
        }
    }
}
