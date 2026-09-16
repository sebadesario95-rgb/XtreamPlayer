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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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

private val BackgroundColor = Color(0xFF090909)
private val SurfaceColor = Color(0xFF151515)
private val BorderColor = Color(0xFF292929)
private val AccentColor = Color(0xFFCAEA00)

@Composable
fun LiveContentScreen(
    categories: List<Category>,
    streams: List<LiveStream>,
    initialCategoryId: String? = null,
    onInitialCategoryConsumed: () -> Unit = {},
    onBack: () -> Unit,
    onPlay: (String) -> Unit
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
            onInitialCategoryConsumed()
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
                    searchQuery.trim(),
                    ignoreCase = true
                ) == true
            }
        } else {
            if (selectedCategoryId == null) {
                streams
            } else {
                streams.filter {
                    it.category_id == selectedCategoryId
                }
            }
        }
    }

    ContentWithSidebar(
        title = "LIVE TV",
        categories = categories,
        selectedCategoryId = selectedCategoryId,
        onCategorySelected = {
            selectedCategoryId = it
        },
        onBack = onBack,
        searchOpen = searchOpen,
        searchQuery = searchQuery,
        onSearchClick = {
            searchOpen = true
        },
        onSearchQueryChange = {
            searchQuery = it
        },
        onSearchClose = {
            searchOpen = false
            searchQuery = ""
        }
    ) {
        if (filteredStreams.isEmpty()) {
            EmptyContentMessage(
                message =
                    if (searchQuery.isNotBlank()) {
                        "Nessun canale trovato"
                    } else {
                        "Nessun canale disponibile"
                    }
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(
                    minSize = 180.dp
                ),
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
                        it.stream_id ?: it.num ?: 0
                    }
                ) { channel ->
                    LiveChannelCard(
                        channel = channel,
                        onClick = {
                            val id =
                                channel.stream_id
                                    ?: return@LiveChannelCard

                            onPlay("live:$id")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MovieContentScreen(
    categories: List<Category>,
    movies: List<VodStream>,
    initialMovie: VodStream? = null,
    onInitialMovieConsumed: () -> Unit = {},
    onBack: () -> Unit,
    onPlay: (String) -> Unit
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

    if (selectedMovie != null) {
        MovieDetailsScreen(
            movie = selectedMovie!!,
            onBack = {
                selectedMovie = null
            },
            onPlay = onPlay
        )
        return
    }

    val filteredMovies = remember(
        movies,
        selectedCategoryId,
        searchQuery
    ) {
        if (searchQuery.isNotBlank()) {
            movies.filter {
                it.name?.contains(
                    searchQuery.trim(),
                    ignoreCase = true
                ) == true
            }
        } else {
            if (selectedCategoryId == null) {
                movies
            } else {
                movies.filter {
                    it.category_id == selectedCategoryId
                }
            }
        }
    }

    ContentWithSidebar(
        title = "FILM",
        categories = categories,
        selectedCategoryId = selectedCategoryId,
        onCategorySelected = {
            selectedCategoryId = it
        },
        onBack = onBack,
        searchOpen = searchOpen,
        searchQuery = searchQuery,
        onSearchClick = {
            searchOpen = true
        },
        onSearchQueryChange = {
            searchQuery = it
        },
        onSearchClose = {
            searchOpen = false
            searchQuery = ""
        }
    ) {
        if (filteredMovies.isEmpty()) {
            EmptyContentMessage(
                message =
                    if (searchQuery.isNotBlank()) {
                        "Nessun film trovato"
                    } else {
                        "Nessun film disponibile"
                    }
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(
                    minSize = 160.dp
                ),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(20.dp),
                horizontalArrangement =
                    Arrangement.spacedBy(16.dp),
                verticalArrangement =
                    Arrangement.spacedBy(18.dp)
            ) {
                items(
                    items = filteredMovies,
                    key = {
                        it.stream_id ?: it.num ?: 0
                    }
                ) { movie ->
                    MovieCard(
                        movie = movie,
                        onClick = {
                            selectedMovie = movie
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
    onBack: () -> Unit,
    searchOpen: Boolean = false,
    searchQuery: String = "",
    onSearchClick: () -> Unit = {},
    onSearchQueryChange: (String) -> Unit = {},
    onSearchClose: () -> Unit = {},
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(SurfaceColor),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onBack
            ) {
                Text(
                    text = "← INDIETRO",
                    color = AccentColor,
                    fontWeight = FontWeight.Bold
                )
            }

            if (searchOpen) {
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .weight(1f)
                        .padding(
                            horizontal = 12.dp,
                            vertical = 7.dp
                        ),
                    singleLine = true,
                    placeholder = {
                        Text(
                            text =
                                if (title == "FILM") {
                                    "Cerca film..."
                                } else {
                                    "Cerca canale..."
                                },
                            color = Color.Gray
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor =
                            Color(0xFF202020),
                        unfocusedContainerColor =
                            Color(0xFF202020),
                        focusedTextColor =
                            Color.White,
                        unfocusedTextColor =
                            Color.White,
                        cursorColor =
                            AccentColor,
                        focusedIndicatorColor =
                            AccentColor,
                        unfocusedIndicatorColor =
                            Color.Transparent
                    ),
                    shape =
                        RoundedCornerShape(8.dp)
                )

                TextButton(
                    onClick = onSearchClose
                ) {
                    Text(
                        text = "CHIUDI",
                        color = AccentColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Text(
                    text = title,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                if (
                    title == "LIVE TV" ||
                    title == "FILM"
                ) {
                    TextButton(
                        onClick = onSearchClick
                    ) {
                        Text(
                            text = "🔍 CERCA",
                            color = AccentColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            CategorySidebar(
                categories = categories,
                selectedCategoryId =
                    selectedCategoryId,
                onCategorySelected =
                    onCategorySelected
            )

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
                    .background(BorderColor)
            )

            Box(
                modifier = Modifier
                    .fillMaxHeight()
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
            .fillMaxHeight()
            .background(Color(0xFF0D0D0D)),
        verticalArrangement =
            Arrangement.spacedBy(6.dp),
        contentPadding = PaddingValues(
            start = 12.dp,
            end = 12.dp,
            top = 16.dp,
            bottom = 20.dp
        )
    ) {
        item {
            SidebarCategory(
                name = "TUTTI",
                selected =
                    selectedCategoryId == null,
                onClick = {
                    onCategorySelected(null)
                }
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
            val id =
                category.category_id
                    ?: return@items

            SidebarCategory(
                name =
                    category.category_name
                        ?: "Categoria",
                selected =
                    selectedCategoryId == id,
                onClick = {
                    onCategorySelected(id)
                }
            )
        }
    }
}

@Composable
private fun SidebarCategory(
    name: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(8.dp)
            )
            .background(
                if (selected) {
                    AccentColor
                } else {
                    Color.Transparent
                }
            )
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
            maxLines = 1,
            overflow =
                TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun LiveChannelCard(
    channel: LiveStream,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(10.dp)
            )
            .background(SurfaceColor)
            .clickable {
                onClick()
            }
            .padding(10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(
                    RoundedCornerShape(8.dp)
                )
                .background(
                    Color(0xFF202020)
                ),
            contentAlignment =
                Alignment.Center
        ) {
            if (!channel.stream_icon.isNullOrBlank()) {
                AsyncImage(
                    model =
                        channel.stream_icon,
                    contentDescription =
                        channel.name,
                    modifier =
                        Modifier.fillMaxSize(),
                    contentScale =
                        ContentScale.Fit
                )
            } else {
                Text(
                    text = "LIVE",
                    color = AccentColor,
                    fontWeight =
                        FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        Text(
            text =
                channel.name ?: "Canale",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight =
                FontWeight.Bold,
            maxLines = 2,
            overflow =
                TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun MovieCard(
    movie: VodStream,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(10.dp)
            )
            .background(SurfaceColor)
            .clickable {
                onClick()
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.67f)
                .clip(
                    RoundedCornerShape(
                        topStart = 10.dp,
                        topEnd = 10.dp
                    )
                )
                .background(
                    Color(0xFF202020)
                )
        ) {
            if (!movie.stream_icon.isNullOrBlank()) {
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
            } else {
                Text(
                    text = "FILM",
                    modifier =
                        Modifier.align(
                            Alignment.Center
                        ),
                    color = AccentColor,
                    fontWeight =
                        FontWeight.Bold
                )
            }
        }

        Text(
            text =
                movie.name ?: "Film",
            modifier = Modifier.padding(
                horizontal = 10.dp,
                vertical = 10.dp
            ),
            color = Color.White,
            fontSize = 14.sp,
            fontWeight =
                FontWeight.Bold,
            maxLines = 2,
            overflow =
                TextOverflow.Ellipsis
        )
    }
}

@Composable
fun MovieDetailsScreen(
    movie: VodStream,
    onBack: () -> Unit,
    onPlay: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                BackgroundColor
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(
                    SurfaceColor
                ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onBack
            ) {
                Text(
                    text = "← INDIETRO",
                    color = AccentColor,
                    fontWeight =
                        FontWeight.Bold
                )
            }

            Text(
                text =
                    movie.name ?: "Film",
                modifier = Modifier.padding(
                    start = 8.dp
                ),
                color = Color.White,
                fontSize = 20.sp,
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
                .padding(30.dp),
            horizontalArrangement =
                Arrangement.spacedBy(30.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(240.dp)
                    .fillMaxHeight(0.8f)
                    .clip(
                        RoundedCornerShape(12.dp)
                    )
                    .background(
                        Color(0xFF202020)
                    )
            ) {
                if (
                    !movie.stream_icon
                        .isNullOrBlank()
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
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Text(
                    text =
                        movie.name ?: "Film",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight =
                        FontWeight.Bold
                )

                Spacer(
                    modifier =
                        Modifier.height(16.dp)
                )

                if (
                    !movie.rating
                        .isNullOrBlank()
                ) {
                    Text(
                        text =
                            "⭐ ${movie.rating}",
                        color =
                            AccentColor,
                        fontSize = 16.sp
                    )

                    Spacer(
                        modifier =
                            Modifier.height(12.dp)
                    )
                }

                Button(
                    onClick = {
                        val id =
                            movie.stream_id
                                ?: return@Button

                        onPlay(
                            "movie:$id:${movie.container_extension ?: "mp4"}"
                        )
                    },
                    colors =
                        ButtonDefaults
                            .buttonColors(
                                containerColor =
                                    AccentColor,
                                contentColor =
                                    Color.Black
                            )
                ) {
                    Text(
                        text =
                            "▶ RIPRODUCI",
                        fontWeight =
                            FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyContentMessage(
    message: String
) {
    Box(
        modifier =
            Modifier.fillMaxSize(),
        contentAlignment =
            Alignment.Center
    ) {
        Text(
            text = message,
            color = Color.Gray,
            fontSize = 16.sp
        )
    }
}
