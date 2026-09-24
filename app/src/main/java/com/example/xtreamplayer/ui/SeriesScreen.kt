package com.example.xtreamplayer.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
import kotlinx.coroutines.launch

/*
 * Palette legacy del dettaglio Serie attuale.
 * In questo step il dettaglio resta intenzionalmente invariato.
 */
private val SeriesAccentGreen = Color(0xFFCAEA00)
private val SeriesDarkBackground = Color(0xFF090909)
private val SeriesSidebarBackground = Color(0xFF111111)
private val SeriesCardBackground = Color(0xFF181818)

/*
 * Palette nuovo catalogo SERIE TV.
 */
private val SeriesMovieBackground = Color(0xFF050A12)
private val SeriesMovieTopBar = Color(0xFF080E18)
private val SeriesMovieSidebar = Color(0xFF080D16)
private val SeriesMovieCard = Color(0xFF0C1420)
private val SeriesBlue = Color(0xFF1677FF)
private val SeriesTextSecondary = Color(0xFF8E9BAD)
private val SeriesVpnGreen = Color(0xFF43E07B)

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
    /*
     * Come FILM: niente "TUTTI".
     * All'ingresso selezioniamo la prima categoria reale disponibile.
     */
    var selectedCategoryId by remember(categories) {
        mutableStateOf(categories.firstOrNull()?.category_id)
    }

    var showFavorites by remember {
        mutableStateOf(false)
    }

    var selectedSeries by remember {
        mutableStateOf<SeriesStream?>(initialSeries)
    }

    /*
     * Ricerca globale sempre visibile.
     * Non cambia la categoria selezionata: cancellando il testo
     * si torna esattamente alla categoria/preferiti precedenti.
     */
    var globalSearchQuery by remember {
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

    /*
     * Il dettaglio esistente resta invariato in questo primo step.
     * loadSeriesInfo viene eseguito solo quando apriamo una serie.
     */
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
        globalSearchQuery,
        vm.favoriteSeriesIds
    ) {
        when {
            globalSearchQuery.isNotBlank() -> {
                series.filter {
                    it.name?.contains(
                        globalSearchQuery,
                        ignoreCase = true
                    ) == true
                }
            }

            showFavorites -> {
                series.filter {
                    val id = it.series_id
                    id != null && vm.isFavoriteSeries(id)
                }
            }

            selectedCategoryId != null -> {
                series.filter {
                    it.category_id == selectedCategoryId
                }
            }

            else -> emptyList()
        }
    }

    val selectedCategoryName = remember(
        categories,
        selectedCategoryId,
        showFavorites,
        globalSearchQuery
    ) {
        when {
            globalSearchQuery.isNotBlank() -> "RISULTATI"
            showFavorites -> "PREFERITI"
            else -> categories
                .firstOrNull {
                    it.category_id == selectedCategoryId
                }
                ?.category_name
                ?.uppercase()
                ?: "SERIE TV"
        }
    }

    /*
     * Navigazione Fire TV robusta come FILM V2:
     * la LazyVerticalGrid viene scrollata esplicitamente quando il D-pad
     * deve raggiungere una riga non ancora composta.
     */
    val seriesGridState = rememberLazyGridState()
    val seriesFocusRequesters = remember {
        mutableStateMapOf<Int, FocusRequester>()
    }
    val seriesGridScope = rememberCoroutineScope()

    SeriesCatalogLayout(
        categories = categories,
        selectedCategoryId = selectedCategoryId,
        showFavorites = showFavorites,
        globalSearchQuery = globalSearchQuery,
        selectedCategoryName = selectedCategoryName,
        seriesCount = filteredSeries.size,
        onSearchQueryChange = {
            globalSearchQuery = it
        },
        onClearSearch = {
            globalSearchQuery = ""
        },
        onCategorySelected = { categoryId ->
            globalSearchQuery = ""
            showFavorites = false
            selectedCategoryId = categoryId
        },
        onFavoritesSelected = {
            globalSearchQuery = ""
            showFavorites = true
        },
        onBack = onBack
    ) {
        when {
            filteredSeries.isEmpty() &&
                globalSearchQuery.isNotBlank() -> {
                SeriesCatalogEmptyState(
                    icon = "⌕",
                    title = "Nessuna serie trovata",
                    subtitle = "Prova con un altro titolo."
                )
            }

            filteredSeries.isEmpty() && showFavorites -> {
                SeriesCatalogEmptyState(
                    icon = "★",
                    title = "Nessun preferito",
                    subtitle = "Tieni premuto su una serie per aggiungerla."
                )
            }

            filteredSeries.isEmpty() -> {
                SeriesCatalogEmptyState(
                    icon = "▣",
                    title = "Nessuna serie disponibile",
                    subtitle = "Questa categoria non contiene serie."
                )
            }

            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(
                        minSize = 95.dp
                    ),
                    state = seriesGridState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 22.dp,
                        end = 22.dp,
                        top = 6.dp,
                        bottom = 24.dp
                    ),
                    horizontalArrangement =
                        Arrangement.spacedBy(10.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        count = filteredSeries.size,
                        key = { index ->
                            val item = filteredSeries[index]
                            item.series_id
                                ?: item.num
                                ?: item.name
                                ?: index
                        }
                    ) { index ->
                        val item = filteredSeries[index]

                        val posterFocusRequester =
                            seriesFocusRequesters.getOrPut(index) {
                                FocusRequester()
                            }

                        SeriesPosterCard(
                            series = item,
                            favorite =
                                item.series_id?.let {
                                    vm.isFavoriteSeries(it)
                                } == true,
                            focusRequester = posterFocusRequester,
                            onVerticalMove = { direction ->
                                val columns =
                                    seriesGridState.layoutInfo.visibleItemsInfo
                                        .maxOfOrNull { it.column + 1 }
                                        ?.coerceAtLeast(1)
                                        ?: 1

                                val targetIndex =
                                    (index + (direction * columns))
                                        .coerceIn(
                                            0,
                                            filteredSeries.lastIndex
                                        )

                                if (targetIndex != index) {
                                    seriesGridScope.launch {
                                        seriesGridState.scrollToItem(targetIndex)
                                        withFrameNanos { }
                                        seriesFocusRequesters[targetIndex]
                                            ?.requestFocus()
                                    }
                                    true
                                } else {
                                    false
                                }
                            },
                            onClick = {
                                /*
                                 * Nessun caricamento durante focus/scroll.
                                 * La richiesta parte soltanto al click.
                                 */
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
}

@Composable
private fun SeriesCatalogLayout(
    categories: List<Category>,
    selectedCategoryId: String?,
    showFavorites: Boolean,
    globalSearchQuery: String,
    selectedCategoryName: String,
    seriesCount: Int,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onCategorySelected: (String?) -> Unit,
    onFavoritesSelected: () -> Unit,
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    /*
     * Ricerca TV:
     * il focus D-pad illumina soltanto il campo.
     * La tastiera si apre SOLO dopo OK/Enter/DirectionCenter.
     */
    var searchEditing by remember {
        mutableStateOf(false)
    }
    var searchFocused by remember {
        mutableStateOf(false)
    }
    val keyboardController =
        LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SeriesMovieBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp)
                .background(SeriesMovieTopBar)
                .padding(
                    start = 14.dp,
                    end = 24.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                onClick = onBack,
                modifier = Modifier.size(46.dp),
                shape = RoundedCornerShape(23.dp),
                color = Color(0xFF111A27)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "‹",
                        color = Color.White,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Light
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Text(
                text = "XTREAM PLAYER",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(22.dp)
                    .background(
                        Color.White.copy(alpha = 0.22f)
                    )
            )

            Spacer(Modifier.width(10.dp))

            Text(
                text = "SERIE TV",
                color = SeriesBlue,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.width(34.dp))

            OutlinedTextField(
                value = globalSearchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .onFocusChanged {
                        searchFocused = it.isFocused

                        if (!it.isFocused) {
                            searchEditing = false
                            keyboardController?.hide()
                        }
                    }
                    .onKeyEvent { event ->
                        if (
                            event.type == KeyEventType.KeyDown &&
                            !searchEditing &&
                            (
                                event.key == Key.Enter ||
                                    event.key == Key.DirectionCenter
                            )
                        ) {
                            searchEditing = true
                            keyboardController?.show()
                            true
                        } else {
                            false
                        }
                    },
                readOnly = !searchEditing,
                singleLine = true,
                leadingIcon = {
                    Text(
                        text = "⌕",
                        color = SeriesTextSecondary,
                        fontSize = 24.sp
                    )
                },
                trailingIcon = {
                    if (globalSearchQuery.isNotBlank()) {
                        TextButton(
                            onClick = onClearSearch
                        ) {
                            Text(
                                text = "✕",
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }
                    }
                },
                placeholder = {
                    Text(
                        text = "Cerca in tutte le serie…",
                        color = SeriesTextSecondary,
                        fontSize = 14.sp
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SeriesBlue,
                    unfocusedBorderColor = Color(0xFF26364B),
                    focusedContainerColor = Color(0xFF0B1320),
                    unfocusedContainerColor = Color(0xFF0B1320),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = SeriesBlue
                ),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(Modifier.width(28.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            SeriesVpnGreen,
                            RoundedCornerShape(4.dp)
                        )
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = "VPN ATTIVA",
                    color = SeriesVpnGreen,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            SeriesCategorySidebar(
                categories = categories,
                selectedCategoryId = selectedCategoryId,
                showFavorites = showFavorites,
                onCategorySelected = onCategorySelected,
                onFavoritesSelected = onFavoritesSelected
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .padding(
                            start = 22.dp,
                            end = 22.dp
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedCategoryName,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(Modifier.width(12.dp))

                    Text(
                        text = "$seriesCount serie",
                        color = SeriesTextSecondary,
                        fontSize = 14.sp
                    )
                }

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
}

@Composable
private fun SeriesCategorySidebar(
    categories: List<Category>,
    selectedCategoryId: String?,
    showFavorites: Boolean,
    onCategorySelected: (String?) -> Unit,
    onFavoritesSelected: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .width(220.dp)
            .fillMaxHeight()
            .background(SeriesMovieSidebar),
        contentPadding = PaddingValues(
            start = 12.dp,
            end = 12.dp,
            top = 18.dp,
            bottom = 18.dp
        ),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        item {
            SeriesSidebarItem(
                text = "★  PREFERITI",
                selected = showFavorites,
                onClick = onFavoritesSelected
            )
        }

        items(
            items = categories,
            key = {
                it.category_id
                    ?: it.category_name
                    ?: ""
            }
        ) { category ->
            val categoryId = category.category_id

            SeriesSidebarItem(
                text = category.category_name ?: "Categoria",
                selected =
                    !showFavorites &&
                        selectedCategoryId == categoryId,
                onClick = {
                    onCategorySelected(categoryId)
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
    var focused by remember {
        mutableStateOf(false)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged {
                focused = it.isFocused
            }
            .border(
                width = if (focused) 3.dp else 0.dp,
                color = if (focused) SeriesBlue else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            ),
        color =
            if (selected) {
                SeriesBlue
            } else if (focused) {
                SeriesBlue.copy(alpha = 0.30f)
            } else {
                Color.Transparent
            },
        shape = RoundedCornerShape(10.dp),
        onClick = onClick
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight =
                if (selected || focused) {
                    FontWeight.Bold
                } else {
                    FontWeight.Medium
                },
            modifier = Modifier.padding(
                horizontal = 14.dp,
                vertical = 11.dp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SeriesPosterCard(
    series: SeriesStream,
    favorite: Boolean,
    focusRequester: FocusRequester,
    onVerticalMove: (Int) -> Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    var focused by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .scale(if (focused) 1.035f else 1f)
            .onFocusChanged {
                focused = it.isFocused
            }
            .onPreviewKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    when (event.key) {
                        Key.DirectionDown -> onVerticalMove(1)
                        Key.DirectionUp -> onVerticalMove(-1)
                        else -> false
                    }
                } else {
                    false
                }
            }
            .border(
                width = if (focused) 3.dp else 0.dp,
                color = if (focused) SeriesBlue else Color.Transparent,
                shape = RoundedCornerShape(14.dp)
            )
            .padding(if (focused) 3.dp else 0.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .focusable()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.67f)
                .border(
                    width = 1.dp,
                    color =
                        if (favorite) {
                            SeriesBlue
                        } else {
                            Color.White.copy(alpha = 0.08f)
                        },
                    shape = RoundedCornerShape(12.dp)
                ),
            colors = CardDefaults.cardColors(
                containerColor = SeriesMovieCard
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                AsyncImage(
                    model = series.cover,
                    contentDescription = series.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                if (favorite) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp),
                        color = Color.Black.copy(alpha = 0.72f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "★",
                            color = SeriesBlue,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(
                                horizontal = 6.dp,
                                vertical = 3.dp
                            )
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        Text(
            text = series.name ?: "Serie TV",
            color = Color.White,
            fontSize = 11.sp,
            lineHeight = 13.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SeriesCatalogEmptyState(
    icon: String,
    title: String,
    subtitle: String
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = icon,
                color = SeriesBlue,
                fontSize = 38.sp
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = title,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = subtitle,
                color = SeriesTextSecondary,
                fontSize = 13.sp
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
    /*
     * BACK fisico del telecomando:
     * dal dettaglio torna al catalogo SERIE TV.
     * Usa lo stesso onBack del pulsante grafico.
     */
    BackHandler {
        onBack()
    }

    val info = vm.selectedSeriesInfo
    val seriesInfo = info?.info
    val seriesId = series.series_id

    val seasonNumbers =
        info?.episodes?.keys
            ?.sortedWith(
                compareBy {
                    it.toIntOrNull() ?: Int.MAX_VALUE
                }
            )
            ?: emptyList()

    var selectedSeason by remember(
        info,
        initialSeason
    ) {
        mutableStateOf(
            initialSeason
                ?.takeIf { it in seasonNumbers }
                ?: seasonNumbers.firstOrNull()
        )
    }

    LaunchedEffect(
        info,
        initialSeason
    ) {
        selectedSeason =
            initialSeason
                ?.takeIf { it in seasonNumbers }
                ?: selectedSeason
                    ?.takeIf { it in seasonNumbers }
                ?: seasonNumbers.firstOrNull()
    }

    val episodes =
        selectedSeason
            ?.let { info?.episodes?.get(it) }
            ?: emptyList()

    val selectedSeasonInfo =
        info?.seasons
            ?.firstOrNull {
                it.season_number?.toString() ==
                    selectedSeason
            }

    val title =
        seriesInfo?.name
            ?.takeIf { it.isNotBlank() }
            ?: series.name
            ?: "Serie TV"

    val cover =
        selectedSeasonInfo?.cover
            ?.takeIf { it.isNotBlank() }
            ?: seriesInfo?.cover
                ?.takeIf { it.isNotBlank() }
            ?: series.cover

    val plot =
        seriesInfo?.plot
            ?.takeIf { it.isNotBlank() }

    val genre =
        seriesInfo?.genre
            ?.takeIf { it.isNotBlank() }

    val rating =
        seriesInfo?.rating
            ?.takeIf { it.isNotBlank() }
            ?: series.rating
                ?.takeIf { it.isNotBlank() }

    val favorite =
        seriesId?.let {
            vm.isFavoriteSeries(it)
        } == true

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SeriesMovieBackground)
    ) {
        /*
         * Hero: i modelli attuali non espongono un backdrop
         * dedicato, quindi usiamo la cover reale della serie
         * come sfondo cinematografico.
         */
        AsyncImage(
            model = cover,
            contentDescription = title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.22f
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xD9050A12))
        )

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            /*
             * TOP BAR
             */
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(76.dp)
                    .background(
                        SeriesMovieTopBar.copy(
                            alpha = 0.90f
                        )
                    )
                    .padding(
                        start = 14.dp,
                        end = 24.dp
                    ),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                var detailBackFocused by remember {
                    mutableStateOf(false)
                }

                Surface(
                    onClick = onBack,
                    modifier = Modifier
                        .size(46.dp)
                        .scale(if (detailBackFocused) 1.06f else 1f)
                        .onFocusChanged {
                            detailBackFocused = it.isFocused
                        },
                    shape = RoundedCornerShape(23.dp),
                    color =
                        if (detailBackFocused) {
                            SeriesBlue.copy(alpha = 0.32f)
                        } else {
                            Color(0xFF111A27)
                        },
                    border = androidx.compose.foundation.BorderStroke(
                        if (detailBackFocused) 3.dp else 1.dp,
                        if (detailBackFocused) SeriesBlue else Color.Transparent
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment =
                            Alignment.Center
                    ) {
                        Text(
                            text = "‹",
                            color = Color.White,
                            fontSize = 34.sp,
                            fontWeight =
                                FontWeight.Light
                        )
                    }
                }

                Spacer(Modifier.width(16.dp))

                Text(
                    text = "XTREAM PLAYER",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.width(10.dp))

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(22.dp)
                        .background(
                            Color.White.copy(
                                alpha = 0.22f
                            )
                        )
                )

                Spacer(Modifier.width(10.dp))

                Text(
                    text = "SERIE TV",
                    color = SeriesBlue,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.weight(1f))

                Row(
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                SeriesVpnGreen,
                                RoundedCornerShape(4.dp)
                            )
                    )

                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = "VPN ATTIVA",
                        color = SeriesVpnGreen,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (vm.loadingSeriesInfo && info == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment =
                        Alignment.Center
                ) {
                    Column(
                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = SeriesBlue
                        )

                        Spacer(Modifier.height(14.dp))

                        Text(
                            text =
                                "Caricamento serie…",
                            color =
                                SeriesTextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            start = 34.dp,
                            end = 34.dp,
                            top = 24.dp,
                            bottom = 24.dp
                        ),
                    horizontalArrangement =
                        Arrangement.spacedBy(30.dp)
                ) {
                    /*
                     * HERO / INFO
                     * Ordine definitivo:
                     * locandina -> preferiti -> titolo/metadata -> trama.
                     */
                    Column(
                        modifier = Modifier
                            .width(330.dp)
                            .fillMaxHeight()
                    ) {
                        Card(
                            modifier = Modifier
                                .width(220.dp)
                                .height(300.dp)
                                .border(
                                    width = 1.dp,
                                    color = Color.White.copy(alpha = 0.14f),
                                    shape = RoundedCornerShape(14.dp)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = SeriesMovieCard
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            AsyncImage(
                                model = cover,
                                contentDescription = title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(Modifier.height(14.dp))

                        if (seriesId != null) {
                            var favoriteButtonFocused by remember {
                                mutableStateOf(false)
                            }

                            Surface(
                                onClick = {
                                    vm.toggleFavoriteSeries(seriesId)
                                },
                                modifier = Modifier
                                    .width(220.dp)
                                    .scale(if (favoriteButtonFocused) 1.025f else 1f)
                                    .onFocusChanged {
                                        favoriteButtonFocused = it.isFocused
                                    },
                                color =
                                    if (favoriteButtonFocused) {
                                        SeriesBlue.copy(alpha = 0.22f)
                                    } else {
                                        Color.Black.copy(alpha = 0.32f)
                                    },
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    if (favoriteButtonFocused) 3.dp else 1.dp,
                                    when {
                                        favoriteButtonFocused -> SeriesBlue
                                        favorite -> SeriesBlue
                                        else -> Color.White.copy(alpha = 0.22f)
                                    }
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(
                                        horizontal = 16.dp,
                                        vertical = 11.dp
                                    ),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (favorite) "★" else "☆",
                                        color = if (favorite) {
                                            SeriesBlue
                                        } else {
                                            Color.White
                                        },
                                        fontSize = 20.sp
                                    )

                                    Spacer(Modifier.width(8.dp))

                                    Text(
                                        text = if (favorite) {
                                            "Nei preferiti"
                                        } else {
                                            "Aggiungi ai preferiti"
                                        },
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(18.dp))

                        Text(
                            text = title,
                            color = Color.White,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (rating != null || genre != null) {
                            Spacer(Modifier.height(8.dp))

                            Text(
                                text = listOfNotNull(
                                    rating?.let { "★ $it" },
                                    genre
                                ).joinToString("   •   "),
                                color = SeriesTextSecondary,
                                fontSize = 13.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (plot != null) {
                            Spacer(Modifier.height(12.dp))

                            Text(
                                text = plot,
                                color = Color.White.copy(alpha = 0.82f),
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                maxLines = 5,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    /*
                     * STAGIONI + EPISODI
                     */
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        Row(
                            modifier =
                                Modifier.fillMaxWidth(),
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {
                            Text(
                                text = "STAGIONI",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(Modifier.width(12.dp))

                            Text(
                                text =
                                    "${seasonNumbers.size} stagioni",
                                color =
                                    SeriesTextSecondary,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        if (seasonNumbers.isEmpty()) {
                            Text(
                                text =
                                    "Nessuna stagione disponibile.",
                                color =
                                    SeriesTextSecondary,
                                fontSize = 14.sp
                            )
                        } else {
                            LazyVerticalGrid(
                                columns =
                                    GridCells.Adaptive(
                                        minSize = 92.dp
                                    ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(
                                        max = 100.dp
                                    ),
                                horizontalArrangement =
                                    Arrangement.spacedBy(8.dp),
                                verticalArrangement =
                                    Arrangement.spacedBy(8.dp)
                            ) {
                                items(
                                    items = seasonNumbers,
                                    key = { it }
                                ) { season ->
                                    val selected =
                                        selectedSeason ==
                                            season

                                    var seasonFocused by remember(season) {
                                        mutableStateOf(false)
                                    }

                                    Surface(
                                        onClick = {
                                            selectedSeason =
                                                season
                                        },
                                        modifier = Modifier
                                            .scale(if (seasonFocused) 1.035f else 1f)
                                            .onFocusChanged {
                                                seasonFocused = it.isFocused
                                            },
                                        color =
                                            when {
                                                selected -> SeriesBlue
                                                seasonFocused ->
                                                    SeriesBlue.copy(alpha = 0.28f)
                                                else -> Color(0xFF111A27)
                                            },
                                        border = androidx.compose.foundation.BorderStroke(
                                            if (seasonFocused) 3.dp else 1.dp,
                                            if (seasonFocused) {
                                                Color(0xFF55C7FF)
                                            } else {
                                                Color.Transparent
                                            }
                                        ),
                                        shape =
                                            RoundedCornerShape(
                                                9.dp
                                            )
                                    ) {
                                        Box(
                                            modifier =
                                                Modifier.padding(
                                                    horizontal =
                                                        12.dp,
                                                    vertical =
                                                        10.dp
                                                ),
                                            contentAlignment =
                                                Alignment.Center
                                        ) {
                                            Text(
                                                text =
                                                    "Stagione $season",
                                                color =
                                                    Color.White,
                                                fontSize =
                                                    12.sp,
                                                fontWeight =
                                                    if (selected) {
                                                        FontWeight.Bold
                                                    } else {
                                                        FontWeight.Medium
                                                    },
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        selectedSeasonInfo
                            ?.overview
                            ?.takeIf {
                                it.isNotBlank()
                            }
                            ?.let { overview ->
                                Spacer(
                                    Modifier.height(
                                        10.dp
                                    )
                                )

                                Text(
                                    text = overview,
                                    color =
                                        SeriesTextSecondary,
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp,
                                    maxLines = 2,
                                    overflow =
                                        TextOverflow.Ellipsis
                                )
                            }

                        Spacer(Modifier.height(16.dp))

                        Row(
                            modifier =
                                Modifier.fillMaxWidth(),
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {
                            Text(
                                text =
                                    selectedSeason?.let {
                                        "EPISODI · STAGIONE $it"
                                    } ?: "EPISODI",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight =
                                    FontWeight.Bold
                            )

                            Spacer(Modifier.width(12.dp))

                            Text(
                                text =
                                    "${episodes.size} episodi",
                                color =
                                    SeriesTextSecondary,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(Modifier.height(10.dp))

                        if (episodes.isEmpty()) {
                            Box(
                                modifier =
                                    Modifier.fillMaxSize(),
                                contentAlignment =
                                    Alignment.Center
                            ) {
                                Text(
                                    text =
                                        "Nessun episodio disponibile.",
                                    color =
                                        SeriesTextSecondary,
                                    fontSize = 14.sp
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier =
                                    Modifier.fillMaxSize(),
                                verticalArrangement =
                                    Arrangement.spacedBy(
                                        10.dp
                                    ),
                                contentPadding =
                                    PaddingValues(
                                        bottom = 12.dp
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
                                    CinematicEpisodeCard(
                                        episode = episode,
                                        onClick = {
                                            val id =
                                                episode.id
                                                    ?.toIntOrNull()
                                                    ?: return@CinematicEpisodeCard

                                            /*
                                             * PLAYBACK SACRO:
                                             * stesso builder e stessa
                                             * estensione del flusso
                                             * funzionante precedente.
                                             */
                                            val url =
                                                vm.streamUrl(
                                                    type = "series",
                                                    id = id,
                                                    extension =
                                                        episode.container_extension
                                                )
                                                    ?: return@CinematicEpisodeCard

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
}

@Composable
private fun CinematicEpisodeCard(
    episode: SeriesEpisode,
    onClick: () -> Unit
) {
    val episodeInfo = episode.info

    val episodeTitle =
        episode.title
            ?.takeIf { it.isNotBlank() }
            ?: episodeInfo?.name
                ?.takeIf { it.isNotBlank() }
            ?: "Episodio"

    val metadata =
        listOfNotNull(
            episodeInfo?.duration
                ?.takeIf { it.isNotBlank() },
            episodeInfo?.rating
                ?.takeIf { it.isNotBlank() }
                ?.let { "★ $it" }
        )

    var focused by remember {
        mutableStateOf(false)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(112.dp)
            .scale(if (focused) 1.012f else 1f)
            .onFocusChanged {
                focused = it.isFocused
            }
            .border(
                width = if (focused) 3.dp else 1.dp,
                color =
                    if (focused) {
                        Color(0xFF55C7FF)
                    } else {
                        Color.White.copy(alpha = 0.06f)
                    },
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor =
                if (focused) {
                    SeriesBlue.copy(alpha = 0.20f)
                } else {
                    Color(0xCC0C1420)
                }
        ),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(150.dp)
                    .fillMaxHeight()
                    .clip(
                        RoundedCornerShape(8.dp)
                    )
                    .background(
                        Color(0xFF111A27)
                    ),
                contentAlignment =
                    Alignment.Center
            ) {
                AsyncImage(
                    model =
                        episodeInfo?.movie_image,
                    contentDescription =
                        episodeTitle,
                    modifier =
                        Modifier.fillMaxSize(),
                    contentScale =
                        ContentScale.Crop
                )

                Surface(
                    modifier = Modifier
                        .align(
                            Alignment.BottomStart
                        )
                        .padding(7.dp),
                    color =
                        Color.Black.copy(
                            alpha = 0.72f
                        ),
                    shape =
                        RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text =
                            "E${episode.episode_num ?: ""}",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight =
                            FontWeight.Bold,
                        modifier =
                            Modifier.padding(
                                horizontal = 7.dp,
                                vertical = 4.dp
                            )
                    )
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(
                modifier =
                    Modifier.weight(1f)
            ) {
                Text(
                    text = episodeTitle,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight =
                        FontWeight.SemiBold,
                    maxLines = 1,
                    overflow =
                        TextOverflow.Ellipsis
                )

                if (metadata.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))

                    Text(
                        text =
                            metadata.joinToString(
                                "   •   "
                            ),
                        color =
                            SeriesTextSecondary,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }

                episodeInfo?.plot
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?.let { episodePlot ->
                        Spacer(Modifier.height(5.dp))

                        Text(
                            text = episodePlot,
                            color =
                                Color.White.copy(
                                    alpha = 0.68f
                                ),
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            maxLines = 2,
                            overflow =
                                TextOverflow.Ellipsis
                        )
                    }
            }

            Spacer(Modifier.width(12.dp))

            Surface(
                color = SeriesBlue,
                shape =
                    RoundedCornerShape(20.dp)
            ) {
                Box(
                    modifier =
                        Modifier.size(38.dp),
                    contentAlignment =
                        Alignment.Center
                ) {
                    Text(
                        text = "▶",
                        color = Color.White,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
