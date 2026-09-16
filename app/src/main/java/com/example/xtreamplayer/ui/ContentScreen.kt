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

@Composable
fun LiveContentScreen(
    title: String,
    categories: List<Category>,
    streams: List<LiveStream>,
    vm: AppViewModel,
    onPlay: (String) -> Unit,
    onLivePlay: (String, LiveStream) -> Unit = { url, _ ->
        onPlay(url)
    },
    initialCategoryId: String? = null,
    onInitialLiveConsumed: () -> Unit = {},
    onBack: () -> Unit
) {
    var selectedCategoryId by remember {
        mutableStateOf(initialCategoryId)
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
            onInitialLiveConsumed()
        }
    }

    val filteredStreams = remember(
        streams,
        selectedCategoryId,
        searchQuery
    ) {
        if (searchQuery.isNotBlank()) {
            streams.filter {
                it.name?.contains(
                    searchQuery,
                    ignoreCase = true
                ) == true
            }
        } else if (selectedCategoryId == null) {
            streams
        } else {
            streams.filter {
                it.category_id == selectedCategoryId
            }
        }
    }

    ContentWithSidebar(
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
            items(filteredStreams) { stream ->
                LiveCard(
                    stream = stream,
                    favorite = stream.stream_id?.let {
                        vm.isFavoriteLive(it)
                    } == true,
                    onClick = {
                        val id = stream.stream_id ?: return@LiveCard

                        vm.streamUrl(
                            type = "live",
                            id = id
                        )?.let { url ->
                            onLivePlay(
                                url,
                                stream
                            )
                        }
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
    var selectedCategoryId by remember {
        mutableStateOf<String?>(null)
    }

    var selectedMovie by remember {
        mutableStateOf<VodStream?>(initialMovie)
    }

    var searchOpen by remember {
        mutableStateOf(false)
    }

    var searchQuery by remember {
        mutableStateOf("")
    }

    LaunchedEffect(initialMovie) {
        if (initialMovie != null) {
            selectedMovie = initialMovie
            onInitialMovieConsumed()
        }
    }

    val filteredMovies = remember(
        movies,
        selectedCategoryId,
        searchQuery
    ) {
        if (searchQuery.isNotBlank()) {
            movies.filter {
                it.name?.contains(
                    searchQuery,
                    ignoreCase = true
                ) == true
            }
        } else if (selectedCategoryId == null) {
            movies
        } else {
            movies.filter {
                it.category_id == selectedCategoryId
            }
        }
    }

    if (selectedMovie != null) {
        MovieDetailScreen(
            movie = selectedMovie!!,
            vm = vm,
            onPlay = onMoviePlay,
            onBack = {
                selectedMovie = null
            }
        )
    } else {
        ContentWithSidebar(
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
                items(filteredMovies) { movie ->
                    MovieCard(
                        movie = movie,
                        favorite = movie.stream_id?.let {
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
private fun ContentWithSidebar(
    title: String,
    categories: List<Category>,
    selectedCategoryId: String?,
    onCategorySelected: (String?) -> Unit,
    searchOpen: Boolean = false,
    searchQuery: String = "",
    onSearchOpen: () -> Unit = {},
    onSearchQueryChange: (String) -> Unit = {},
    onSearchClose: () -> Unit = {},
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090909))
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
                    "← INDIETRO",
                    color = Color(0xFFCAEA00),
                    fontWeight = FontWeight.Bold
                )
            }

            if (searchOpen) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .weight(1f)
                        .padding(
                            start = 12.dp,
                            end = 8.dp
                        ),
                    placeholder = {
                        Text(
                            "Cerca...",
                            color = Color.Gray
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFCAEA00),
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color(0xFFCAEA00)
                    )
                )

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
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

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

        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF090909))
        ) {
            CategorySidebar(
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
private fun CategorySidebar(
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
            SidebarItem(
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

            SidebarItem(
                name = category.category_name ?: "Categoria",
                selected = selectedCategoryId == id,
                onClick = {
                    onCategorySelected(id)
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SidebarItem(
    name: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (selected) {
                    Color(0xFFCAEA00)
                } else {
                    Color(0xFF151515)
                }
            )
            .combinedClickable(
                onClick = onClick
            )
            .padding(
                horizontal = 14.dp,
                vertical = 12.dp
            )
    ) {
        Text(
            text = name,
            color =
                if (selected) {
                    Color.Black
                } else {
                    Color.White
                },
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
            .height(170.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF151515)
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                AsyncImage(
                    model = stream.stream_icon,
                    contentDescription = stream.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentScale = ContentScale.Fit
                )

                Text(
                    text = stream.name ?: "Canale",
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

            if (favorite) {
                Text(
                    text = "★",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    color = Color(0xFFCAEA00),
                    fontSize = 22.sp
                )
            }
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
            .height(260.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF151515)
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                AsyncImage(
                    model = movie.stream_icon,
                    contentDescription = movie.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentScale = ContentScale.Crop
                )

                Text(
                    text = movie.name ?: "Film",
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

            if (favorite) {
                Text(
                    text = "★",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    color = Color(0xFFCAEA00),
                    fontSize = 22.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MovieDetailScreen(
    movie: VodStream,
    vm: AppViewModel,
    onPlay: (String, VodStream) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        movie.name ?: "Film",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF090909))
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                AsyncImage(
                    model = movie.stream_icon,
                    contentDescription = movie.name,
                    modifier = Modifier
                        .width(220.dp)
                        .height(320.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = movie.name ?: "Film",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    movie.rating?.let {
                        Text(
                            text = "★ $it",
                            color = Color(0xFFCAEA00),
                            fontSize = 15.sp
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(18.dp)
                    )

                    Button(
                        onClick = {
                            val id =
                                movie.stream_id
                                    ?: return@Button

                            vm.streamUrl(
                                type = "movie",
                                id = id,
                                extension =
                                    movie.container_extension
                            )?.let { url ->
                                onPlay(
                                    url,
                                    movie
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFCAEA00),
                            contentColor = Color.Black
                        )
                    ) {
                        Text(
                            "▶ GUARDA",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
