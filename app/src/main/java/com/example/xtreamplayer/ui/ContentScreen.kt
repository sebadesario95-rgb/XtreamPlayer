package com.example.xtreamplayer.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
                    Arrangement.spacedBy(12.dp),
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

    /*
     * Stato esplicito della griglia FILM.
     * Su Fire TV il focus search di Compose può fermarsi all'ultima riga
     * attualmente composta. Manteniamo quindi lo stato della LazyVerticalGrid
     * e i FocusRequester dei poster per poter comporre e focalizzare
     * esplicitamente la riga successiva/precedente.
     */
    val movieGridState = rememberLazyGridState()
    val movieFocusRequesters = remember {
        mutableStateMapOf<Int, FocusRequester>()
    }
    val movieGridScope = rememberCoroutineScope()

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
                            minSize = 95.dp
                        ),
                    state = movieGridState,
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
                        Arrangement.spacedBy(10.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(16.dp)
                ) {
                    items(
                        count = filteredMovies.size,
                        key = { index ->
                            val movie = filteredMovies[index]
                            movie.stream_id
                                ?: movie.num
                                ?: movie.name
                                ?: index
                        }
                    ) { index ->

                        val movie = filteredMovies[index]

                        val posterFocusRequester =
                            movieFocusRequesters.getOrPut(index) {
                                FocusRequester()
                            }

                        MoviePosterCard(
                            movie = movie,
                            favorite =
                                movie.stream_id?.let {
                                    vm.isFavoriteMovie(it)
                                } == true,
                            focusRequester = posterFocusRequester,
                            onVerticalMove = { direction ->
                                /*
                                 * Ricaviamo quante colonne sono realmente
                                 * presenti dalla griglia Adaptive corrente.
                                 */
                                val columns =
                                    movieGridState.layoutInfo.visibleItemsInfo
                                        .maxOfOrNull { it.column + 1 }
                                        ?.coerceAtLeast(1)
                                        ?: 1

                                val targetIndex =
                                    (index + (direction * columns))
                                        .coerceIn(
                                            0,
                                            filteredMovies.lastIndex
                                        )

                                if (targetIndex != index) {
                                    movieGridScope.launch {
                                        /*
                                         * Prima componiamo/portiamo in vista
                                         * il poster destinazione...
                                         */
                                        movieGridState.scrollToItem(targetIndex)

                                        /*
                                         * ...poi lasciamo un frame a Compose
                                         * per creare il nuovo item e infine
                                         * gli assegniamo il focus.
                                         */
                                        withFrameNanos { }

                                        movieFocusRequesters[targetIndex]
                                            ?.requestFocus()
                                    }
                                    true
                                } else {
                                    false
                                }
                            },
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
             * RICERCA GLOBALE TV-FRIENDLY.
             *
             * D-pad: il campo riceve solo il focus e si illumina.
             * OK/ENTER: entra in modalità scrittura e apre la tastiera.
             */
            MovieSearchField(
                value = globalSearchQuery,
                onValueChange = onSearchQueryChange,
                onClearSearch = onClearSearch,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
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

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun MovieSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    var focused by remember {
        mutableStateOf(false)
    }

    var editing by remember {
        mutableStateOf(false)
    }

    val focusRequester = remember {
        FocusRequester()
    }

    val keyboardController =
        LocalSoftwareKeyboardController.current

    val focusManager =
        LocalFocusManager.current

    /*
     * Fine ricerca TV:
     * 1) chiude la tastiera;
     * 2) esce dalla modalità editing;
     * 3) sposta SUBITO il focus fuori dal TextField verso il contenuto
     *    sottostante, così il successivo D-pad lavora sui poster.
     *
     * Il problema precedente era proprio qui: la tastiera si chiudeva,
     * ma il focus restava prigioniero nel TextField.
     */
    fun finishEditingAndLeaveSearch() {
        keyboardController?.hide()
        editing = false

        /*
         * Il campo ricerca è nella top bar e i poster sono sotto.
         * MoveFocus(Down) usa la normale navigazione Compose TV e
         * consegna il focus al primo elemento focalizzabile sottostante.
         */
        focusManager.moveFocus(FocusDirection.Down)
    }

    LaunchedEffect(editing) {
        if (editing) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    /*
     * BACK durante la scrittura deve comportarsi come "fine ricerca",
     * NON come BACK della schermata Film/Home.
     */
    BackHandler(enabled = editing) {
        finishEditingAndLeaveSearch()
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .focusRequester(focusRequester)
            .onFocusChanged {
                focused = it.isFocused

                if (!it.isFocused && editing) {
                    editing = false
                    keyboardController?.hide()
                }
            }
            .onKeyEvent { event ->
                if (
                    event.type == KeyEventType.KeyUp &&
                    (
                        event.key == Key.Enter ||
                            event.key == Key.NumPadEnter ||
                            event.key == Key.DirectionCenter
                    )
                ) {
                    if (!editing) {
                        editing = true
                        true
                    } else {
                        finishEditingAndLeaveSearch()
                        true
                    }
                } else {
                    false
                }
            }
            .border(
                width = if (focused) 3.dp else 0.dp,
                color = if (focused) MovieBlue else Color.Transparent,
                shape = RoundedCornerShape(14.dp)
            ),
        readOnly = !editing,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                finishEditingAndLeaveSearch()
            }
        ),
        leadingIcon = {
            Text(
                text = "⌕",
                color = if (focused) MovieBlue else MovieTextSecondary,
                fontSize = 24.sp
            )
        },
        trailingIcon = {
            if (value.isNotBlank()) {
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
                text = "Cerca in tutti i film…",
                color = MovieTextSecondary,
                fontSize = 14.sp
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MovieBlue,
            unfocusedBorderColor = Color(0xFF26364B),
            focusedContainerColor = Color(0xFF0B1320),
            unfocusedContainerColor = Color(0xFF0B1320),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = MovieBlue
        ),
        shape = RoundedCornerShape(14.dp)
    )
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
                color = if (focused) MovieBlue else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            ),
        color =
            if (selected) {
                MovieBlue
            } else if (focused) {
                MovieBlueSoft.copy(alpha = 0.55f)
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
                        Key.DirectionDown ->
                            onVerticalMove(1)

                        Key.DirectionUp ->
                            onVerticalMove(-1)

                        else -> false
                    }
                } else {
                    false
                }
            }
            .border(
                width = if (focused) 3.dp else 0.dp,
                color = if (focused) MovieBlue else Color.Transparent,
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
            fontSize = 11.sp,
            lineHeight = 13.sp,
            fontWeight =
                FontWeight.SemiBold,
            maxLines = 2,
            overflow =
                TextOverflow.Ellipsis
        )
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
 * ============================================================
 * DETTAGLIO FILM CINEMATOGRAFICO
 * ============================================================
 */

@Composable
private fun MovieDetailFocusSurface(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(10.dp),
    selectedColor: Color = MovieBlue,
    normalColor: Color = Color.Black.copy(alpha = 0.38f),
    content: @Composable BoxScope.() -> Unit
) {
    var focused by remember {
        mutableStateOf(false)
    }

    Surface(
        onClick = onClick,
        modifier = modifier
            .scale(if (focused) 1.06f else 1f)
            .onFocusChanged {
                focused = it.isFocused
            }
            .border(
                width = if (focused) 3.dp else 1.dp,
                color = if (focused) {
                    MovieBlue
                } else {
                    Color.White.copy(alpha = 0.18f)
                },
                shape = shape
            ),
        shape = shape,
        color = if (focused) selectedColor else normalColor
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
            content = content
        )
    }
}

@Composable
private fun MovieDetailScreen(
    movie: VodStream,
    vm: AppViewModel,
    onPlay: (String) -> Unit,
    onBack: () -> Unit
) {
    /*
     * BACK fisico del telecomando:
     * dal dettaglio torna al catalogo FILM.
     * Questo handler interno ha priorità su quello generale di HomeScreen.
     */
    BackHandler {
        onBack()
    }

    val streamId = movie.stream_id

    LaunchedEffect(streamId) {
        streamId?.let { vm.loadVodInfo(it) }
    }

    DisposableEffect(streamId) {
        onDispose { vm.clearVodInfo() }
    }

    val response = vm.selectedVodInfo
    val info = response?.info
    val movieData = response?.movie_data

    val title = info?.name?.takeIf { it.isNotBlank() }
        ?: movieData?.name?.takeIf { it.isNotBlank() }
        ?: movie.name
        ?: "Film"

    val poster = info?.cover_big?.takeIf { it.isNotBlank() }
        ?: info?.movie_image_big?.takeIf { it.isNotBlank() }
        ?: info?.movie_image?.takeIf { it.isNotBlank() }
        ?: movie.stream_icon

    val backdrop = info?.backdrop_path
        ?.firstOrNull { it.isNotBlank() }
        ?: poster

    val releaseDate = info?.releasedate?.takeIf { it.isNotBlank() }
        ?: info?.releaseDate?.takeIf { it.isNotBlank() }

    val year = releaseDate
        ?.take(4)
        ?.takeIf { value -> value.length == 4 && value.all { it.isDigit() } }

    val duration = info?.duration?.takeIf { it.isNotBlank() }
        ?: info?.episode_run_time?.takeIf { it.isNotBlank() }

    val rating = info?.rating?.takeIf { it.isNotBlank() }
        ?: movie.rating?.takeIf { it.isNotBlank() }

    val genre = info?.genre?.takeIf { it.isNotBlank() }
    val plot = info?.plot?.takeIf { it.isNotBlank() }
        ?: info?.description?.takeIf { it.isNotBlank() }
    val director = info?.director?.takeIf { it.isNotBlank() }
    val cast = info?.cast?.takeIf { it.isNotBlank() }
    val country = info?.country?.takeIf { it.isNotBlank() }

    val metadata = listOfNotNull(
        year,
        duration,
        rating?.let { "★ $it" },
        genre
    )

    val favorite = streamId?.let { vm.isFavoriteMovie(it) } == true

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MovieBackground)
    ) {
        AsyncImage(
            model = backdrop,
            contentDescription = title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.34f
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xD9050A12))
        )

        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(76.dp)
                    .background(MovieTopBar.copy(alpha = 0.90f))
                    .padding(start = 14.dp, end = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MovieDetailFocusSurface(
                    onClick = onBack,
                    modifier = Modifier.size(46.dp),
                    shape = RoundedCornerShape(23.dp),
                    selectedColor = MovieBlue,
                    normalColor = Color(0xFF111A27)
                ) {
                    Text(
                        text = "‹",
                        color = Color.White,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Light
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "XTREAM PLAYER",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(10.dp))

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(22.dp)
                        .background(Color.White.copy(alpha = 0.22f))
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "FILM",
                    color = MovieBlue,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.weight(1f))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(VpnGreen, RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "VPN ATTIVA",
                        color = VpnGreen,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 42.dp, end = 42.dp, top = 28.dp, bottom = 28.dp),
                horizontalArrangement = Arrangement.spacedBy(36.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Center
                ) {
                    if (vm.loadingVodInfo) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MovieBlue,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Caricamento dettagli…",
                                color = MovieTextSecondary,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (metadata.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = metadata.joinToString("   •   "),
                            color = MovieTextSecondary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (plot != null) {
                        Spacer(modifier = Modifier.height(22.dp))
                        Text(
                            text = plot,
                            color = Color.White.copy(alpha = 0.86f),
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            maxLines = 6,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else if (!vm.loadingVodInfo && vm.vodInfoError != null) {
                        Spacer(modifier = Modifier.height(18.dp))
                        Text(
                            text = "Informazioni dettagliate non disponibili.",
                            color = MovieTextSecondary,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(26.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        MovieDetailFocusSurface(
                            onClick = {
                                val id = movie.stream_id
                                    ?: return@MovieDetailFocusSurface

                                val extension =
                                    movieData?.container_extension
                                        ?.takeIf { it.isNotBlank() }
                                        ?: movie.container_extension

                                val url = vm.streamUrl(
                                    type = "movie",
                                    id = id,
                                    extension = extension
                                ) ?: return@MovieDetailFocusSurface

                                onPlay(url)
                            },
                            modifier = Modifier
                                .width(178.dp)
                                .height(48.dp),
                            shape = RoundedCornerShape(10.dp),
                            selectedColor = MovieBlue,
                            normalColor = MovieBlue.copy(alpha = 0.78f)
                        ) {
                            Text(
                                text = "▶  GUARDA ORA",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        if (streamId != null) {
                            MovieDetailFocusSurface(
                                onClick = {
                                    vm.toggleFavoriteMovie(streamId)
                                },
                                modifier = Modifier
                                    .width(56.dp)
                                    .height(48.dp),
                                shape = RoundedCornerShape(10.dp),
                                selectedColor = MovieBlueSoft,
                                normalColor = Color.Black.copy(alpha = 0.38f)
                            ) {
                                Text(
                                    text = if (favorite) "★" else "☆",
                                    color = if (favorite) {
                                        MovieBlue
                                    } else {
                                        Color.White
                                    },
                                    fontSize = 22.sp
                                )
                            }
                        }
                    }

                    if (director != null || cast != null || country != null) {
                        Spacer(modifier = Modifier.height(26.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color.White.copy(alpha = 0.10f))
                        )
                        Spacer(modifier = Modifier.height(18.dp))

                        director?.let { MovieInfoLine("REGIA", it) }
                        cast?.let { MovieInfoLine("CAST", it) }
                        country?.let { MovieInfoLine("PAESE", it) }
                    }
                }

                Card(
                    modifier = Modifier
                        .width(250.dp)
                        .fillMaxHeight()
                        .padding(top = 10.dp, bottom = 10.dp)
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = MovieCardBackground
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    AsyncImage(
                        model = poster,
                        contentDescription = title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

@Composable
private fun MovieInfoLine(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            color = MovieBlue,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(58.dp)
        )

        Text(
            text = value,
            color = Color.White.copy(alpha = 0.78f),
            fontSize = 12.sp,
            lineHeight = 17.sp,
            maxLines = if (label == "CAST") 2 else 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
