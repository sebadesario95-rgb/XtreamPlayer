package com.example.xtreamplayer.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.xtreamplayer.data.*
import com.example.xtreamplayer.player.XtreamStreamUrlBuilder
import kotlinx.coroutines.launch

class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val store = CredentialStore(app)

    var loggedIn by mutableStateOf(false)
        private set

    var loading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    var credentials: Credentials? = store.load()
        private set

    var auth: AuthResponse? by mutableStateOf(null)
        private set

    // =========================
    // CATEGORIE
    // =========================

    var liveCategories by mutableStateOf<List<Category>>(emptyList())
        private set

    var movieCategories by mutableStateOf<List<Category>>(emptyList())
        private set

    var seriesCategories by mutableStateOf<List<Category>>(emptyList())
        private set

    // =========================
    // CONTENUTI
    // =========================

    var live by mutableStateOf<List<LiveStream>>(emptyList())
        private set

    var movies by mutableStateOf<List<VodStream>>(emptyList())
        private set

    var series by mutableStateOf<List<SeriesStream>>(emptyList())
        private set

    // =========================
    // DETTAGLI SERIE
    // =========================

    var selectedSeriesInfo by mutableStateOf<SeriesInfoResponse?>(null)
        private set

    var loadingSeriesInfo by mutableStateOf(false)
        private set

    init {
        credentials?.let {
            login(it, save = false)
        }
    }

    // =========================
    // LOGIN
    // =========================

    fun login(c: Credentials, save: Boolean = true) {
        viewModelScope.launch {
            loading = true
            error = null

            try {
                val api = XtreamApiFactory.create(c.serverUrl)

                val result = api.authenticate(
                    c.username,
                    c.password
                )

                val status = result.user_info?.status?.lowercase()

                if (
                    result.user_info == null ||
                    (status != null &&
                        status !in setOf("active", "enabled"))
                ) {
                    throw IllegalStateException(
                        "Account non valido o non attivo."
                    )
                }

                credentials = c
                auth = result

                if (save) {
                    store.save(c)
                }

                loggedIn = true

                loadCatalog(api, c)

            } catch (e: Exception) {
                loggedIn = false
                error = e.message
                    ?: "Impossibile collegarsi al server."
            } finally {
                loading = false
            }
        }
    }

    // =========================
    // CARICAMENTO CATALOGO
    // =========================

    private suspend fun loadCatalog(
        api: XtreamApiService,
        c: Credentials
    ) {
        liveCategories = runCatching {
            api.liveCategories(
                c.username,
                c.password
            )
        }.getOrDefault(emptyList())

        movieCategories = runCatching {
            api.vodCategories(
                c.username,
                c.password
            )
        }.getOrDefault(emptyList())

        seriesCategories = runCatching {
            api.seriesCategories(
                c.username,
                c.password
            )
        }.getOrDefault(emptyList())

        live = runCatching {
            api.liveStreams(
                c.username,
                c.password
            )
        }.getOrDefault(emptyList())

        movies = runCatching {
            api.vodStreams(
                c.username,
                c.password
            )
        }.getOrDefault(emptyList())

        series = runCatching {
            api.series(
                c.username,
                c.password
            )
        }.getOrDefault(emptyList())
    }

    // =========================
    // UPDATE
    // =========================

    fun updateCatalog() {
        val c = credentials ?: return

        viewModelScope.launch {
            loading = true
            error = null

            try {
                val api = XtreamApiFactory.create(
                    c.serverUrl
                )

                loadCatalog(api, c)

            } catch (e: Exception) {
                error = e.message
                    ?: "Errore durante l'aggiornamento."
            } finally {
                loading = false
            }
        }
    }

    // =========================
    // DETTAGLI SERIE
    // =========================

    fun loadSeriesInfo(seriesId: Int) {
        val c = credentials ?: return

        viewModelScope.launch {
            loadingSeriesInfo = true
            error = null
            selectedSeriesInfo = null

            try {
                val api = XtreamApiFactory.create(
                    c.serverUrl
                )

                selectedSeriesInfo = api.seriesInfo(
                    username = c.username,
                    password = c.password,
                    seriesId = seriesId
                )

            } catch (e: Exception) {
                error = e.message
                    ?: "Impossibile caricare i dettagli della serie."
            } finally {
                loadingSeriesInfo = false
            }
        }
    }

    fun clearSeriesInfo() {
        selectedSeriesInfo = null
        loadingSeriesInfo = false
    }

    // =========================
    // LOGOUT
    // =========================

    fun logout() {
        store.clear()

        credentials = null
        auth = null

        loggedIn = false

        liveCategories = emptyList()
        movieCategories = emptyList()
        seriesCategories = emptyList()

        live = emptyList()
        movies = emptyList()
        series = emptyList()

        selectedSeriesInfo = null
    }

    // =========================
    // STREAM URL
    // =========================

    fun streamUrl(
        type: String,
        id: Int,
        extension: String? = null
    ): String? {

        val c = credentials ?: return null

        val builder = XtreamStreamUrlBuilder(
            c,
            auth?.server_info
        )

        return when (type) {
            "live" -> builder.live(id, extension)

            "movie" -> builder.vod(id, extension)

            "series" -> builder.seriesEpisode(
                id,
                extension
            )

            else -> null
        }
    }
}
