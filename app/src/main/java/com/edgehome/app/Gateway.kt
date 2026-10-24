package com.edgehome.app

import android.content.Context
import android.net.Uri

/** Stores and validates the hub's gateway address. */
object Gateway {
    private const val PREFS = "edge_home"
    private const val KEY_URL = "gateway_url"

    /** The saved gateway, or null before first-run setup is done. */
    fun saved(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_URL, null)

    fun save(context: Context, url: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_URL, url)
            .apply()
    }

    /**
     * Turns what the user typed into a usable https URL, or null if it can't be one.
     * A bare host gets https:// added. Plain http is refused: the app only talks
     * to the hub over TLS (Tailscale serves a valid certificate for *.ts.net).
     */
    fun normalize(input: String): String? {
        var text = input.trim()
        if (text.isEmpty() || text.any { it.isWhitespace() }) return null
        if (!text.contains("://")) text = "https://$text"
        val uri = Uri.parse(text)
        if (!uri.scheme.equals("https", ignoreCase = true)) return null
        val host = uri.host
        if (host.isNullOrBlank() || !host.contains('.')) return null
        return text.trimEnd('/')
    }
}
