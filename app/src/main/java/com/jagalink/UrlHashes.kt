package com.jagalink

import java.security.MessageDigest
import java.util.Base64
import java.util.Locale

/**
 * Tukar link kepada "expression" ikut spesifikasi Google Safe Browsing, kemudian hash SHA-256.
 * Hanya 4 bait pertama setiap hash dihantar ke Google — Google tak tahu link sebenar.
 * Pure Kotlin (tiada Android) supaya boleh diuji di PC.
 * Rujukan: developers.google.com/safe-browsing/reference/URLs.and.Hashing
 */
object UrlHashes {

    /** Semua kombinasi host-suffix × path-prefix (maks 30) untuk satu link. */
    fun expressions(raw: String): List<String> {
        val c = canonicalize(raw) ?: return emptyList()
        val hosts = hostSuffixes(c.host)
        val paths = pathPrefixes(c.path, c.query)
        val out = LinkedHashSet<String>()
        for (h in hosts) for (p in paths) out += h + p
        return out.take(30)
    }

    fun sha256(s: String): ByteArray = MessageDigest.getInstance("SHA-256").digest(s.toByteArray(Charsets.UTF_8))

    fun prefixBase64(fullHash: ByteArray): String = Base64.getEncoder().encodeToString(fullHash.copyOf(4))

    // ---------------------------------------------------------------- canonicalize

    class Canonical(val host: String, val path: String, val query: String?)

    fun canonicalize(raw: String): Canonical? {
        var s = raw.trim().replace("\t", "").replace("\r", "").replace("\n", "")
        s = s.substringBefore('#')
        s = unescapeRepeatedly(s)
        s = s.replace(Regex("^(?i)[a-z][a-z0-9+.-]*://"), "")
        val split = s.indexOfFirst { it == '/' || it == '?' }
        var authority = if (split < 0) s else s.substring(0, split)
        var rest = if (split < 0) "/" else s.substring(split)
        if (rest.startsWith("?")) rest = "/$rest"
        if ('@' in authority) authority = authority.substringAfterLast('@')
        authority = authority.substringBefore(':')
        var host = authority.lowercase(Locale.ROOT).trim('.').replace(Regex("\\.{2,}"), ".")
        if (host.isEmpty()) return null
        host = escape(host)

        val query = if ('?' in rest) rest.substringAfter('?') else null
        val path = escape(normalizePath(rest.substringBefore('?')))
        return Canonical(host, path, query?.let { escape(it) })
    }

    private fun unescapeRepeatedly(s: String): String {
        var cur = s
        repeat(5) {
            val next = Regex("%([0-9A-Fa-f]{2})").replace(cur) { m -> m.groupValues[1].toInt(16).toChar().toString() }
            if (next == cur) return cur
            cur = next
        }
        return cur
    }

    private fun normalizePath(p: String): String {
        val parts = ArrayList<String>()
        for (seg in p.split('/')) {
            when (seg) {
                "", "." -> Unit
                ".." -> if (parts.isNotEmpty()) parts.removeAt(parts.size - 1)
                else -> parts += seg
            }
        }
        val trailing = p.endsWith("/") || p.endsWith("/.") || p.endsWith("/..")
        val joined = "/" + parts.joinToString("/")
        return if (trailing && joined != "/") "$joined/" else joined
    }

    /** Escape aksara ≤ 0x20, ≥ 0x7F, '#' dan '%'. */
    private fun escape(s: String): String {
        val sb = StringBuilder()
        for (b in s.toByteArray(Charsets.UTF_8)) {
            val i = b.toInt() and 0xFF
            if (i <= 0x20 || i >= 0x7F || i == '#'.code || i == '%'.code) sb.append('%').append(String.format("%02X", i))
            else sb.append(i.toChar())
        }
        return sb.toString()
    }

    // ---------------------------------------------------------------- suffix / prefix

    private val IPV4 = Regex("^\\d{1,3}(\\.\\d{1,3}){3}$")

    /** Host penuh + sehingga 4 host dibina dari 5 komponen terakhir (tanpa TLD sahaja). */
    fun hostSuffixes(host: String): List<String> {
        if (IPV4.matches(host)) return listOf(host)
        val labels = host.split('.')
        val out = LinkedHashSet<String>()
        out += host
        val tail = labels.takeLast(5)
        for (i in 0 until tail.size - 1) {
            out += tail.subList(i, tail.size).joinToString(".")
            if (out.size >= 5) break
        }
        return out.toList()
    }

    /** Path + query, path, dan sehingga 4 prefix bermula dari "/". */
    fun pathPrefixes(path: String, query: String?): List<String> {
        val out = LinkedHashSet<String>()
        if (query != null) out += "$path?$query"
        out += path
        val segs = path.split('/').filter { it.isNotEmpty() }
        var cur = "/"
        out += cur
        for (i in 0 until minOf(3, segs.size)) {
            // komponen terakhir tanpa '/' sudah diwakili oleh path penuh
            if (i == segs.size - 1 && !path.endsWith("/")) break
            cur += segs[i] + "/"
            out += cur
        }
        return out.take(6)
    }
}
