package com.jagalink

import java.net.HttpURLConnection
import java.net.URL

/**
 * Cari destinasi sebenar link pendek (bit.ly, tinyurl…) tanpa membuka laman tu.
 * PRIVASI: hanya servis link pendek yang dihubungi — sebaik destinasi bukan lagi link pendek,
 * kita berhenti, jadi laman destinasi (mungkin scam) tak tahu link tu disemak.
 * Pure Kotlin (java.net sahaja) supaya boleh diuji di PC.
 */
object ShortLinks {

    /** Pulangkan link destinasi, atau null kalau gagal / tiada internet / bukan link pendek. */
    fun resolve(url: String, isShortener: (String) -> Boolean, timeoutMs: Int = 2500, maxHops: Int = 5): String? {
        var cur = if (url.contains("://")) url else "https://$url"
        val deadline = System.currentTimeMillis() + timeoutMs
        repeat(maxHops) {
            val u = try { URL(cur) } catch (_: Exception) { return null }
            if (!isShortener(u.host.lowercase())) return cur.takeIf { it != url }
            val left = (deadline - System.currentTimeMillis()).toInt()
            if (left <= 0) return null
            val next = try { location(u, "HEAD", left) ?: location(u, "GET", left) } catch (_: Exception) { null } ?: return null
            cur = next
        }
        return cur.takeIf { it != url }
    }

    /** Satu langkah: baca header Location tanpa ikut redirect dan tanpa baca kandungan laman. */
    private fun location(u: URL, method: String, timeoutMs: Int): String? {
        val conn = u.openConnection() as HttpURLConnection
        conn.instanceFollowRedirects = false
        conn.requestMethod = method
        conn.connectTimeout = timeoutMs
        conn.readTimeout = timeoutMs
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android) JagaPMA")
        try {
            if (conn.responseCode !in 300..399) return null
            val loc = conn.getHeaderField("Location") ?: return null
            return URL(u, loc).toString() // Location mungkin relatif
        } finally {
            conn.disconnect()
        }
    }
}
