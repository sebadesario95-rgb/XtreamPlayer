package com.example.xtreamplayer.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.xtreamplayer.data.LiveStream
import com.example.xtreamplayer.data.VodStream
import com.example.xtreamplayer.viewmodel.AppViewModel

/*
 * Colori legacy utilizzati dalla vecchia schermata Live
 * presente ancora in questo file.
 */
private val AccentGreen = Color(0xFFCAEA00)
private val DarkBackground = Color(0xFF090909)
private val SidebarBackground = Color(0xFF111111)
private val CardBackground = Color(0xFF181818)

/*
 * Palette FILM.
 */
private val MovieBackground = Color(0xFF050A12)
private val MovieTopBar = Color(0xFF080E18)
private val MovieSidebar = Color(0xFF080D16)
private val MovieCardBackground = Color(0xFF0C1420)
private val MovieBlue = Color(0xFF1677FF)
private val MovieBlueSoft = Color(0xFF0E3F82)
private val MovieTextSecondary = Color(0xFF8E9BAD)
private val VpnGreen = Color(0xFF43E07B)

@Composable
fun LiveContentScreen(
    title: String,
    categories: List<Category>,
    streams: List<LiveStream>,
    vm: AppViewModel,
    onPlay: (String) -> Unit,
    onLivePlay: (String, LiveStream) -> Unit = { url, _ -> onPlay(url) },
    initialCategoryId: String? = null,
    onInitialLiveConsumed: () -> Unit = {},
    onBack: () -> Unit
) {
    var selectedCategoryId by remember {
        mutableStateOf(initialCategoryId)
    }

    var showFavorites by remember {
        mutableStateOf(false)
    }

    var searchOpen by remember {
        mutableStateOf(false)
    }

    var searchQuery by remember {
        mutableStateOf("")
    }

    LaunchedEffect(initialCategoryId) {
        if (initialCategoryId != null) {
            selectedCategoryId = initialCategoryId
            showFavorites = false
            onInitialLiveConsumed()
        }
    }

    val filteredStreams = remember(
        streams,
        selectedCategoryId,
        showFavorites,
        searchQuery,
        vm.favoriteLiveIds
    ) {
        when {
            searchQuery.isNotBlank() -> {
                streams.filter {
                    it.name?.contains(
                        searchQuery,
                        ignoreCase = true
                    ) == true
                }
            }

            showFavorites -> {
                streams.filter {
                    val id = it.stream_id
                    id != null &&
                        vm.isFavoriteLive(id)
                }
            }

            selectedCategoryId == null -> {
                streams
            }

            else -> {
                streams.filter {
                    it.category_id ==
                        selectedCategoryId
                }
            }
        }
    }

    ContentWithSidebar(
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
            filteredStreams.isEmpty() &&
            showFavorites &&
            searchQuery.isBlank()
        ) {
            EmptyFavoritesMessage()
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
                    items = filteredStreams,
                    key = {
                        it.stream_id
                            ?: it.num
                            ?: 0
                    }
                ) { stream ->

                    LiveCard(
                        stream = stream,
                        favorite =
                            stream.stream_id?.let {
                                vm.isFavoriteLive(it)
                            } == true,
                        onClick = {
                            val id =
                                stream.stream_id
                                    ?: return@LiveCard

                            val url =
                                vm.streamUrl(
                                    type = "live",
                                    id = id
                                )
                                    ?: return@LiveCard

                            onLivePlay(
                                url,
                                stream
                            )
                        },
                        onLongClick = {
                            stream.stream_id?.let {
                                vm.toggleFavoriteLive(it)
                            }
                        }
                    )
                }
            }
        }
    }
}

/*
 * ============================================================
 * FILM
 * ============================================================
 */

@Composable
fun MovieContentScreen(
    title: String,
    categories: List<Category>,
    movies: List<VodStream>,
    vm: AppViewModel,
    onPlay: (String) -> Unit,
    onMoviePlay: (String, VodStream) -> Unit = { url, _ ->
        onPlay(url)
    },
    initialMovie: VodStream? = null,
    onInitialMovieConsumed: () -> Unit = {},
    onBack: () -> Unit
) {
    /*
     * Non esiste più "TUTTI".
     *
     * Quando entriamo nella sezione Film selezioniamo
     * automaticamente la prima categoria disponibile.
     */
    var selectedCategoryId by remember(categories) {
        mutableStateOf(
            categories.firstOrNull()?.category_id
        )
    }

    var showFavorites by remember {
        mutableStateOf(false)
    }

    var selectedMovie by remember {
        mutableStateOf<VodStream?>(initialMovie)
    }

    /*
     * Ricerca globale:
     * quando contiene testo, ignora la categoria selezionata
     * e cerca sull'intero catalogo Film.
     */
    var globalSearchQuery by remember {
        mutableStateOf("")
    }

    LaunchedEffect(initialMovie) {
        if (initialMovie != null) {
            selectedMovie = initialMovie
            onInitialMovieConsumed()
        }
    }

    /*
     * Per questo step manteniamo il dettaglio già esistente.
     * Nel prossimo step verrà sostituito dalla nuova schermata
     * cinematografica alimentata da get_vod_info.
     */
    if (selectedMovie != null) {
        MovieDetailScreen(
            movie = selectedMovie!!,
            vm = vm,
            onPlay = { url ->
                onMoviePlay(
                    url,
                    selectedMovie!!
                )
            },
            onBack = {
                selectedMovie = null
            }
        )
        return
    }

    val filteredMovies = remember(
        movies,
        selectedCategoryId,
        showFavorites,
        globalSearchQuery,
        vm.favoriteMovieIds
    ) {
        when {
            globalSearchQuery.isNotBlank() -> {
                movies.filter {
                    it.name?.contains(
                        globalSearchQuery,
                        ignoreCase = true
                    ) == true
                }
            }

            showFavorites -> {
                movies.filter {
                    val id = it.stream_id

                    id != null &&
                        vm.isFavoriteMovie(id)
                }
            }

            selectedCategoryId != null -> {
                movies.filter {
                    it.category_id ==
                        selectedCategoryId
                }
            }

            else -> {
                emptyList()
            }
        }
    }

    val selectedCategoryName = remember(
        categories,
        selectedCategoryId,
        showFavorites,
        globalSearchQuery
    ) {
        when {
            globalSearchQuery.isNotBlank() ->
                "RISULTATI"

            showFavorites ->
                "PREFERITI"

            else ->
                categories
                    .firstOrNull {
                        it.category_id ==
                            selectedCategoryId
                    }
                    ?.category_name
                    ?.uppercase()
                    ?: "FILM"
        }
    }

    MovieCatalogLayout(
        categories = categories,
        selectedCategoryId = selectedCategoryId,
        showFavorites = showFavorites,
        globalSearchQuery = globalSearchQuery,
        selectedCategoryName = selectedCategoryName,
        movieCount = filteredMovies.size,
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
            filteredMovies.isEmpty() &&
                globalSearchQuery.isNotBlank() -> {
                MovieEmptyState(
                    icon = "⌕",
                    title = "Nessun film trovato",
                    subtitle =
                        "Prova con un altro titolo."
                )
            }

            filteredMovies.isEmpty() &&
                showFavorites -> {
                MovieEmptyState(
                    icon = "★",
                    title = "Nessun preferito",
                    subtitle =
                        "Tieni premuto su un film per aggiungerlo."
                )
            }

            filteredMovies.isEmpty() -> {
                MovieEmptyState(
                    icon = "🎬",
                    title = "Nessun film disponibile",
                    subtitle =
                        "Questa categoria non contiene film."
                )
            }

            else -> {
                LazyVerticalGrid(
                    columns =
                        GridCells.Adaptive(
                            minSize = 145.dp
                        ),
                    modifier =
                        Modifier.fillMaxSize(),
                    contentPadding =
                        PaddingValues(
                            start = 22.dp,
                            end = 22.dp,
                            top = 6.dp,
                            bottom = 24.dp
                        ),
                    horizontalArrangement =
                        Arrangement.spacedBy(16.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(22.dp)
                ) {
                    items(
                        items = filteredMovies,
                        key = {
                            it.stream_id
                                ?: it.num
                                ?: it.name
                                ?: ""
                        }
                    ) { movie ->

                        MoviePosterCard(
                            movie = movie,
                            favorite =
                                movie.stream_id?.let {
                                    vm.isFavoriteMovie(it)
                                } == true,
                            onClick = {
                                /*
                                 * IMPORTANTISSIMO:
                                 * nessun get_vod_info qui durante
                                 * focus/scroll.
                                 *
                                 * Selezioniamo semplicemente il film.
                                 * Il caricamento dettagli verrà collegato
                                 * alla schermata Detail nel prossimo step.
                                 */
                                selectedMovie = movie
                            },
                            onLongClick = {
                                movie.stream_id?.let {
                                    vm.toggleFavoriteMovie(it)
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
private fun MovieCatalogLayout(
    categories: List<Category>,
    selectedCategoryId: String?,
    showFavorites: Boolean,
    globalSearchQuery: String,
    selectedCategoryName: String,
    movieCount: Int,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onCategorySelected: (String?) -> Unit,
    onFavoritesSelected: () -> Unit,
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MovieBackground)
    ) {

        /*
         * TOP BAR
         */
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp)
                .background(MovieTopBar)
                .padding(
                    start = 14.dp,
                    end = 24.dp
                ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Surface(
                onClick = onBack,
                modifier = Modifier.size(46.dp),
                shape = RoundedCornerShape(23.dp),
                color = Color(0xFF111A27)
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

            Spacer(
                modifier = Modifier.width(16.dp)
            )

            Text(
                text = "XTREAM PLAYER",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.width(10.dp)
            )

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

            Spacer(
                modifier = Modifier.width(10.dp)
            )

            Text(
                text = "FILM",
                color = MovieBlue,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.width(34.dp)
            )

            /*
             * RICERCA GLOBALE
             */
            OutlinedTextField(
                value = globalSearchQuery,
                onValueChange =
                    onSearchQueryChange,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                singleLine = true,
                leadingIcon = {
                    Text(
                        text = "⌕",
                        color = MovieTextSecondary,
                        fontSize = 24.sp
                    )
                },
                trailingIcon = {
                    if (
                        globalSearchQuery
                            .isNotBlank()
                    ) {
                        TextButton(
                            onClick =
                                onClearSearch
                        ) {
                            Text(
                                text = "✕",
                                color =
                                    Color.White,
                                fontSize = 16.sp
                            )
                        }
                    }
                },
                placeholder = {
                    Text(
                        text =
                            "Cerca in tutti i film…",
                        color =
                            MovieTextSecondary,
                        fontSize = 14.sp
                    )
                },
                colors =
                    OutlinedTextFieldDefaults
                        .colors(
                            focusedBorderColor =
                                MovieBlue,
                            unfocusedBorderColor =
                                Color(
                                    0xFF26364B
                                ),
                            focusedContainerColor =
                                Color(
                                    0xFF0B1320
                                ),
                            unfocusedContainerColor =
                                Color(
                                    0xFF0B1320
                                ),
                            focusedTextColor =
                                Color.White,
                            unfocusedTextColor =
                                Color.White,
                            cursorColor =
                                MovieBlue
                        ),
                shape =
                    RoundedCornerShape(14.dp)
            )

            Spacer(
                modifier = Modifier.width(28.dp)
            )

            /*
             * VPN puramente decorativa.
             */
            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            VpnGreen,
                            RoundedCornerShape(
                                4.dp
                            )
                        )
                )

                Spacer(
                    modifier =
                        Modifier.width(8.dp)
                )

                Text(
                    text = "VPN ATTIVA",
                    color = VpnGreen,
                    fontSize = 13.sp,
                    fontWeight =
                        FontWeight.Bold
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxSize()
        ) {

            /*
             * SIDEBAR FILM
             */
            MovieCategorySidebar(
                categories = categories,
                selectedCategoryId =
                    selectedCategoryId,
                showFavorites =
                    showFavorites,
                onCategorySelected =
                    onCategorySelected,
                onFavoritesSelected =
                    onFavoritesSelected
            )

            /*
             * CATALOGO
             */
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
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Text(
                        text =
                            selectedCategoryName,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight =
                            FontWeight.Bold,
                        maxLines = 1,
                        overflow =
                            TextOverflow.Ellipsis
                    )

                    Spacer(
                        modifier =
                            Modifier.width(12.dp)
                    )

                    Text(
                        text =
                            "$movieCount film",
                        color =
                            MovieTextSecondary,
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
private fun MovieCategorySidebar(
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
            .background(MovieSidebar),
        contentPadding =
            PaddingValues(
                start = 12.dp,
                end = 12.dp,
                top = 18.dp,
                bottom = 18.dp
            ),
        verticalArrangement =
            Arrangement.spacedBy(5.dp)
    ) {

        /*
         * Niente "TUTTI".
         * Preferiti è la prima voce.
         */
        item {
            MovieSidebarItem(
                text = "★  PREFERITI",
                selected = showFavorites,
                onClick =
                    onFavoritesSelected
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

            val categoryId =
                category.category_id

            MovieSidebarItem(
                text =
                    category.category_name
                        ?: "Categoria",
                selected =
                    !showFavorites &&
                        selectedCategoryId ==
                            categoryId,
                onClick = {
                    onCategorySelected(
                        categoryId
                    )
                }
            )
        }
    }
}

@Composable
private fun MovieSidebarItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        color =
            if (selected) {
                MovieBlue
            } else {
                Color.Transparent
            },
        shape =
            RoundedCornerShape(10.dp),
        onClick = onClick
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight =
                if (selected) {
                    FontWeight.Bold
                } else {
                    FontWeight.Medium
                },
            modifier = Modifier.padding(
                horizontal = 14.dp,
                vertical = 11.dp
            ),
            maxLines = 1,
            overflow =
                TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MoviePosterCard(
    movie: VodStream,
    favorite: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.67f)
                .border(
                    width = 1.dp,
                    color =
                        if (favorite) {
                            MovieBlue
                        } else {
                            Color.White.copy(
                                alpha = 0.08f
                            )
                        },
                    shape =
                        RoundedCornerShape(
                            12.dp
                        )
                ),
            colors =
                CardDefaults.cardColors(
                    containerColor =
                        MovieCardBackground
                ),
            shape =
                RoundedCornerShape(12.dp)
        ) {
            Box(
                modifier =
                    Modifier.fillMaxSize()
            ) {

                AsyncImage(
                    model =
                        movie.stream_icon,
                    contentDescription =
                        movie.name,
                    modifier =
                        Modifier.fillMaxSize(),
                    contentScale =
                        ContentScale.Crop
                )

                /*
                 * Preferito.
                 */
                if (favorite) {
                    Surface(
                        modifier = Modifier
                            .align(
                                Alignment.TopEnd
                            )
                            .padding(8.dp),
                        color =
                            Color.Black.copy(
                                alpha = 0.72f
                            ),
                        shape =
                            RoundedCornerShape(
                                18.dp
                            )
                    ) {
                        Text(
                            text = "★",
                            color = MovieBlue,
                            fontSize = 18.sp,
                            modifier =
                                Modifier.padding(
                                    horizontal =
                                        8.dp,
                                    vertical =
                                        4.dp
                                )
                        )
                    }
                }
            }
        }

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = movie.name ?: "Film",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight =
                FontWeight.SemiBold,
            maxLines = 1,
            overflow =
                TextOverflow.Ellipsis
        )

        movie.rating
            ?.takeIf {
                it.isNotBlank()
            }
            ?.let { rating ->
                Spacer(
                    modifier =
                        Modifier.height(3.dp)
                )

                Text(
                    text = "★ $rating",
                    color =
                        MovieTextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }
    }
}

@Composable
private fun MovieEmptyState(
    icon: String,
    title: String,
    subtitle: String
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment =
            Alignment.Center
    ) {
        Column(
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {
            Text(
                text = icon,
                color = MovieBlue,
                fontSize = 38.sp
            )

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            Text(
                text = title,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight =
                    FontWeight.SemiBold
            )

            Spacer(
                modifier =
                    Modifier.height(6.dp)
            )

            Text(
                text = subtitle,
                color =
                    MovieTextSecondary,
                fontSize = 13.sp
            )
        }
    }
}

/*
 * ============================================================
 * COMPONENTI LEGACY LIVE
 * ============================================================
 */

@Composable
private fun EmptyFavoritesMessage() {
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
                modifier =
                    Modifier.height(12.dp)
            )

            Text(
                text =
                    "Nessun preferito aggiunto",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight =
                    FontWeight.SemiBold
            )

            Spacer(
                modifier =
                    Modifier.height(6.dp)
            )

            Text(
                text =
                    "Tieni premuto su un contenuto per aggiungerlo.",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun ContentWithSidebar(
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
            .background(DarkBackground)
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
                onClick = onBack,
                modifier =
                    Modifier.padding(
                        start = 8.dp
                    )
            ) {
                Text(
                    text = "‹",
                    color = AccentGreen,
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
                            "Cerca...",
                            color =
                                Color.Gray
                        )
                    },
                    colors =
                        OutlinedTextFieldDefaults
                            .colors(
                                focusedBorderColor =
                                    AccentGreen,
                                unfocusedBorderColor =
                                    Color.DarkGray,
                                focusedTextColor =
                                    Color.White,
                                unfocusedTextColor =
                                    Color.White,
                                cursorColor =
                                    AccentGreen
                            )
                )

                TextButton(
                    onClick =
                        onSearchClose
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
                    fontWeight =
                        FontWeight.Bold,
                    modifier =
                        Modifier.weight(1f)
                )

                TextButton(
                    onClick =
                        onSearchOpen
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
            modifier =
                Modifier.fillMaxSize()
        ) {

            CategorySidebar(
                categories = categories,
                selectedCategoryId =
                    selectedCategoryId,
                showFavorites =
                    showFavorites,
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
private fun CategorySidebar(
    categories: List<Category>,
    selectedCategoryId: String?,
    showFavorites: Boolean,
    onCategorySelected: (String?) -> Unit,
    onFavoritesSelected: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .width(230.dp)
            .fillMaxHeight()
            .background(
                SidebarBackground
            ),
        contentPadding =
            PaddingValues(
                vertical = 14.dp,
                horizontal = 10.dp
            )
    ) {

        item {
            SidebarItem(
                text = "TUTTI",
                selected =
                    !showFavorites &&
                        selectedCategoryId ==
                            null,
                onClick = {
                    onCategorySelected(
                        null
                    )
                }
            )
        }

        item {
            SidebarItem(
                text = "⭐ PREFERITI",
                selected =
                    showFavorites,
                onClick =
                    onFavoritesSelected
            )
        }

        items(
            items = categories,
            key = {
                it.category_id
                    ?: it.category_name
                        .orEmpty()
            }
        ) { category ->

            SidebarItem(
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
private fun SidebarItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val background =
        if (selected) {
            AccentGreen
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
            .padding(
                vertical = 3.dp
            )
            .clip(
                RoundedCornerShape(
                    10.dp
                )
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
private fun LiveCard(
    stream: LiveStream,
    favorite: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    CardBackground
            ),
        shape =
            RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier =
                Modifier.fillMaxSize()
        ) {

            AsyncImage(
                model =
                    stream.stream_icon,
                contentDescription =
                    stream.name,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                contentScale =
                    ContentScale.Fit
            )

            if (favorite) {
                Text(
                    text = "★",
                    color = AccentGreen,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .align(
                            Alignment.TopEnd
                        )
                        .padding(8.dp)
                )
            }

            Text(
                text =
                    stream.name
                        ?: "Canale",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight =
                    FontWeight.SemiBold,
                maxLines = 1,
                overflow =
                    TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(
                        Alignment.BottomStart
                    )
                    .fillMaxWidth()
                    .background(
                        Color.Black.copy(
                            alpha = 0.72f
                        )
                    )
                    .padding(
                        horizontal = 10.dp,
                        vertical = 7.dp
                    )
            )
        }
    }
}

/*
 * Vecchio dettaglio Film.
 *
 * Lo lasciamo intenzionalmente funzionante per questo build.
 * Nel prossimo step verrà completamente sostituito.
 */
@Composable
private fun MovieDetailScreen(
    movie: VodStream,
    vm: AppViewModel,
    onPlay: (String) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
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
                    color = AccentGreen,
                    fontSize = 34.sp
                )
            }

            Text(
                text =
                    movie.name
                        ?: "Film",
                color = Color.White,
                fontSize = 21.sp,
                fontWeight =
                    FontWeight.Bold,
                maxLines = 1,
                overflow =
                    TextOverflow.Ellipsis
            )
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            horizontalArrangement =
                Arrangement.spacedBy(
                    28.dp
                )
        ) {

            AsyncImage(
                model =
                    movie.stream_icon,
                contentDescription =
                    movie.name,
                modifier = Modifier
                    .width(260.dp)
                    .fillMaxHeight()
                    .clip(
                        RoundedCornerShape(
                            14.dp
                        )
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
                        movie.name
                            ?: "Film",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight =
                        FontWeight.Bold
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            14.dp
                        )
                )

                movie.rating
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?.let {
                        Text(
                            text = "★ $it",
                            color =
                                AccentGreen,
                            fontSize =
                                16.sp
                        )
                    }

                Spacer(
                    modifier =
                        Modifier.height(
                            24.dp
                        )
                )

                Button(
                    onClick = {
                        val id =
                            movie.stream_id
                                ?: return@Button

                        val url =
                            vm.streamUrl(
                                type = "movie",
                                id = id,
                                extension =
                                    movie.container_extension
                            )
                                ?: return@Button

                        onPlay(url)
                    },
                    colors =
                        ButtonDefaults
                            .buttonColors(
                                containerColor =
                                    AccentGreen,
                                contentColor =
                                    Color.Black
                            )
                ) {
                    Text(
                        text =
                            "▶  RIPRODUCI",
                        fontWeight =
                            FontWeight.Bold
                    )
                }
            }
        }
    }
}
