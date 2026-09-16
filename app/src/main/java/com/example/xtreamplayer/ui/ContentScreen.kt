package com.example.xtreamplayer.ui

import androidx.compose.foundation.ExperimentalFoundationApi
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
import com.example.xtreamplayer.data.LiveStream
import com.example.xtreamplayer.data.VodStream

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

val filteredStreams = remember(
    streams,
    selectedCategoryId
) {
    if (selectedCategoryId == null) {
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
    onBack = onBack
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(150.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
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

val filteredMovies = remember(
    movies,
    selectedCategoryId
) {
    if (selectedCategoryId == null) {
        movies
    } else {
        movies.filter {
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
                onClick = {
                    onMovieClick(movie)
                }
            )
        }
    }
}

}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun ContentWithSidebar(
title: String,
categories: List<Category>,
selectedCategoryId: String?,
onCategorySelected: (String?) -> Unit,
onBack: () -> Unit,
content: @Composable () -> Unit
) {
Scaffold(
topBar = {
TopAppBar(
title = {
Text(
title,
fontWeight = FontWeight.Bold
)
},
navigationIcon = {
TextButton(
onClick = onBack
) {
Text("← INDIETRO")
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

        CategorySidebar(
            categories = categories,
            selectedCategoryId = selectedCategoryId,
            onCategorySelected = onCategorySelected
        )

        Box(
modifier = Modifier
.fillMaxHeight()
.width(1.dp)
.background(Color(0xFF292929))
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

}

@Composable
private fun SidebarCategory(
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
        .clip(
            RoundedCornerShape(8.dp)
        )
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
        fontWeight = if (selected) {
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
private fun LiveChannelCard(
channel: LiveStream,
onClick: () -> Unit
) {
Card(
modifier = Modifier
.fillMaxWidth()
.height(150.dp)
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
            model = channel.stream_icon,
            contentDescription = channel.name,
            modifier = Modifier
                .fillMaxWidth()
                .height(105.dp),
            contentScale = ContentScale.Fit
        )

        Text(
            text = channel.name ?: "Canale",
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 10.dp,
                    vertical = 7.dp
                ),
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

}

@Composable
private fun MovieCard(
movie: VodStream,
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
}

}

@Composable
fun MovieDetailsScreen(
movie: VodStream,
onPlay: () -> Unit,
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
Text("← INDIETRO")
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
            .padding(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        AsyncImage(
            model = movie.stream_icon,
            contentDescription = movie.name,
            modifier = Modifier
                .width(260.dp)
                .height(360.dp)
                .clip(
                    RoundedCornerShape(12.dp)
                ),
            contentScale = ContentScale.Crop
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Text(
            text = movie.name ?: "Film",
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Button(
            onClick = onPlay,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFCAEA00),
                contentColor = Color.Black
            )
        ) {
            Text(
                "▶ RIPRODUCI",
                fontWeight = FontWeight.Bold
            )
        }
    }
}

}

@Composable
private fun EmptyContentMessage(
message: String
) {
Box(
modifier = Modifier.fillMaxSize(),
contentAlignment = Alignment.Center
) {
Text(
text = message,
color = Color.Gray,
fontSize = 16.sp
)
}
}
