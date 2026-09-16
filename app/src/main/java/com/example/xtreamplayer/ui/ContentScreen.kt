package com.example.xtreamplayer.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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

private val AccentGreen = Color(0xFFCAEA00)
private val DarkBackground = Color(0xFF090909)
private val SidebarBackground = Color(0xFF111111)
private val CardBackground = Color(0xFF181818)

private const val FAVORITES_CATEGORY = "__FAVORITES__"

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
    var selectedCategoryId by remember { mutableStateOf(initialCategoryId) }
    var showFavorites by remember { mutableStateOf(false) }
    var searchOpen by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

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
                    id != null && vm.isFavoriteLive(id)
                }
            }

            selectedCategoryId == null -> {
                streams
            }

            else -> {
                streams.filter {
                    it.category_id == selectedCategoryId
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
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                items(
                    items = filteredStreams,
                    key = { it.stream_id ?: it.num ?: 0 }
                ) { stream ->

                    LiveCard(
                        stream = stream,
                        favorite =
                            stream.stream_id?.let {
                                vm.isFavoriteLive(it)
                            } == true,
                        onClick = {
                            val id = stream.stream_id ?: return@LiveCard
                            val url = vm.streamUrl(
                                type = "live",
                                id = id
                            ) ?: return@LiveCard

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

@Composable
fun MovieContentScreen(
    title: String,
    categories: List<Category>,
    movies: List<VodStream>,
    vm: AppViewModel,
    onPlay: (String) -> Unit,
    onMoviePlay: (String, VodStream) -> Unit = { url, _ -> onPlay(url) },
    initialMovie: VodStream? = null,
    onInitialMovieConsumed: () -> Unit = {},
    onBack: () -> Unit
) {
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var showFavorites by remember { mutableStateOf(false) }
    var selectedMovie by remember { mutableStateOf<VodStream?>(initialMovie) }
    var searchOpen by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(initialMovie) {
        if (initialMovie != null) {
            selectedMovie = initialMovie
            onInitialMovieConsumed()
        }
    }

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
        searchQuery,
        vm.favoriteMovieIds
    ) {
        when {
            searchQuery.isNotBlank() -> {
                movies.filter {
                    it.name?.contains(
                        searchQuery,
                        ignoreCase = true
                    ) == true
                }
            }

            showFavorites -> {
                movies.filter {
                    val id = it.stream_id
                    id != null && vm.isFavoriteMovie(id)
                }
            }

            selectedCategoryId == null -> {
                movies
            }

            else -> {
                movies.filter {
                    it.category_id == selectedCategoryId
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
            filteredMovies.isEmpty() &&
            showFavorites &&
            searchQuery.isBlank()
        ) {
            EmptyFavoritesMessage()
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(180.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                items(
                    items = filteredMovies,
                    key = { it.stream_id ?: it.num ?: 0 }
                ) { movie ->

                    MovieCard(
                        movie = movie,
                        favorite =
                            movie.stream_id?.let {
                                vm.isFavoriteMovie(it)
                            } == true,
                        onClick = {
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

@Composable
private fun EmptyFavoritesMessage() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
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
                text = "Tieni premuto su un contenuto per aggiungerlo.",
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
                .background(Color(0xFF101010)),
            verticalAlignment = Alignment.CenterVertically
        ) {

            TextButton(
                onClick = onBack,
                modifier = Modifier.padding(start = 8.dp)
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
                    onValueChange = onSearchQueryChange,
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
                            color = Color.Gray
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentGreen,
                        unfocusedBorderColor = Color.DarkGray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = AccentGreen
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

            CategorySidebar(
                categories = categories,
                selectedCategoryId = selectedCategoryId,
                showFavorites = showFavorites,
                onCategorySelected = onCategorySelected,
                onFavoritesSelected = onFavoritesSelected
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
            .background(SidebarBackground),
        contentPadding = PaddingValues(
            vertical = 14.dp,
            horizontal = 10.dp
        )
    ) {

        item {
            SidebarItem(
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
            SidebarItem(
                text = "⭐ PREFERITI",
                selected = showFavorites,
                onClick = onFavoritesSelected
            )
        }

        items(
            items = categories,
            key = { it.category_id ?: it.category_name.orEmpty() }
        ) { category ->

            SidebarItem(
                text = category.category_name
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
            overflow = TextOverflow.Ellipsis
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
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            AsyncImage(
                model = stream.stream_icon,
                contentDescription = stream.name,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                contentScale = ContentScale.Fit
            )

            if (favorite) {
                Text(
                    text = "★",
                    color = AccentGreen,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                )
            }

            Text(
                text = stream.name ?: "Canale",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(
                        Color.Black.copy(alpha = 0.72f)
                    )
                    .padding(
                        horizontal = 10.dp,
                        vertical = 7.dp
                    )
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MovieCard(
    movie: VodStream,
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
            containerColor = CardBackground
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            AsyncImage(
                model = movie.stream_icon,
                contentDescription = movie.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            if (favorite) {
                Text(
                    text = "★",
                    color = AccentGreen,
                    fontSize = 22.sp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                )
            }

            Text(
                text = movie.name ?: "Film",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(
                        Color.Black.copy(alpha = 0.78f)
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
                .background(Color(0xFF101010)),
            verticalAlignment = Alignment.CenterVertically
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
                text = movie.name ?: "Film",
                color = Color.White,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            horizontalArrangement = Arrangement.spacedBy(28.dp)
        ) {

            AsyncImage(
                model = movie.stream_icon,
                contentDescription = movie.name,
                modifier = Modifier
                    .width(260.dp)
                    .fillMaxHeight()
                    .clip(
                        RoundedCornerShape(14.dp)
                    ),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
            ) {

                Text(
                    text = movie.name ?: "Film",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(14.dp)
                )

                movie.rating?.takeIf {
                    it.isNotBlank()
                }?.let {
                    Text(
                        text = "★ $it",
                        color = AccentGreen,
                        fontSize = 16.sp
                    )
                }

                Spacer(
                    modifier = Modifier.height(24.dp)
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
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentGreen,
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = "▶  RIPRODUCI",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
