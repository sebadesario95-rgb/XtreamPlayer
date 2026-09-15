package com.example.xtreamplayer.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.xtreamplayer.viewmodel.AppViewModel

@Composable
fun HomeScreen(vm: AppViewModel, onPlay: (String) -> Unit) {
    val account = vm.auth?.user_info

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("XTREAM PLAYER") },
                actions = { TextButton(onClick = vm::logout) { Text("LOGOUT") } }
            )
        }
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).padding(16.dp)) {
            Text("Benvenuto ${account?.username ?: ""}", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(24.dp))

            Text("LIVE TV", style = MaterialTheme.typography.titleLarge)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(vm.live.take(20)) { channel ->
                    Card(
                        Modifier.width(180.dp).height(90.dp)
                            .clickable {
                                channel.stream_id?.let { id ->
                                    vm.streamUrl("live", id)?.let(onPlay)
                                }
                            }
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(channel.name ?: "Canale")
                            Text("LIVE", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text("MOVIES", style = MaterialTheme.typography.titleLarge)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(vm.movies.take(20)) { movie ->
                    Card(
                        Modifier.width(180.dp).height(90.dp)
                            .clickable {
                                movie.stream_id?.let { id ->
                                    vm.streamUrl("movie", id, movie.container_extension)?.let(onPlay)
                                }
                            }
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(movie.name ?: "Film")
                            Text(movie.rating ?: "", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text("SERIES", style = MaterialTheme.typography.titleLarge)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(vm.series.take(20)) { show ->
                    Card(Modifier.width(180.dp).height(90.dp)) {
                        Column(Modifier.padding(12.dp)) {
                            Text(show.name ?: "Serie")
                            Text(show.genre ?: "", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
