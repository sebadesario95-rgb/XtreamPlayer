package com.example.xtreamplayer.player

import com.example.xtreamplayer.data.Credentials
import com.example.xtreamplayer.data.ServerInfo

/**
 * Builds stream URLs without hardcoding a provider/server.
 *
 * The builder prefers the protocol/host information returned by Xtream
 * when available and falls back to the user's configured server URL.
 * Container extensions are normalized and can be changed by the server
 * response rather than being fixed in the UI.
 */
class XtreamStreamUrlBuilder(
    private val credentials: Credentials,
    private val serverInfo: ServerInfo? = null
) {
    private val base = chooseBaseUrl()

    private fun chooseBaseUrl(): String {
        val protocol = serverInfo?.server_protocol
            ?.takeIf { it == "http" || it == "https" }
            ?: credentials.serverUrl.substringBefore("://").takeIf {
                it == "http" || it == "https"
            }
            ?: "http"

        val configuredHost = credentials.serverUrl
            .substringAfter("://", credentials.serverUrl)
            .trimEnd('/')

        val apiHost = serverInfo?.url?.trim()?.takeIf { it.isNotEmpty() }
        val port = if (protocol == "https") {
            serverInfo?.https_port?.takeIf { it.isNotBlank() }
        } else {
            serverInfo?.port?.takeIf { it.isNotBlank() }
        }

        val host = apiHost ?: configuredHost
        val hostWithPort = if (port != null && !host.substringAfterLast("/").contains(":$port")) {
            "$host:$port"
        } else host

        return "$protocol://$hostWithPort".trimEnd('/')
    }

    fun live(streamId: Int, extensionHint: String? = null): String {
        val ext = normalizeExtension(extensionHint, "ts")
        return "$base/live/${credentials.username}/${credentials.password}/$streamId.$ext"
    }

    fun vod(streamId: Int, extensionHint: String?): String {
        val ext = normalizeExtension(extensionHint, "mp4")
        return "$base/movie/${credentials.username}/${credentials.password}/$streamId.$ext"
    }

    fun seriesEpisode(streamId: Int, extensionHint: String?): String {
        val ext = normalizeExtension(extensionHint, "mp4")
        return "$base/series/${credentials.username}/${credentials.password}/$streamId.$ext"
    }

    private fun normalizeExtension(value: String?, fallback: String): String {
        val clean = value?.trim()?.lowercase()?.removePrefix(".")
        return when (clean) {
            "ts", "m3u8", "mp4", "mkv", "avi", "mov", "webm" -> clean
            null, "" -> fallback
            else -> fallback
        }
    }
}
