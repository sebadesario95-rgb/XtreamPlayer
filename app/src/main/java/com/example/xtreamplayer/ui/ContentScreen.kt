package com.example.xtreamplayer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.xtreamplayer.data.LiveStream
import com.example.xtreamplayer.data.VodStream

@Composable
fun LiveContentScreen(
    title: String,
    streams: List<LiveStream>,
    onPlay: (LiveStream) -> Unit,
    onBack: () -> Unit
) {
    ContentHeader(
        title = title,
        onBack = onBack
    ) {
        if (streams.isEmpty()) {
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
                items(streams) { channel ->
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
}

@Composable
fun MovieContentScreen(
    title: String,
    movies: List<VodStream>,
    onPlay: (VodStream) -> Unit,
    onBack: () -> Unit
) {
    ContentHeader(
        title = title,
        onBack = onBack
    ) {
        if (movies.isEmpty()) {
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
                items(movies) { movie ->
                    MovieCard(
                        movie = movie,
                        onClick = {
                            onPlay(movie)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ContentHeader(
    title: String,
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090909))
            .padding(24.dp)
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
                modifier = Modifier.width(20.dp)
            )

            Text(
                text = title,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(
            modifier = Modifier.height(25.dp)
        )

        content()
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
                        .background(
                            Color(0xFF202020)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "NESSUNA IMMAGINE",
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
                        color = MaterialTheme
                            .colorScheme
                            .primary
                    )
                }
            }
        }
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
                        text = "TV",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme
                            .colorScheme
                            .primary
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
}

@Composable
private fun EmptyContentMessage(
    text: String
) {
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
}
