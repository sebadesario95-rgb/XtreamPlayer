package com.example.xtreamplayer.ui

import android.net.Uri
import android.view.View
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

private val PlayerFocusBlue = Color(0xFF1677FF)

@Composable
fun PlayerScreen(
    url: String,
    onBack: () -> Unit
) {
    val context =
        androidx.compose.ui.platform.LocalContext.current

    var controlsVisible by remember {
        mutableStateOf(false)
    }

    var backButtonFocused by remember {
        mutableStateOf(false)
    }

    val player = remember(url) {
        ExoPlayer.Builder(context)
            .build()
            .apply {
                setMediaItem(
                    MediaItem.fromUri(
                        Uri.parse(url)
                    )
                )

                prepare()
                playWhenReady = true
            }
    }

    DisposableEffect(player) {
        onDispose {
            player.release()
        }
    }

    /*
     * BACK fisico SACRO:
     * continua a tornare esattamente alla schermata precedente.
     */
    BackHandler {
        onBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->

                PlayerView(ctx).apply {
                    this.player = player

                    useController = true

                    controllerShowTimeoutMs = 3000

                    controllerAutoShow = true

                    setControllerVisibilityListener(
                        PlayerView.ControllerVisibilityListener { visibility ->

                            controlsVisible =
                                visibility == View.VISIBLE
                        }
                    )

                    layoutParams =
                        FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT
                        )
                }
            },
            update = {
                it.player = player
            }
        )

        /*
         * Il pulsante BACK Compose resta sincronizzato con la visibilità
         * dei controlli Media3, come nella versione funzionante.
         *
         * Aggiungiamo soltanto il feedback TV:
         * quando il telecomando gli assegna il focus compare il LED cyan.
         */
        if (controlsVisible) {

            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(
                        start = 20.dp,
                        top = 20.dp
                    )
                    .size(46.dp)
                    .scale(
                        if (backButtonFocused) {
                            1.08f
                        } else {
                            1f
                        }
                    )
                    .onFocusChanged {
                        backButtonFocused =
                            it.isFocused
                    }
                    .border(
                        width =
                            if (backButtonFocused) {
                                3.dp
                            } else {
                                0.dp
                            },
                        color =
                            if (backButtonFocused) {
                                PlayerFocusBlue
                            } else {
                                Color.Transparent
                            },
                        shape = CircleShape
                    )
                    .clickable {
                        onBack()
                    }
                    .focusable(),
                shape = CircleShape,
                color =
                    if (backButtonFocused) {
                        PlayerFocusBlue.copy(
                            alpha = 0.30f
                        )
                    } else {
                        Color.Black.copy(
                            alpha = 0.68f
                        )
                    }
            ) {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "‹",
                        color = Color.White,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Light
                    )
                }
            }
        }
    }
}
