package com.example.xtreamplayer.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class CredentialStore(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "xtream_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun save(c: Credentials) {
        prefs.edit()
            .putString("server", c.serverUrl)
            .putString("username", c.username)
            .putString("password", c.password)
            .apply()
    }

    fun load(): Credentials? {
        val s = prefs.getString("server", null) ?: return null
        val u = prefs.getString("username", null) ?: return null
        val p = prefs.getString("password", null) ?: return null
        return Credentials(s, u, p)
    }

    fun clear() = prefs.edit().clear().apply()
}
