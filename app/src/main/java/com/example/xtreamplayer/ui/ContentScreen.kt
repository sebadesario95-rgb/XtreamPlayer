package com.example.xtreamplayer.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {

            items(streams) { channel ->

                ContentCard(
                    title = channel.name ?: "Canale",
                    subtitle = "LIVE",
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
    movies: List<VodStream>,
    onPlay: (VodStream) -> Unit,
    onBack: () -> Unit
) {

    ContentHeader(
        title = title,
        onBack = onBack
    ) {

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {

            items(movies) { movie ->

                ContentCard(
                    title = movie.name ?: "Film",
                    subtitle = movie.rating ?: "",
                    onClick = {
                        onPlay(movie)
                    }
                )
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
            .padding(24.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth()
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
                modifier = Modifier.padding(top = 8.dp),
                fontSize = 26.sp,
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
private fun ContentCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(78.dp)
            .clickable {
                onClick()
            },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF151515)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 5.dp
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = 20.dp,
                    vertical = 14.dp
                ),
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            if (subtitle.isNotBlank()) {

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
