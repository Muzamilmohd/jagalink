package com.jagalink

import java.util.Locale

enum class Level { SAFE, WARN, DANGER }

/** Bahasa paparan: Melayu (asal), English, 中文 (ringkas). */
enum class Lang(val code: String) {
    MS("ms"), EN("en"), ZH("zh");

    fun t(ms: String, en: String, zh: String): String = when (this) {
        MS -> ms
        EN -> en
        ZH -> zh
    }

    companion object {
        fun of(code: String?): Lang = entries.firstOrNull { it.code == code } ?: MS
    }
}

data class Verdict(
    val url: String,
    val host: String,
    val score: Int,
    val reasons: List<String>,
    val adult: Boolean = false,
) {
    val level: Level
        get() = when {
            score >= 60 -> Level.DANGER
            score >= 30 -> Level.WARN
            else -> Level.SAFE
        }
}

/**
 * Enjin semak link. Semua dibuat dalam telefon (offline) — tiada data dihantar keluar.
 * Skor 0–100: >=60 BAHAYA, >=30 MENCURIGAKAN.
 */
object LinkAnalyzer {

    private data class Brand(val token: String, val display: String, val officials: List<String>)

    // Jenama yang selalu ditiru scammer di Malaysia + laman rasmi mereka.
    private val BRANDS = listOf(
        Brand("maybank", "Maybank", listOf("maybank2u.com.my", "maybank.com", "maybank.com.my")),
        Brand("cimb", "CIMB", listOf("cimbclicks.com.my", "cimb.com.my", "cimb.com")),
        Brand("publicbank", "Public Bank", listOf("pbebank.com", "publicbank.com.my")),
        Brand("pbebank", "Public Bank", listOf("pbebank.com")),
        Brand("rhb", "RHB", listOf("rhbgroup.com", "rhbnow.com.my")),
        Brand("bankislam", "Bank Islam", listOf("bankislam.com")),
        Brand("bsn", "BSN", listOf("bsn.com.my")),
        Brand("ambank", "AmBank", listOf("ambank.com.my", "ambankgroup.com")),
        Brand("hongleong", "Hong Leong Bank", listOf("hlb.com.my", "hongleong.com.my")),
        Brand("bankrakyat", "Bank Rakyat", listOf("bankrakyat.com.my")),
        Brand("touchngo", "Touch 'n Go", listOf("touchngo.com.my", "tngdigital.com.my")),
        Brand("tng", "Touch 'n Go", listOf("touchngo.com.my", "tngdigital.com.my")),
        Brand("poslaju", "Pos Laju", listOf("pos.com.my")),
        Brand("posmalaysia", "Pos Malaysia", listOf("pos.com.my")),
        Brand("lhdn", "LHDN", listOf("hasil.gov.my")),
        Brand("kwsp", "KWSP", listOf("kwsp.gov.my")),
        Brand("epf", "KWSP", listOf("kwsp.gov.my")),
        Brand("jpj", "JPJ", listOf("jpj.gov.my")),
        Brand("pdrm", "PDRM", listOf("rmp.gov.my")),
        Brand("perkeso", "PERKESO", listOf("perkeso.gov.my")),
        Brand("shopee", "Shopee", listOf("shopee.com.my", "shopee.com", "shope.ee")),
        Brand("lazada", "Lazada", listOf("lazada.com.my", "lazada.com")),
        Brand("tnb", "TNB", listOf("tnb.com.my", "mytnb.com.my")),
        Brand("myeg", "MyEG", listOf("myeg.com.my")),
        Brand("unifi", "Unifi", listOf("unifi.com.my", "tm.com.my")),
        Brand("jnt", "J&T", listOf("jtexpress.my")),
        Brand("ninjavan", "Ninja Van", listOf("ninjavan.co")),
        Brand("dhl", "DHL", listOf("dhl.com", "dhl.com.my")),
        Brand("whatsapp", "WhatsApp", listOf("whatsapp.com", "whatsapp.net", "wa.me")),
        Brand("facebook", "Facebook", listOf("facebook.com", "fb.com", "fb.watch")),
        Brand("instagram", "Instagram", listOf("instagram.com")),
        Brand("google", "Google", listOf("google.com", "google.com.my", "goo.gl")),
        Brand("youtube", "YouTube", listOf("youtube.com", "youtu.be")),
        Brand("netflix", "Netflix", listOf("netflix.com")),
        Brand("telegram", "Telegram", listOf("telegram.org", "t.me")),
    )

    // Laman yang dikira selamat (kecuali kalau link terus ke fail APK).
    private val TRUSTED = setOf(
        "gov.my", "google.com", "google.com.my", "youtube.com", "youtu.be", "whatsapp.com", "wa.me", "wa.link",
        "facebook.com", "fb.com", "fb.watch", "instagram.com", "tiktok.com", "telegram.org", "t.me",
        "wikipedia.org", "apple.com", "microsoft.com", "shope.ee", "maps.app.goo.gl",
        "googleusercontent.com", "googleapis.com", "gstatic.com", "fbcdn.net", "facebook.net", "telegram.me",
        "play.google.com", "shopee.sg", "shopee.co.id",
        "astroawani.com", "bharian.com.my", "hmetro.com.my", "sinarharian.com.my", "utusan.com.my",
        "kosmo.com.my", "thestar.com.my", "nst.com.my", "malaysiakini.com", "says.com", "mstar.com.my",
    ) + BRANDS.flatMap { it.officials }

    private val SHORTENERS = setOf(
        "bit.ly", "bitly.com", "tinyurl.com", "t.co", "s.id", "cutt.ly", "rb.gy", "is.gd", "v.gd",
        "shorturl.at", "shorturl.asia", "tiny.cc", "ow.ly", "rebrand.ly", "t.ly", "short.gy",
        "urlz.fr", "qrco.de", "shorturl.me", "bit.do", "tny.im", "clck.ru", "u.to",
    )

    private val SUSPICIOUS_TLDS = setOf(
        "xyz", "top", "icu", "click", "link", "live", "vip", "cc", "tk", "ml", "ga", "cf", "gq", "buzz",
        "rest", "cyou", "sbs", "cfd", "monster", "win", "loan", "work", "online", "site", "pw", "fun",
        "support", "help", "services", "website", "space", "bond", "lol", "shop", "club", "asia", "mobi",
    )

    // Ayat biasa dalam mesej scam (BM + BI).
    private val SCAM_WORDS = listOf(
        "tahniah", "anda menang", "anda telah menang", "hadiah", "cabutan bertuah", "percuma",
        "akaun anda", "dibekukan", "disekat", "digantung", "ditamatkan", "sahkan", "pengesahan",
        "kemas kini", "kemaskini", "otp", "tac", "kata laluan", "segera", "dalam masa 24 jam",
        "pinjaman", "pelaburan", "untung", "pulangan", "bungkusan", "parcel", "tertahan",
        "saman", "tertunggak", "bayaran balik", "refund", "bantuan tunai", "klik link",
        "klik pautan", "tekan link", "kerja sambilan", "part time", "jemputan", "kad kahwin",
        "verify", "suspended", "urgent", "you won", "prize", "claim", "login now", "unpaid",
        "恭喜", "中奖", "奖品", "账户", "冻结", "验证", "紧急", "立即", "贷款", "投资", "包裹",
        "退款", "罚款", "点击链接", "兼职", "验证码", "密码",
    )

    // Laman lucah — disekat terus (tiada pilihan "buka juga"). Subdomain & cermin ikut sekali.
    private val ADULT_SITES = setOf(
        "pornhub.com", "xvideos.com", "xnxx.com", "xhamster.com", "redtube.com", "youporn.com", "tube8.com",
        "spankbang.com", "eporner.com", "beeg.com", "tnaflix.com", "porntrex.com", "youjizz.com",
        "motherless.com", "hqporner.com", "txxx.com", "4tube.com", "drtuber.com", "sunporno.com",
        "xozilla.com", "porn.com", "sex.com", "brazzers.com", "realitykings.com", "bangbros.com",
        "chaturbate.com", "stripchat.com", "bongacams.com", "livejasmin.com", "cam4.com", "camsoda.com",
        "myfreecams.com", "onlyfans.com", "fansly.com", "manyvids.com", "clips4sale.com",
        "nhentai.net", "e-hentai.org", "exhentai.org", "rule34.xxx", "gelbooru.com", "hanime.tv",
        "missav.com", "missav.ws", "jable.tv", "javlibrary.com", "javhd.com", "supjav.com",
        "erome.com", "fapello.com", "thothub.to", "spankwire.com", "keezmovies.com", "slutload.com",
    )
    // Perkataan dalam nama laman yang hampir pasti lucah (dicari di mana-mana dalam alamat).
    private val ADULT_WORDS = listOf(
        "porn", "xvideo", "xnxx", "xhamster", "hentai", "bokep", "lucah", "onlyfans", "redtube",
        "youporn", "spankbang", "chaturbate", "stripchat", "xxx",
    )
    // Perkataan pendek — mesti jadi satu bahagian penuh alamat (elak "essex", "javascript").
    private val ADULT_TOKENS = setOf("sex", "jav", "nsfw", "milf", "nude", "nudes", "bogel", "camgirls")
    private val ADULT_TLDS = setOf("xxx", "porn", "sex", "adult", "sexy")

    private val BAIT_IN_URL = listOf(
        "login", "log-in", "signin", "verify", "verif", "update", "secure", "account", "akaun",
        "claim", "bonus", "hadiah", "reward", "free", "percuma", "bank", "otp", "tac", "pay",
    )

    private const val TLDS =
        "com|net|org|info|biz|my|asia|xyz|top|icu|click|link|shop|store|live|vip|cc|tk|ml|ga|cf|gq|" +
            "buzz|rest|cyou|sbs|cfd|monster|win|loan|work|online|site|app|io|co|me|ly|gl|gd|to|pw|club|" +
            "fun|life|today|support|help|services|website|space|pro|bond|lol|mobi|id|ru|cn"

    private val URL_REGEX = Regex(
        "(?i)((?:https?://|www\\.)[^\\s<>\"'()\\[\\]{}]+" +
            "|(?<![\\p{L}\\p{N}@.-])[\\p{L}\\p{N}][\\p{L}\\p{N}-]*(?:\\.[\\p{L}\\p{N}-]+)*\\.(?:$TLDS)" +
            "(?![\\p{L}\\p{N}-])(?:[/?#][^\\s<>\"'()\\[\\]{}]*)?)"
    )
    private val APK_FILE_REGEX = Regex("(?i)[\\p{L}\\p{N}_()\\-][\\p{L}\\p{N} _()\\-]{0,60}\\.apk(?![\\p{L}\\p{N}])")
    private val APK_URL_REGEX = Regex("(?i)\\.apk(?:$|[?#&/])")
    private val IP_REGEX = Regex("^\\d{1,3}(?:\\.\\d{1,3}){3}$")

    // ---------------------------------------------------------------- API

    /** Semak satu blok teks (mesej / skrin). Pulangkan keputusan untuk setiap link & fail APK. */
    fun analyzeMessage(text: String, lang: Lang = Lang.MS): List<Verdict> {
        val out = mutableListOf<Verdict>()
        for (u in findUrls(text)) out += analyzeUrl(u, text, lang)
        val apks = APK_FILE_REGEX.findAll(text).map { it.value.trim() }.distinct()
        for (name in apks) {
            if (out.none { it.url.contains(name, ignoreCase = true) }) {
                out += Verdict(
                    name, "", 85,
                    listOf(
                        lang.t(
                            "Ada fail APK (aplikasi) dihantar: \"$name\". JANGAN buka atau pasang.",
                            "An APK (app) file was sent: \"$name\". Do NOT open or install it.",
                            "有人发送了 APK（应用程序）文件：\"$name\"。千万不要打开或安装。",
                        ),
                        lang.t(
                            "Ini taktik scam paling biasa — contohnya kad jemputan kahwin palsu, resit, atau bungkusan.",
                            "This is the most common scam trick — e.g. fake wedding invitations, receipts or parcel notices.",
                            "这是最常见的诈骗手法——例如假的婚礼请柬、收据或包裹通知。",
                        ),
                    )
                )
            }
        }
        return out
    }

    fun findUrls(text: String): List<String> =
        URL_REGEX.findAll(text)
            .map { it.value.trimEnd('.', ',', ';', ':', '!', '?', ')', '*', '_', '~') }
            .filter { it.length >= 4 }
            .distinct()
            .toList()

    fun analyzeUrl(raw: String, context: String = "", lang: Lang = Lang.MS): Verdict {
        val url = raw.trim()
        val host = extractHost(url)
        val reasons = mutableListOf<String>()
        var score = 0
        fun add(points: Int, ms: String, en: String, zh: String) {
            score += points
            val why = lang.t(ms, en, zh)
            if (why !in reasons) reasons += why
        }

        if (isAdult(host)) {
            return Verdict(
                url, host, 100,
                listOf(
                    lang.t(
                        "Laman ni ada kandungan lucah. JagaPMA dah sekat link ni.",
                        "This site has pornographic content. JagaPMA has blocked this link.",
                        "这个网站含有色情内容。JagaPMA 已拦截此链接。",
                    )
                ),
                adult = true,
            )
        }

        if (APK_URL_REGEX.containsMatchIn(url)) {
            add(
                80,
                "Link ni akan muat turun fail APK (aplikasi). Scammer guna cara ni untuk curi data dan duit dalam phone.",
                "This link downloads an APK (app) file. Scammers use this to steal data and money from your phone.",
                "这个链接会下载 APK（应用程序）文件。骗子用这种方法盗取手机里的资料和钱。",
            )
        }

        if (isTrusted(host)) return Verdict(url, host, score.coerceAtMost(100), reasons)

        if (host.any { it.code > 127 }) {
            add(
                60,
                "Alamat link ada huruf pelik / huruf asing yang menyamar jadi huruf biasa.",
                "The link address has strange / foreign letters pretending to be normal letters.",
                "链接地址里有奇怪的外文字母，假装成普通字母。",
            )
        }
        if (host.contains("xn--")) {
            add(
                60,
                "Alamat link guna huruf tersembunyi (kod xn--) untuk menyamar jadi laman lain.",
                "The link uses hidden letters (xn-- code) to pretend to be another site.",
                "链接使用隐藏字母（xn-- 代码）冒充其他网站。",
            )
        }
        if (IP_REGEX.matches(host)) {
            add(
                50,
                "Link guna nombor IP, bukan nama laman yang sah.",
                "The link uses an IP number, not a proper site name.",
                "链接使用 IP 号码，而不是正规的网站名称。",
            )
        }
        val authority = url.replace(Regex("(?i)^[a-z][a-z0-9+.-]*://"), "").substringBefore('/')
        if ('@' in authority) {
            add(
                45,
                "Link ada simbol @ untuk sorok alamat sebenar.",
                "The link has an @ symbol to hide the real address.",
                "链接里有 @ 符号，用来隐藏真正的地址。",
            )
        }
        if (host in SHORTENERS) {
            add(
                35,
                "Link dipendekkan — kita tak nampak ke mana ia sebenarnya pergi.",
                "The link is shortened — we can't see where it really goes.",
                "这是缩短的链接——看不到它真正会去哪里。",
            )
        }
        impersonatedBrand(host)?.let { brand ->
            add(
                65,
                "Guna nama \"$brand\" tapi BUKAN laman rasmi $brand.",
                "Uses the name \"$brand\" but is NOT the official $brand site.",
                "使用「$brand」的名字，但不是 $brand 的官方网站。",
            )
        }
        val tld = host.substringAfterLast('.', "")
        if (tld in SUSPICIOUS_TLDS) {
            add(
                30,
                "Hujung alamat (.$tld) selalu dipakai laman scam.",
                "The ending (.$tld) is often used by scam sites.",
                "网址结尾（.$tld）常被诈骗网站使用。",
            )
        }
        if (url.startsWith("http://", ignoreCase = true)) {
            add(10, "Link tak selamat (tiada https).", "Unsafe link (no https).", "不安全的链接（没有 https）。")
        }
        if (host.count { it == '-' } >= 2) {
            add(
                10,
                "Alamat berselirat dengan banyak sengkang (-).",
                "Messy address with many dashes (-).",
                "地址很乱，有很多横线（-）。",
            )
        }
        if (host.split('.').size >= 5 || host.length > 35) {
            add(
                15,
                "Alamat terlalu panjang / banyak bahagian — taktik menyamar.",
                "The address is too long / has too many parts — a disguise trick.",
                "地址太长或分段太多——这是伪装手法。",
            )
        }
        val lowerUrl = url.lowercase(Locale.ROOT)
        if (BAIT_IN_URL.any { lowerUrl.contains(it) }) {
            add(
                15,
                "Link ada perkataan umpan macam login / verify / hadiah.",
                "The link has bait words like login / verify / prize.",
                "链接里有诱饵字眼，例如 login / verify / 奖品。",
            )
        }
        if (context.isNotBlank()) {
            val ctx = context.lowercase(Locale.ROOT)
            val hits = SCAM_WORDS.count { ctx.contains(it) }
            if (hits > 0) {
                add(
                    minOf(30, hits * 10),
                    "Mesej guna ayat biasa scammer (menang hadiah, akaun disekat, segera, dll).",
                    "The message uses common scammer phrases (you won a prize, account blocked, urgent, etc.).",
                    "信息使用骗子常用的话（中奖、账户被冻结、紧急等）。",
                )
            }
        }
        return Verdict(url, host, score.coerceAtMost(100), reasons)
    }

    // ---------------------------------------------------------------- helpers

    fun extractHost(url: String): String {
        var s = url.trim().replace(Regex("(?i)^[a-z][a-z0-9+.-]*://"), "")
        s = s.substringBefore('/').substringBefore('?').substringBefore('#')
        if ('@' in s) s = s.substringAfterLast('@')
        s = s.substringBefore(':')
        return s.lowercase(Locale.ROOT).trimEnd('.')
    }

    private fun isTrusted(host: String): Boolean =
        host.isNotEmpty() && TRUSTED.any { host == it || host.endsWith(".$it") }

    private fun isAdult(host: String): Boolean {
        if (host.isEmpty()) return false
        if (ADULT_SITES.any { host == it || host.endsWith(".$it") }) return true
        if (host.substringAfterLast('.', "") in ADULT_TLDS) return true
        if (ADULT_WORDS.any { host.contains(it) }) return true
        return host.split('.', '-').any { it in ADULT_TOKENS }
    }

    private fun normalize(s: String): String =
        s.lowercase(Locale.ROOT)
            .replace("rn", "m").replace("vv", "w")
            .map {
                when (it) {
                    '0' -> 'o'; '1' -> 'l'; '3' -> 'e'; '4' -> 'a'; '5' -> 's'; '7' -> 't'; '@' -> 'a'
                    else -> it
                }
            }
            .joinToString("")

    private fun impersonatedBrand(host: String): String? {
        val tokens = host.split('.', '-').filter { it.isNotEmpty() }.map { normalize(it) }
        val compact = normalize(host.replace(".", "").replace("-", ""))
        for (b in BRANDS) {
            if (b.officials.any { host == it || host.endsWith(".$it") }) continue
            val hit = if (b.token.length <= 3) {
                tokens.any { it == b.token }
            } else {
                compact.contains(b.token) ||
                    tokens.any { it.length >= 5 && kotlin.math.abs(it.length - b.token.length) <= 1 && levenshtein(it, b.token) == 1 }
            }
            if (hit) return b.display
        }
        return null
    }

    private fun levenshtein(a: String, b: String): Int {
        val dp = IntArray(b.length + 1) { it }
        for (i in 1..a.length) {
            var prev = dp[0]
            dp[0] = i
            for (j in 1..b.length) {
                val tmp = dp[j]
                dp[j] = minOf(dp[j] + 1, dp[j - 1] + 1, prev + if (a[i - 1] == b[j - 1]) 0 else 1)
                prev = tmp
            }
        }
        return dp[b.length]
    }
}
