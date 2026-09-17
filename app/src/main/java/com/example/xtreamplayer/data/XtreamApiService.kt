package com.example.xtreamplayer.data

import retrofit2.http.GET
import retrofit2.http.Query

interface XtreamApiService {

    @GET("player_api.php")
    suspend fun authenticate(
        @Query("username") username: String,
        @Query("password") password: String
    ): AuthResponse

    @GET("player_api.php")
    suspend fun getLiveCategories(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_live_categories"
    ): List<Category>

    @GET("player_api.php")
    suspend fun getLiveStreams(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_live_streams"
    ): List<LiveStream>

    @GET("player_api.php")
    suspend fun getVodCategories(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_vod_categories"
    ): List<Category>

    @GET("player_api.php")
    suspend fun getVodStreams(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_vod_streams"
    ): List<VodStream>

    @GET("player_api.php")
    suspend fun getSeriesCategories(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_series_categories"
    ): List<Category>

    @GET("player_api.php")
    suspend fun getSeriesStreams(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_series"
    ): List<SeriesStream>

    @GET("player_api.php")
    suspend fun getSeriesInfo(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_series_info",
        @Query("series_id") seriesId: Int
    ): SeriesInfoResponse

    /*
     * EPG breve del singolo canale.
     *
     * Usiamo stream_id perché get_short_epg lavora sullo stream
     * Xtream selezionato. limit=4 ci evita di scaricare dati
     * inutili: ci servono soltanto programma attuale e successivo.
     */
    @GET("player_api.php")
    suspend fun getShortEpg(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_short_epg",
        @Query("stream_id") streamId: Int,
        @Query("limit") limit: Int = 4
    ): ShortEpgResponse
}
