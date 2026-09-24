package com.example.xtreamplayer.ui

import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.view.View
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay
import java.util.WeakHashMap

private val PlayerFocusBlue = Color(0xFF1677FF)

private const val PLAYER_LED_STROKE_PX = 4
private const val PLAYER_PROGRESS_PREFS = "future_smart_playback_progress"
private const val PLAYER_PROGRESS_PREFIX = "position_"
private const val PLAYER_MIN_RESUME_MS = 15_000L
private const val PLAYER_FINISHED_REMAINING_MS = 30_000L
private const val PLAYER_FINISHED_PERCENT = 0.95

/*
 * Applica un LED cyan ai controlli Android nativi del PlayerView
 * senza sostituire layout, controller o logica Media3.
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

    val progressPrefs = remember {
        context.getSharedPreferences(
            PLAYER_PROGRESS_PREFS,
            android.content.Context.MODE_PRIVATE
        )
    }

    /*
     * STEP 2:
     * ricaviamo dalla URL Xtream il tipo reale e lo stream ID.
     *
     * Esempi:
     * /movie/utente/password/12345.mp4  -> movie:12345
     * /series/utente/password/67890.mp4 -> series:67890
     *
     * In questo modo la memoria non dipende più da dominio,
     * protocollo, username/password o estensione del file.
     *
     * LIVE viene escluso: "Continua la visione" è solo FILM/SERIE.
     */
    val stableContentKey = remember(url) {
        runCatching {
            val uri = Uri.parse(url)
            val segments = uri.pathSegments

            val type =
                when {
                    segments.contains("movie") ->
                        "movie"

                    segments.contains("series") ->
                        "series"

                    else ->
                        null
                }

            val id =
                segments.lastOrNull()
                    ?.substringBeforeLast(".")
                    ?.takeIf { value ->
                        value.isNotBlank()
                    }

            if (type != null && id != null) {
                "$type:$id"
            } else {
                null
            }
        }.getOrNull()
    }

    val progressKey = remember(stableContentKey) {
        stableContentKey?.let {
            PLAYER_PROGRESS_PREFIX + it
        }
    }

    val savedPosition = remember(progressKey) {
        progressKey?.let {
            progressPrefs.getLong(
                it,
                0L
            )
        } ?: 0L
    }

    var controlsVisible by remember {
        mutableStateOf(false)
    }

    var backButtonFocused by remember {
        mutableStateOf(false)
    }

    /*
     * Riferimento al PlayerView nativo.
     * Serve al contenitore Compose per mostrare direttamente
     * i controlli Media3 quando arriva OK dal telecomando.
     */
    var nativePlayerView by remember {
        mutableStateOf<PlayerView?>(null)
    }

    var resumeChoiceVisible by remember(savedPosition) {
        mutableStateOf(
            savedPosition >= PLAYER_MIN_RESUME_MS
        )
    }

    var playbackStarted by remember(savedPosition) {
        mutableStateOf(
            savedPosition < PLAYER_MIN_RESUME_MS
        )
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

                /*
                 * Se esiste una posizione salvata valida aspettiamo
                 * la scelta dell'utente prima di avviare.
                 */
                playWhenReady =
                    savedPosition < PLAYER_MIN_RESUME_MS
            }
    }

    fun clearSavedProgress() {
        val key =
            progressKey
                ?: return

        progressPrefs
            .edit()
            .remove(key)
            .apply()
    }

    fun saveCurrentProgress() {
        val key =
            progressKey
                ?: return

        val position =
            player.currentPosition
                .coerceAtLeast(0L)

        val duration =
            player.duration

        if (position < PLAYER_MIN_RESUME_MS) {
            clearSavedProgress()
            return
        }

        val hasValidDuration =
            duration > 0L &&
                duration != androidx.media3.common.C.TIME_UNSET

        val almostFinished =
            hasValidDuration &&
                (
                    duration - position <=
                        PLAYER_FINISHED_REMAINING_MS ||
                        position.toDouble() /
                            duration.toDouble() >=
                        PLAYER_FINISHED_PERCENT
                )

        if (almostFinished) {
            clearSavedProgress()
        } else {
            progressPrefs
                .edit()
                .putLong(
                    key,
                    position
                )
                .apply()
        }
    }

    /*
     * Salvataggio periodico mentre il contenuto è in riproduzione.
     * SharedPreferences rende la posizione persistente anche dopo
     * chiusura dell'app o riavvio del dispositivo.
     */
    LaunchedEffect(
        player,
        playbackStarted
    ) {
        while (playbackStarted) {
            delay(5_000L)

            if (
                player.playbackState !=
                    Player.STATE_ENDED
            ) {
                saveCurrentProgress()
            }
        }
    }

    DisposableEffect(player) {
        onDispose {
            if (
                player.playbackState ==
                    Player.STATE_ENDED
            ) {
                clearSavedProgress()
            } else {
                saveCurrentProgress()
            }

            player.release()
        }
    }

    /*
     * BACK fisico SACRO:
     * se è aperta la scelta "continua/riparti", BACK chiude il player
     * e torna alla schermata precedente.
     * Durante la visione salva prima la posizione corrente.
     */
    BackHandler {
        if (playbackStarted) {
            saveCurrentProgress()
        }

        onBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            /*
             * FIRE TV:
             * intercettiamo OK a livello della schermata intera, PRIMA
             * che l'evento venga consegnato al focus corrente.
             *
             * Se i controlli sono nascosti li mostriamo direttamente.
             * Se sono già visibili restituiamo false, quindi Media3
             * continua a gestire normalmente Play/Pausa, seek, ecc.
             *
             * Durante la scelta Continua/Riparti non intercettiamo nulla:
             * i due pulsanti Compose mantengono il loro normale OK.
             */
            .onPreviewKeyEvent { event ->
                val isConfirmKey =
                    event.key == Key.DirectionCenter ||
                        event.key == Key.Enter ||
                        event.key == Key.NumPadEnter

                if (
                    !resumeChoiceVisible &&
                    event.type == KeyEventType.KeyDown &&
                    isConfirmKey &&
                    nativePlayerView?.isControllerFullyVisible == false
                ) {
                    nativePlayerView?.showController()
                    true
                } else {
                    false
                }
            }
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
                     * Conserviamo il riferimento. L'OK viene gestito
                     * dal contenitore Compose sopra, quindi non dipendiamo
                     * dal focus interno dei controlli Media3.
                     */
                    nativePlayerView = this

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

        if (
            controlsVisible &&
            !resumeChoiceVisible
        ) {

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
                        saveCurrentProgress()
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

        if (resumeChoiceVisible) {
            ResumePlaybackChoice(
                onContinue = {
                    player.seekTo(savedPosition)
                    player.playWhenReady = true
                    player.play()

                    playbackStarted = true
                    resumeChoiceVisible = false

                    nativePlayerView?.post {
                        nativePlayerView?.requestFocus()
                    }
                },
                onRestart = {
                    clearSavedProgress()

                    player.seekTo(0L)
                    player.playWhenReady = true
                    player.play()

                    playbackStarted = true
                    resumeChoiceVisible = false

                    nativePlayerView?.post {
                        nativePlayerView?.requestFocus()
                    }
                }
            )
        }
    }
}

@Composable
private fun ResumePlaybackChoice(
    onContinue: () -> Unit,
    onRestart: () -> Unit
) {
    val continueFocusRequester =
        remember {
            FocusRequester()
        }

    var continueFocused by remember {
        mutableStateOf(false)
    }

    var restartFocused by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        continueFocusRequester.requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color.Black.copy(
                    alpha = 0.78f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.width(620.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF07111F).copy(
                alpha = 0.98f
            ),
            shadowElevation = 18.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 34.dp,
                        vertical = 32.dp
                    ),
                horizontalAlignment =
                    Alignment.CenterHorizontally,
                verticalArrangement =
                    Arrangement.spacedBy(18.dp)
            ) {
                Text(
                    text = "VUOI CONTINUARE LA VISIONE?",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                ResumeChoiceButton(
                    text =
                        "CONTINUA DA DOVE ERI RIMASTO",
                    focused = continueFocused,
                    modifier = Modifier
                        .focusRequester(
                            continueFocusRequester
                        )
                        .onFocusChanged {
                            continueFocused =
                                it.isFocused
                        },
                    onClick = onContinue
                )

                ResumeChoiceButton(
                    text =
                        "RIPRODUCI DALL’INIZIO",
                    focused = restartFocused,
                    modifier = Modifier
                        .onFocusChanged {
                            restartFocused =
                                it.isFocused
                        },
                    onClick = onRestart
                )
            }
        }
    }
}

@Composable
private fun ResumeChoiceButton(
    text: String,
    focused: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .scale(
                if (focused) {
                    1.025f
                } else {
                    1f
                }
            )
            .border(
                width =
                    if (focused) {
                        3.dp
                    } else {
                        1.dp
                    },
                color =
                    if (focused) {
                        PlayerFocusBlue
                    } else {
                        Color(0xFF26364B)
                    },
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(
                onClick = onClick
            )
            .focusable(),
        shape = RoundedCornerShape(14.dp),
        color =
            if (focused) {
                PlayerFocusBlue.copy(
                    alpha = 0.28f
                )
            } else {
                Color(0xFF0B1726)
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 24.dp,
                    vertical = 17.dp
                ),
            contentAlignment =
                Alignment.Center
        ) {
            Text(
                text = text,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}
