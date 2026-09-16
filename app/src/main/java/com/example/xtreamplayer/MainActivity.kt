package com.example.xtreamplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.xtreamplayer.data.SeriesStream
import com.example.xtreamplayer.data.VodStream
import com.example.xtreamplayer.ui.HomeScreen
import com.example.xtreamplayer.ui.LoginScreen
import com.example.xtreamplayer.ui.PlayerScreen
import com.example.xtreamplayer.viewmodel.AppViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        setContent {

            MaterialTheme(
                colorScheme = darkColorScheme(
                    background = Color(0xFF090909),
                    surface = Color(0xFF151515),
                    primary = Color(0xFFCAEA00)
                )
            ) {

                val vm: AppViewModel = viewModel()

                var playingUrl by remember {
                    mutableStateOf<String?>(null)
                }

                var playingMovie by remember {
                    mutableStateOf<VodStream?>(null)
                }

                var playingSeries by remember {
                    mutableStateOf<SeriesStream?>(null)
                }

                var playingSeason by remember {
                    mutableStateOf<String?>(null)
                }

                if (playingUrl != null) {

                    PlayerScreen(
                        url = playingUrl!!,

                        onBack = {
                            playingUrl = null
                        }
                    )

                } else if (vm.loggedIn) {

                    HomeScreen(

                        vm = vm,

                        onPlay = { url ->

                            playingMovie = null
                            playingSeries = null
                            playingSeason = null

                            playingUrl = url
                        },

                        onMoviePlay = { url, movie ->

                            playingMovie = movie

                            playingSeries = null
                            playingSeason = null

                            playingUrl = url
                        },

                        onSeriesPlay = { url, series, season ->

                            playingMovie = null

                            playingSeries = series
                            playingSeason = season

                            playingUrl = url
                        },

                        initialMovie = playingMovie,

                        initialSeries = playingSeries,

                        initialSeason = playingSeason,

                        onInitialMovieConsumed = {
                            playingMovie = null
                        },

                        onInitialSeriesConsumed = {
                            playingSeries = null
                            playingSeason = null
                        }
                    )

                } else {

                    LoginScreen(
                        vm = vm
                    )
                }
            }
        }
    }
}
