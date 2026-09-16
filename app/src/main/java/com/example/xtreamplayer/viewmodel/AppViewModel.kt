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
import org.json.JSONArray

class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val store = CredentialStore(app)

    private val prefs =
        app.getSharedPreferences(
            "xtream_player_preferences",
            Application.MODE_PRIVATE
        )

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

    var liveCategories by mutableStateOf<List<Category>>(emptyList())
        private set

    var movieCategories by mutableStateOf<List<Category>>(emptyList())
        private set

    var seriesCategories by mutableStateOf<List<Category>>(emptyList())
        private set

    var live by mutableStateOf<List<LiveStream>>(emptyList())
        private set

    var movies by mutableStateOf<List<VodStream>>(emptyList())
        private set

    var series by mutableStateOf<List<SeriesStream>>(emptyList())
        private set

    var selectedSeriesInfo by mutableStateOf<SeriesInfoResponse?>(null)
        private set

    var loadingSeriesInfo by mutableStateOf(false)
        private set

    var favoriteLiveIds by mutableStateOf<Set<Int>>(emptySet())
        private set

    var favoriteMovieIds by mutableStateOf<Set<Int>>(emptySet())
        private set

    var favoriteSeriesIds by mutableStateOf<Set<Int>>(emptySet())
        private set

    init {
        loadFavorites()
        credentials?.let {
            login(it, save = false)
        }
    }

    fun login(
        c: Credentials,
        save: Boolean = true
    ) {
        viewModelScope.launch {
            loading = true
            error = null

            try {
                val api =
                    XtreamApiFactory.create(c.serverUrl)

                val result =
                    api.authenticate(
                        c.username,
                        c.password
                    )

                val status =
                    result.user_info?.status?.lowercase()

                if (
                    result.user_info == null ||
                    (
                        status != null &&
                        status !in setOf(
                            "active",
                            "enabled"
                        )
                    )
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

                loadCatalog(
                    api,
                    c
                )

            } catch (e: Exception) {
                loggedIn = false
                error =
                    e.message
                        ?: "Impossibile collegarsi al server."
            } finally {
                loading = false
            }
        }
    }

    private suspend fun loadCatalog(
        api: XtreamApiService,
        c: Credentials
    ) {
        liveCategories =
            runCatching {
                api.liveCategories(
                    c.username,
                    c.password
                )
            }.getOrDefault(emptyList())

        movieCategories =
            runCatching {
                api.vodCategories(
                    c.username,
                    c.password
                )
            }.getOrDefault(emptyList())

        seriesCategories =
            runCatching {
                api.seriesCategories(
                    c.username,
                    c.password
                )
            }.getOrDefault(emptyList())

        live =
            runCatching {
                api.liveStreams(
                    c.username,
                    c.password
                )
            }.getOrDefault(emptyList())

        movies =
            runCatching {
                api.vodStreams(
                    c.username,
                    c.password
                )
            }.getOrDefault(emptyList())

        series =
            runCatching {
                api.series(
                    c.username,
                    c.password
                )
            }.getOrDefault(emptyList())
    }

    fun updateCatalog() {
        val c = credentials ?: return

        viewModelScope.launch {
            loading = true
            error = null

            try {
                val api =
                    XtreamApiFactory.create(c.serverUrl)

                loadCatalog(
                    api,
                    c
                )

            } catch (e: Exception) {
                error =
                    e.message
                        ?: "Errore durante l'aggiornamento."
            } finally {
                loading = false
            }
        }
    }

    fun loadSeriesInfo(
        seriesId: Int
    ) {
        val c = credentials ?: return

        viewModelScope.launch {
            loadingSeriesInfo = true
            error = null
            selectedSeriesInfo = null

            try {
                val api =
                    XtreamApiFactory.create(c.serverUrl)

                selectedSeriesInfo =
                    api.seriesInfo(
                        username = c.username,
                        password = c.password,
                        seriesId = seriesId
                    )

            } catch (e: Exception) {
                error =
                    e.message
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

    fun toggleFavoriteLive(
        id: Int
    ) {
        favoriteLiveIds =
            if (id in favoriteLiveIds) {
                favoriteLiveIds - id
            } else {
                favoriteLiveIds + id
            }

        saveFavorites()
    }

    fun toggleFavoriteMovie(
        id: Int
    ) {
        favoriteMovieIds =
            if (id in favoriteMovieIds) {
                favoriteMovieIds - id
            } else {
                favoriteMovieIds + id
            }

        saveFavorites()
    }

    fun toggleFavoriteSeries(
        id: Int
    ) {
        favoriteSeriesIds =
            if (id in favoriteSeriesIds) {
                favoriteSeriesIds - id
            } else {
                favoriteSeriesIds + id
            }

        saveFavorites()
    }

    fun isFavoriteLive(
        id: Int
    ): Boolean {
        return id in favoriteLiveIds
    }

    fun isFavoriteMovie(
        id: Int
    ): Boolean {
        return id in favoriteMovieIds
    }

    fun isFavoriteSeries(
        id: Int
    ): Boolean {
        return id in favoriteSeriesIds
    }

    private fun loadFavorites() {
        favoriteLiveIds =
            loadIds(
                "favorite_live"
            )

        favoriteMovieIds =
            loadIds(
                "favorite_movies"
            )

        favoriteSeriesIds =
            loadIds(
                "favorite_series"
            )
    }

    private fun saveFavorites() {
        saveIds(
            "favorite_live",
            favoriteLiveIds
        )

        saveIds(
            "favorite_movies",
            favoriteMovieIds
        )

        saveIds(
            "favorite_series",
            favoriteSeriesIds
        )
    }

    private fun loadIds(
        key: String
    ): Set<Int> {
        val raw =
            prefs.getString(
                key,
                null
            ) ?: return emptySet()

        return try {
            val array =
                JSONArray(raw)

            buildSet {
                for (i in 0 until array.length()) {
                    add(
                        array.getInt(i)
                    )
                }
            }
        } catch (
            _: Exception
        ) {
            emptySet()
        }
    }

    private fun saveIds(
        key: String,
        ids: Set<Int>
    ) {
        val array =
            JSONArray()

        ids.forEach {
            array.put(it)
        }

        prefs.edit()
            .putString(
                key,
                array.toString()
            )
            .apply()
    }

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

    fun streamUrl(
        type: String,
        id: Int,
        extension: String? = null
    ): String? {
        val c =
            credentials
                ?: return null

        val builder =
            XtreamStreamUrlBuilder(
                c,
                auth?.server_info
            )

        return when (type) {
            "live" ->
                builder.live(
                    id,
                    extension
                )

            "movie" ->
                builder.vod(
                    id,
                    extension
                )

            "series" ->
                builder.seriesEpisode(
                    id,
                    extension
                )

            else ->
                null
        }
    }
}
