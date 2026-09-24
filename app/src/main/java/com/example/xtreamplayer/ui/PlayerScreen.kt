package com.example.xtreamplayer.ui

import android.net.Uri
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
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
import java.util.WeakHashMap

private val PlayerFocusBlue = Color(0xFF1677FF)

private const val PLAYER_LED_STROKE_PX = 4

/*
 * Applica un LED cyan ai controlli Android nativi del PlayerView
 * senza sostituire layout, controller o logica Media3.
 *
 * Usiamo il foreground della View focalizzata e lo ripristiniamo
 * appena il focus si sposta. In questo modo play/pausa, seek e gli
 * altri controlli continuano a essere quelli originali Media3.
 */
private fun installMedia3TvFocusLed(playerView: PlayerView) {
    val originalForegrounds =
        WeakHashMap<View, Drawable?>()

    var lastFocusedView: View? = null

    fun isInsidePlayer(view: View?): Boolean {
        var current = view

        while (current != null) {
            if (current === playerView) {
                return true
            }

            current =
                (current.parent as? View)
        }

        return false
    }

    fun restore(view: View?) {
        if (view == null) return

        if (originalForegrounds.containsKey(view)) {
            view.foreground =
                originalForegrounds.remove(view)
        }

        view.scaleX = 1f
        view.scaleY = 1f
    }

    fun highlight(view: View?) {
        if (view == null) return
        if (view === playerView) return
        if (!isInsidePlayer(view)) return

        originalForegrounds.putIfAbsent(
            view,
            view.foreground
        )

        view.foreground =
            GradientDrawable().apply {
                shape =
                    GradientDrawable.RECTANGLE

                setColor(
                    android.graphics.Color.TRANSPARENT
                )

                setStroke(
                    PLAYER_LED_STROKE_PX,
                    android.graphics.Color.rgb(
                        22,
                        119,
                        255
                    )
                )

                cornerRadius = 18f
            }

        view.scaleX = 1.08f
        view.scaleY = 1.08f
    }

    val focusListener =
        ViewTreeObserver.OnGlobalFocusChangeListener {
                oldFocus,
                newFocus ->

            if (
                oldFocus === lastFocusedView ||
                isInsidePlayer(oldFocus)
            ) {
                restore(oldFocus)
            }

            if (isInsidePlayer(newFocus)) {
                highlight(newFocus)
                lastFocusedView = newFocus
            } else {
                lastFocusedView = null
            }
        }

    playerView.viewTreeObserver
        .addOnGlobalFocusChangeListener(
            focusListener
        )

    playerView.addOnAttachStateChangeListener(
        object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(
                v: View
            ) = Unit

            override fun onViewDetachedFromWindow(
                v: View
            ) {
                restore(lastFocusedView)

                if (
                    playerView.viewTreeObserver
                        .isAlive
                ) {
                    playerView.viewTreeObserver
                        .removeOnGlobalFocusChangeListener(
                            focusListener
                        )
                }

                playerView
                    .removeOnAttachStateChangeListener(
                        this
                    )
            }
        }
    )
}

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

                    /*
                     * LED TV anche sui controlli nativi Media3.
                     * Non cambiamo il controller: osserviamo soltanto
                     * quale View riceve il focus dal telecomando.
                     */
                    installMedia3TvFocusLed(this)

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
