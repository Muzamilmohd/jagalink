package com.jagalink

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.security.MessageDigest
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap

/**
 * Semakan Google Safe Browsing v5 (hashes.search) — versi GitHub sahaja.
 * PRIVASI: hanya 4 bait pertama hash SHA-256 dihantar; padanan penuh dibuat dalam phone.
 * Google tak nampak link sebenar. Tiada data lain dihantar.
 */
object SafeBrowsing {

    private const val ENDPOINT = "https://safebrowsing.googleapis.com/v5/hashes:search"

    // Cache ringkas: prefix → (tamat, full hash → jenis ancaman)
    private class Entry(val until: Long, val hits: Map<String, String>)
    private val cache = ConcurrentHashMap<String, Entry>()

    fun available(): Boolean = !BuildConfig.PLAY && BuildConfig.SB_KEY.isNotBlank()

    fun enabled(ctx: Context): Boolean = available() && Prefs.safeBrowsing(ctx)

    /**
     * Semak satu link. Pulangkan jenis ancaman (cth "SOCIAL_ENGINEERING"), atau null kalau bersih,
     * tiada internet, atau ralat — app tetap guna semakan dalam phone. JANGAN panggil di main thread.
     */
    fun check(ctx: Context, url: String, timeoutMs: Int = 2500): String? {
        val exprs = UrlHashes.expressions(url)
        if (exprs.isEmpty()) return null
        val full = exprs.map { UrlHashes.sha256(it) }
        val now = System.currentTimeMillis()

        val byPrefix = full.groupBy { UrlHashes.prefixBase64(it) }
        val toAsk = byPrefix.keys.filter { (cache[it]?.until ?: 0) < now }
        if (toAsk.isNotEmpty()) {
            val fetched = try {
                fetch(ctx, toAsk, timeoutMs)
            } catch (_: Exception) {
                return cachedHit(byPrefix, now) // gagal → guna apa yang ada dalam cache sahaja
            }
            for (p in toAsk) {
                val hits = fetched.hits.filterKeys { UrlHashes.prefixBase64(Base64.getDecoder().decode(it)) == p }
                cache[p] = Entry(now + fetched.cacheMs, hits)
            }
        }
        return cachedHit(byPrefix, now)
    }

    private fun cachedHit(byPrefix: Map<String, List<ByteArray>>, now: Long): String? {
        for ((p, hashes) in byPrefix) {
            val e = cache[p] ?: continue
            if (e.until < now) continue
            for (h in hashes) e.hits[Base64.getEncoder().encodeToString(h)]?.let { return it }
        }
        return null
    }

    private class Fetched(val hits: Map<String, String>, val cacheMs: Long)

    private fun fetch(ctx: Context, prefixes: List<String>, timeoutMs: Int): Fetched {
        // v5 hanya balas protobuf untuk permintaan sah ($alt=json → "Unsupported Output Format")
        val q = StringBuilder("?key=").append(URLEncoder.encode(BuildConfig.SB_KEY, "UTF-8"))
        for (p in prefixes) q.append("&hashPrefixes=").append(URLEncoder.encode(p, "UTF-8"))
        val conn = URL(ENDPOINT + q).openConnection() as HttpURLConnection
        conn.connectTimeout = timeoutMs
        conn.readTimeout = timeoutMs
        // Untuk API key yang dihadkan kepada app Android ini (Google Cloud → Credentials)
        conn.setRequestProperty("X-Android-Package", ctx.packageName)
        signingSha1(ctx)?.let { conn.setRequestProperty("X-Android-Cert", it) }
        try {
            if (conn.responseCode != 200) throw IllegalStateException("Safe Browsing HTTP ${conn.responseCode}")
            val r = UrlHashes.parseSearchResponse(conn.inputStream.use { it.readBytes() })
            return Fetched(r.hits, (r.cacheSeconds * 1000).coerceIn(60_000, 3_600_000))
        } finally {
            conn.disconnect()
        }
    }

    @Suppress("DEPRECATION")
    private fun signingSha1(ctx: Context): String? = try {
        val sig = if (Build.VERSION.SDK_INT >= 28) {
            ctx.packageManager.getPackageInfo(ctx.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
                .signingInfo?.apkContentsSigners?.firstOrNull()
        } else {
            ctx.packageManager.getPackageInfo(ctx.packageName, PackageManager.GET_SIGNATURES).signatures?.firstOrNull()
        }
        sig?.let { s -> MessageDigest.getInstance("SHA-1").digest(s.toByteArray()).joinToString("") { "%02X".format(it) } }
    } catch (_: Exception) {
        null
    }

    /** Nama ancaman dalam bahasa pengguna. */
    fun describe(ctx: Context, type: String): String = when (type) {
        "SOCIAL_ENGINEERING" -> ctx.tr("laman penipuan (phishing)", "a phishing / scam site", "钓鱼诈骗网站")
        "MALWARE" -> ctx.tr("laman yang sebarkan virus", "a site that spreads malware", "传播病毒的网站")
        "UNWANTED_SOFTWARE" -> ctx.tr("laman perisian berbahaya", "a site with harmful software", "含有害软件的网站")
        "POTENTIALLY_HARMFUL_APPLICATION" -> ctx.tr("laman app berbahaya", "a site with harmful apps", "含有害应用的网站")
        else -> ctx.tr("laman berbahaya", "a dangerous site", "危险网站")
    }
}
