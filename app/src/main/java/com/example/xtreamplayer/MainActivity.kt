
package com.example.xtreamplayer

import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.xtreamplayer.data.LiveStream
import com.example.xtreamplayer.data.SeriesStream
import com.example.xtreamplayer.data.VodStream
import com.example.xtreamplayer.ui.CatalogLoadingScreen
import com.example.xtreamplayer.ui.HomeScreen
import com.example.xtreamplayer.ui.LoginScreen
import com.example.xtreamplayer.ui.PlayerScreen
import com.example.xtreamplayer.viewmodel.AppViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private var fullscreenOnPhone = false

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        // Fullscreen immersivo SOLO su smartphone/tablet.
        // Sui dispositivi TV manteniamo il comportamento originale.
        val uiModeManager =
            getSystemService(Context.UI_MODE_SERVICE) as UiModeManager

        fullscreenOnPhone =
            uiModeManager.currentModeType !=
                Configuration.UI_MODE_TYPE_TELEVISION

        if (fullscreenOnPhone) {
            enablePhoneFullscreen()
        }

        setContent {

            MaterialTheme(

                colorScheme = darkColorScheme(
                    background = Color(0xFF090909),
                    surface = Color(0xFF151515),
                    primary = Color(0xFF1677FF)
                )
            ) {

                val vm: AppViewModel =
                    viewModel()

                // Tiene visibile la schermata di sincronizzazione
                // abbastanza a lungo da mostrare realmente il 100%.
                var showCatalogSync by remember {
                    mutableStateOf(false)
                }

                LaunchedEffect(
                    vm.catalogSyncActive,
                    vm.catalogSyncProgress
                ) {
                    if (vm.catalogSyncActive) {
                        showCatalogSync = true
                    } else if (
                        showCatalogSync &&
                        vm.catalogSyncProgress >= 100
                    ) {
                        delay(700L)
                        showCatalogSync = false
                    } else if (!vm.loggedIn) {
                        showCatalogSync = false
                    }
                }

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
                // SINCRONIZZAZIONE CATALOGO
                // -----------------------------------------

                else if (
                    vm.catalogSyncActive ||
                    showCatalogSync
                ) {

                    CatalogLoadingScreen(
                        vm = vm
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

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (hasFocus && fullscreenOnPhone) {
            enablePhoneFullscreen()
        }
    }

    private fun enablePhoneFullscreen() {

        WindowCompat.setDecorFitsSystemWindows(
            window,
            false
        )

        val controller =
            WindowInsetsControllerCompat(
                window,
                window.decorView
            )

        controller.hide(
            WindowInsetsCompat.Type.statusBars() or
                WindowInsetsCompat.Type.navigationBars()
        )

        controller.systemBarsBehavior =
            WindowInsetsControllerCompat
                .BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
