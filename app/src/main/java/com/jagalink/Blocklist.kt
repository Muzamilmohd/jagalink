package com.jagalink

import android.content.Context
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * Senarai domain scam (blocklist/domains.txt dalam repo).
 * - Salinan dalam APK (assets) — versi Play dapat senarai terkini setiap kali release.
 * - Versi GitHub muat turun versi terbaru dari GitHub sekali sehari (fail awam, tiada data pengguna dihantar).
 */
object Blocklist {
    private const val ASSET = "domains.txt"
    private const val FILE = "blocklist.txt"
    @Volatile private var loaded = false

    /** Muat senarai ke LinkAnalyzer (sekali setiap proses). Murah — boleh dipanggil di mana-mana. */
    fun ensureLoaded(ctx: Context) {
        if (loaded) return
        loaded = true
        val downloaded = File(ctx.filesDir, FILE)
        val text = try {
            if (downloaded.exists()) downloaded.readText()
            else ctx.assets.open(ASSET).use { it.bufferedReader().readText() }
        } catch (_: Exception) {
            ""
        }
        LinkAnalyzer.extraBlocked = LinkAnalyzer.parseBlocklist(text)
    }

    /** Muat turun versi terbaru (versi GitHub sahaja). JANGAN panggil di main thread. */
    fun refresh(ctx: Context) {
        if (BuildConfig.PLAY || !BuildConfig.UPDATE_REPO.contains('/')) return
        try {
            val conn = URL("https://raw.githubusercontent.com/${BuildConfig.UPDATE_REPO}/main/blocklist/domains.txt")
                .openConnection() as HttpURLConnection
            conn.connectTimeout = 15_000
            conn.readTimeout = 15_000
            conn.setRequestProperty("User-Agent", "JagaPMA")
            try {
                if (conn.responseCode != 200) return
                val text = conn.inputStream.use { it.bufferedReader().readText() }
                if (text.length > 2_000_000) return // elak fail pelik yang terlalu besar
                File(ctx.filesDir, FILE).writeText(text)
                LinkAnalyzer.extraBlocked = LinkAnalyzer.parseBlocklist(text)
                loaded = true
                Prefs.setLastBlocklistFetch(ctx, System.currentTimeMillis())
            } finally {
                conn.disconnect()
            }
        } catch (_: Exception) {
        }
    }

    /** Muat turun di latar kalau dah lebih 12 jam. */
    fun refreshIfStale(ctx: Context) {
        if (System.currentTimeMillis() - Prefs.lastBlocklistFetch(ctx) < 12 * 60 * 60_000L) return
        val app = ctx.applicationContext
        Thread { refresh(app) }.start()
    }
}
