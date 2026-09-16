package com.example.xtreamplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.xtreamplayer.data.VodStream
import com.example.xtreamplayer.ui.LoginScreen
import com.example.xtreamplayer.ui.HomeScreen
import com.example.xtreamplayer.ui.PlayerScreen
import com.example.xtreamplayer.viewmodel.AppViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
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

                if (playingUrl != null) {

                    PlayerScreen(
                        url = playingUrl!!,
                        onBack = {
                            /*
                             * NON cancelliamo playingMovie.
                             *
                             * Quando torniamo indietro dal player,
                             * HomeScreen riceverà il film e riaprirà
                             * direttamente i suoi dettagli.
                             */
                            playingUrl = null
                        }
                    )

                } else if (vm.loggedIn) {

                    HomeScreen(
                        vm = vm,

                        onPlay = { url ->
                            // Live TV / Serie TV
                            playingMovie = null
                            playingUrl = url
                        },

                        onMoviePlay = { url, movie ->
                            // Film: conserviamo anche il film corrente
                            playingMovie = movie
                            playingUrl = url
                        },

                        initialMovie = playingMovie,

                        onInitialMovieConsumed = {
                            /*
                             * Una volta ricreato HomeScreen con il film,
                             * possiamo cancellare il riferimento dal livello
                             * superiore. HomeScreen avrà già il suo stato locale.
                             */
                            playingMovie = null
                        }
                    )

                } else {

                    LoginScreen(vm = vm)

                }
            }
        }
    }
}
