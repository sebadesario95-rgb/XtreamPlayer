package com.example.xtreamplayer.data

data class Credentials(
    val serverUrl: String,
    val username: String,
    val password: String
)

data class AuthResponse(
    val user_info: UserInfo? = null,
    val server_info: ServerInfo? = null
)

data class UserInfo(
    val username: String? = null,
    val status: String? = null,
    val exp_date: String? = null,
    val is_trial: String? = null,
    val active_cons: String? = null,
    val max_connections: String? = null
)

data class ServerInfo(
    val url: String? = null,
    val port: String? = null,
    val https_port: String? = null,
    val server_protocol: String? = null,
    val timezone: String? = null
)

data class Category(
    val category_id: String? = null,
    val category_name: String? = null
)

data class LiveStream(
    val num: Int? = null,
    val name: String? = null,
    val stream_type: String? = null,
    val stream_id: Int? = null,
    val stream_icon: String? = null,
    val epg_channel_id: String? = null,
    val category_id: String? = null
)

data class VodStream(
    val num: Int? = null,
    val name: String? = null,
    val stream_type: String? = null,
    val stream_id: Int? = null,
    val stream_icon: String? = null,
    val rating: String? = null,
    val category_id: String? = null,
    val container_extension: String? = null
)

data class SeriesStream(
    val num: Int? = null,
    val name: String? = null,
    val series_id: Int? = null,
    val cover: String? = null,
    val plot: String? = null,
    val genre: String? = null,
    val releaseDate: String? = null,
    val rating: String? = null,
    val category_id: String? = null
)

data class SeriesInfoResponse(
    val info: SeriesInfo? = null,
    val episodes: Map<String, List<SeriesEpisode>>? = null,
    val seasons: List<SeriesSeason>? = null
)

data class SeriesInfo(
    val name: String? = null,
    val cover: String? = null,
    val plot: String? = null,
    val genre: String? = null,
    val rating: String? = null
)

data class SeriesSeason(
    val season_number: Int? = null,
    val name: String? = null,
    val overview: String? = null,
    val cover: String? = null
)

data class SeriesEpisode(
    val id: String? = null,
    val episode_num: Int? = null,
    val title: String? = null,
    val container_extension: String? = null,
    val info: SeriesEpisodeInfo? = null
)

data class SeriesEpisodeInfo(
    val name: String? = null,
    val plot: String? = null,
    val movie_image: String? = null,
    val duration: String? = null,
    val rating: String? = null
)
