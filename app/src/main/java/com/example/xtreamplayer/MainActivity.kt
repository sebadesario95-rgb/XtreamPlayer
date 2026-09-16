package com.example.xtreamplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.xtreamplayer.data.LiveStream
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

                val vm: AppViewModel =
                    viewModel()

                // -----------------------------------------
                // PLAYER
                // -----------------------------------------

                var playingUrl by remember {
                    mutableStateOf<String?>(null)
                }

                // -----------------------------------------
                // FILM
                // -----------------------------------------

                var playingMovie by remember {
                    mutableStateOf<VodStream?>(null)
                }

                // -----------------------------------------
                // SERIE
                // -----------------------------------------

                var playingSeries by remember {
                    mutableStateOf<SeriesStream?>(null)
                }

                var playingSeason by remember {
                    mutableStateOf<String?>(null)
                }

                // -----------------------------------------
                // LIVE TV
                // -----------------------------------------

                var playingLive by remember {
                    mutableStateOf<LiveStream?>(null)
                }

                // -----------------------------------------
                // PLAYER
                // -----------------------------------------

                if (playingUrl != null) {

                    PlayerScreen(

                        url = playingUrl!!,

                        onBack = {

                            playingUrl = null

                            // Non tocchiamo playingLive:
                            // serve per sapere che dobbiamo
                            // tornare dentro LIVE TV.

                        }
                    )

                }

                // -----------------------------------------
                // HOME / CATALOG
                // -----------------------------------------

                else if (vm.loggedIn) {

                    HomeScreen(

                        vm = vm,

                        // ---------------------------------
                        // GENERIC PLAY
                        // ---------------------------------

                        onPlay = { url ->

                            playingMovie = null
                            playingSeries = null
                            playingSeason = null
                            playingLive = null

                            playingUrl = url
                        },

                        // ---------------------------------
                        // MOVIE
                        // ---------------------------------

                        onMoviePlay = { url, movie ->

                            playingMovie = movie

                            playingSeries = null
                            playingSeason = null
                            playingLive = null

                            playingUrl = url
                        },

                        // ---------------------------------
                        // SERIES
                        // ---------------------------------

                        onSeriesPlay = {
                                url,
                                series,
                                season ->

                            playingMovie = null

                            playingSeries = series
                            playingSeason = season

                            playingLive = null

                            playingUrl = url
                        },

                        // ---------------------------------
                        // LIVE TV
                        // ---------------------------------

                        onLivePlay = {
                                url,
                                live ->

                            playingMovie = null

                            playingSeries = null
                            playingSeason = null

                            // Salviamo il canale che stava
                            // guardando l'utente.
                            playingLive = live

                            playingUrl = url
                        },

                        // ---------------------------------
                        // RESTORE MOVIE
                        // ---------------------------------

                        initialMovie = playingMovie,

                        // ---------------------------------
                        // RESTORE SERIES
                        // ---------------------------------

                        initialSeries = playingSeries,

                        initialSeason = playingSeason,

                        // ---------------------------------
                        // RESTORE LIVE
                        // ---------------------------------

                        initialLive = playingLive,

                        // ---------------------------------
                        // CONSUME MOVIE STATE
                        // ---------------------------------

                        onInitialMovieConsumed = {

                            playingMovie = null
                        },

                        // ---------------------------------
                        // CONSUME SERIES STATE
                        // ---------------------------------

                        onInitialSeriesConsumed = {

                            playingSeries = null
                            playingSeason = null
                        },

                        // ---------------------------------
                        // CONSUME LIVE STATE
                        // ---------------------------------

                        onInitialLiveConsumed = {

                            playingLive = null
                        }
                    )
                }

                // -----------------------------------------
                // LOGIN
                // -----------------------------------------

                else {

                    LoginScreen(
                        vm = vm
                    )
                }
            }
        }
    }
}
