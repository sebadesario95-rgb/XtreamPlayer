package com.example.xtreamplayer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.xtreamplayer.data.Category
import com.example.xtreamplayer.data.LiveStream
import com.example.xtreamplayer.data.VodStream

private val AccentGreen = Color(0xFFCAEA00)

@Composable
fun LiveContentScreen(
title: String,
categories: List<Category>,
streams: List<LiveStream>,
onPlay: (LiveStream) -> Unit,
onBack: () -> Unit
) {
var selectedCategoryId by remember {
mutableStateOf<String?>(null)
}

```
val filteredStreams =
    if (selectedCategoryId == null) {
        streams
    } else {
        streams.filter {
            it.category_id == selectedCategoryId
        }
    }

ContentWithSidebar(
    title = title,
    categories = categories,
    selectedCategoryId = selectedCategoryId,
    onCategorySelected = {
        selectedCategoryId = it
    },
    onBack = onBack
) {

    if (filteredStreams.isEmpty()) {

        EmptyContentMessage(
            text = "Nessun canale disponibile."
        )

    } else {

        LazyVerticalGrid(
            columns = GridCells.Adaptive(
                minSize = 180.dp
            ),
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            contentPadding = PaddingValues(
                bottom = 30.dp
            )
        ) {

            items(filteredStreams) { channel ->

                LiveChannelCard(
                    channel = channel,
                    onClick = {
                        onPlay(channel)
                    }
                )
            }
        }
    }
}
```

}

@Composable
fun MovieContentScreen(
title: String,
categories: List<Category>,
movies: List<VodStream>,
onMovieClick: (VodStream) -> Unit,
onBack: () -> Unit
) {
var selectedCategoryId by remember {
mutableStateOf<String?>(null)
}

```
val filteredMovies =
    if (selectedCategoryId == null) {
        movies
    } else {
        movies.filter {
            it.category_id == selectedCategoryId
        }
    }

ContentWithSidebar(
    title = title,
    categories = categories,
    selectedCategoryId = selectedCategoryId,
    onCategorySelected = {
        selectedCategoryId = it
    },
    onBack = onBack
) {

    if (filteredMovies.isEmpty()) {

        EmptyContentMessage(
            text = "Nessun film disponibile."
        )

    } else {

        LazyVerticalGrid(
            columns = GridCells.Adaptive(
                minSize = 170.dp
            ),
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            contentPadding = PaddingValues(
                bottom = 30.dp
            )
        ) {

            items(filteredMovies) { movie ->

                MovieCard(
                    movie = movie,
                    onClick = {
                        onMovieClick(movie)
                    }
                )
            }
        }
    }
}
```

}

@Composable
private fun ContentWithSidebar(
title: String,
categories: List<Category>,
selectedCategoryId: String?,
onCategorySelected: (String?) -> Unit,
onBack: () -> Unit,
content: @Composable () -> Unit
) {

```
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

        CategorySidebar(
            categories = categories,
            selectedCategoryId = selectedCategoryId,
            onCategorySelected = onCategorySelected
        )

        Spacer(
            modifier = Modifier.width(22.dp)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            content()
        }
    }
}
```

}

@Composable
private fun CategorySidebar(
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

        SidebarCategory(
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

        SidebarCategory(
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
private fun SidebarCategory(
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
                AccentGreen
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
fun MovieDetailsScreen(
movie: VodStream,
onPlay: () -> Unit,
onBack: () -> Unit
) {

```
Column(
    modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFF090909))
        .padding(24.dp)
) {

    TextButton(
        onClick = onBack
    ) {
        Text("← INDIETRO")
    }

    Spacer(
        modifier = Modifier.height(20.dp)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(28.dp)
    ) {

        if (!movie.stream_icon.isNullOrBlank()) {

            AsyncImage(
                model = movie.stream_icon,
                contentDescription = movie.name,
                modifier = Modifier
                    .width(280.dp)
                    .height(410.dp)
                    .clip(
                        RoundedCornerShape(14.dp)
                    ),
                contentScale = ContentScale.Crop
            )

        } else {

            Box(
                modifier = Modifier
                    .width(280.dp)
                    .height(410.dp)
                    .clip(
                        RoundedCornerShape(14.dp)
                    )
                    .background(Color(0xFF202020)),
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
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = movie.name ?: "Film",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            if (!movie.rating.isNullOrBlank()) {

                Text(
                    text = "★ ${movie.rating}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentGreen
                )

                Spacer(
                    modifier = Modifier.height(18.dp)
                )
            }

            Text(
                text = "FILM",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
            )

            Spacer(
                modifier = Modifier.height(25.dp)
            )

            Button(
                onClick = onPlay
            ) {
                Text(
                    "▶  RIPRODUCI",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
```

}

@Composable
private fun MovieCard(
movie: VodStream,
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

        if (!movie.stream_icon.isNullOrBlank()) {

            AsyncImage(
                model = movie.stream_icon,
                contentDescription = movie.name,
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
                    .background(Color(0xFF202020)),
                contentAlignment = Alignment.Center
            ) {

                Text(
                    "NESSUNA IMMAGINE",
                    fontSize = 12.sp,
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
                text = movie.name ?: "Film",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2
            )

            if (!movie.rating.isNullOrBlank()) {

                Spacer(
                    modifier = Modifier.height(5.dp)
                )

                Text(
                    text = "★ ${movie.rating}",
                    fontSize = 13.sp,
                    color = AccentGreen
                )
            }
        }
    }
}
```

}

@Composable
private fun LiveChannelCard(
channel: LiveStream,
onClick: () -> Unit
) {

```
Card(
    modifier = Modifier
        .fillMaxWidth()
        .height(180.dp)
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

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        if (!channel.stream_icon.isNullOrBlank()) {

            AsyncImage(
                model = channel.stream_icon,
                contentDescription = channel.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(15.dp),
                contentScale = ContentScale.Fit
            )

        } else {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {

                Text(
                    "TV",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentGreen
                )
            }
        }

        Text(
            text = channel.name ?: "Canale",
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 14.dp,
                    vertical = 12.dp
                ),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
```

}

@Composable
private fun EmptyContentMessage(
text: String
) {

```
Box(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.Center
) {

    Text(
        text = text,
        color = MaterialTheme
            .colorScheme
            .onSurfaceVariant
    )
}
```

}
