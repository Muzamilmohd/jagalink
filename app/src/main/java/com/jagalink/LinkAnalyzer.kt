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

    /**
     * BANK   – bank, bank digital, e-wallet, pembayaran: ejaan hampir sama pun dikira BAHAYA.
     * OTHER  – kerajaan, kurier, kedai online, utiliti: ejaan hampir sama dikira MENCURIGAKAN.
     * SOCIAL – app sosial: hanya nama tepat (elak salah tangkap perkataan biasa, cth "telegraph").
     */
    private enum class Kind { BANK, OTHER, SOCIAL }

    /** [keywords] = nama jenama + nama domain rasmi (cth "maybank2u") — semua dibanding dengan alamat link. */
    private data class Brand(val display: String, val keywords: List<String>, val officials: List<String>, val kind: Kind)

    // Laman rasmi DISAHKAN wujud (HTTPS / DNS, Okt 2026). Jangan tambah domain yang belum disahkan:
    // kalau domain tu belum didaftar, scammer boleh daftar dan app akan anggap ia selamat.
    private val BRANDS = listOf(
        // ---- Bank
        Brand("Maybank", listOf("maybank", "maybank2u", "maybank2e"),
            listOf("maybank2u.com.my", "maybank.com", "maybank.com.my", "maybank2e.net", "maybank2e.com",
                "maybank.com.sg", "maybank2u.com.sg", "maybank.co.id"), Kind.BANK),
        Brand("CIMB", listOf("cimb", "cimbclicks", "cimbbank"),
            listOf("cimbclicks.com.my", "cimb.com.my", "cimb.com", "cimbislamic.com",
                "cimb.com.sg", "cimbthai.com", "cimbniaga.co.id"), Kind.BANK),
        Brand("Public Bank", listOf("publicbank", "pbebank", "publicislamic"),
            listOf("pbebank.com", "publicbank.com.my", "publicislamicbank.com.my"), Kind.BANK),
        Brand("RHB", listOf("rhb", "rhbbank", "rhbnow", "rhbgroup"), listOf("rhbgroup.com", "rhb.com.my", "rhbgroup.com.sg"), Kind.BANK),
        Brand("Hong Leong Bank", listOf("hongleong", "hlbank", "hlbconnect", "hongleongconnect"),
            listOf("hlb.com.my", "hongleongconnect.my"), Kind.BANK),
        Brand("AmBank", listOf("ambank"), listOf("ambank.com.my", "ambankgroup.com", "amonline.com.my"), Kind.BANK),
        Brand("Bank Islam", listOf("bankislam"), listOf("bankislam.com"), Kind.BANK),
        Brand("Bank Rakyat", listOf("bankrakyat", "irakyat"), listOf("bankrakyat.com.my", "irakyat.com.my"), Kind.BANK),
        Brand("BSN", listOf("bsn", "mybsn"), listOf("bsn.com.my", "mybsn.com.my"), Kind.BANK),
        Brand("Affin Bank", listOf("affinbank", "affinalways"), listOf("affinalways.com"), Kind.BANK),
        Brand("Alliance Bank", listOf("alliancebank", "allianceonline"),
            listOf("alliancebank.com.my", "allianceonline.com.my"), Kind.BANK),
        Brand("Bank Muamalat", listOf("muamalat"), listOf("muamalat.com.my"), Kind.BANK),
        Brand("Agrobank", listOf("agrobank"), listOf("agrobank.com.my"), Kind.BANK),
        Brand("MBSB Bank", listOf("mbsb", "mbsbbank"), listOf("mbsb.com", "mbsb.com.my", "mbsbbank.com"), Kind.BANK),
        Brand("OCBC", listOf("ocbc"), listOf("ocbc.com.my", "ocbc.com", "ocbc.com.sg"), Kind.BANK),
        Brand("UOB", listOf("uob", "uobbank"), listOf("uob.com.my", "uob.com.sg"), Kind.BANK),
        Brand("HSBC", listOf("hsbc", "hsbcamanah"), listOf("hsbc.com.my", "hsbcamanah.com.my", "hsbc.com", "hsbcnet.com"), Kind.BANK),
        Brand("Standard Chartered", listOf("standardchartered"), listOf("sc.com"), Kind.BANK),
        Brand("Citibank", listOf("citibank"), listOf("citibank.com.my"), Kind.BANK),
        Brand("Al Rajhi Bank", listOf("alrajhi"), listOf("alrajhibank.com.my"), Kind.BANK),
        Brand("Kuwait Finance House", listOf("kfh"), listOf("kfh.com.my"), Kind.BANK),
        // ---- Bank digital
        Brand("GXBank", listOf("gxbank"), listOf("gxbank.my"), Kind.BANK),
        Brand("Boost", listOf("boostbank", "myboost"), listOf("boostbank.my", "myboost.com.my", "myboost.co"), Kind.BANK),
        Brand("AEON Bank", listOf("aeonbank"), listOf("aeonbank.com.my"), Kind.BANK),
        Brand("KAF Digital Bank", listOf("kafdigital", "kafbank"), listOf("kaf.com.my"), Kind.BANK),
        Brand("Ryt Bank", listOf("rytbank"), listOf("rytbank.my"), Kind.BANK),
        // ---- E-wallet & pembayaran
        Brand("Touch 'n Go", listOf("touchngo", "tngdigital", "tng", "tngo"),
            listOf("touchngo.com.my", "tngdigital.com.my"), Kind.BANK),
        Brand("BigPay", listOf("bigpay"), listOf("bigpayme.com"), Kind.BANK),
        Brand("ShopeePay", listOf("shopeepay"), listOf("shopeepay.com.my"), Kind.BANK),
        Brand("DuitNow / FPX", listOf("duitnow", "paynet", "mepsfpx"),
            listOf("paynet.my", "duitnow.my", "mepsfpx.com.my"), Kind.BANK),
        // ---- Kerajaan (semua *.gov.my memang dipercayai)
        Brand("LHDN", listOf("lhdn", "myhasil"), listOf("hasil.gov.my"), Kind.OTHER),
        Brand("KWSP", listOf("kwsp", "epf", "ikwsp"), listOf("kwsp.gov.my"), Kind.OTHER),
        Brand("JPJ", listOf("jpj", "myjpj"), listOf("jpj.gov.my"), Kind.OTHER),
        Brand("PDRM", listOf("pdrm"), listOf("rmp.gov.my"), Kind.OTHER),
        Brand("PERKESO", listOf("perkeso"), listOf("perkeso.gov.my"), Kind.OTHER),
        Brand("MyEG", listOf("myeg"), listOf("myeg.com.my"), Kind.OTHER),
        // ---- Kurier, kedai online, utiliti
        Brand("Pos Malaysia", listOf("poslaju", "posmalaysia"), listOf("pos.com.my"), Kind.OTHER),
        Brand("J&T", listOf("jnt", "jtexpress"), listOf("jtexpress.my"), Kind.OTHER),
        Brand("Ninja Van", listOf("ninjavan"), listOf("ninjavan.co"), Kind.OTHER),
        Brand("DHL", listOf("dhl"), listOf("dhl.com", "dhl.com.my"), Kind.OTHER),
        Brand("Shopee", listOf("shopee"), listOf("shopee.com.my", "shopee.com", "shope.ee"), Kind.OTHER),
        Brand("Lazada", listOf("lazada"), listOf("lazada.com.my", "lazada.com"), Kind.OTHER),
        Brand("TNB", listOf("tnb", "mytnb"), listOf("tnb.com.my", "mytnb.com.my"), Kind.OTHER),
        Brand("Unifi", listOf("unifi"), listOf("unifi.com.my", "tm.com.my"), Kind.SOCIAL),
        Brand("Setel", listOf("setel"), listOf("setel.com"), Kind.SOCIAL),
        // ---- Sosial & teknologi — nama tepat sahaja
        Brand("WhatsApp", listOf("whatsapp"), listOf("whatsapp.com", "whatsapp.net", "wa.me"), Kind.SOCIAL),
        Brand("Facebook", listOf("facebook"), listOf("facebook.com", "fb.com", "fb.watch"), Kind.SOCIAL),
        Brand("Instagram", listOf("instagram"), listOf("instagram.com"), Kind.SOCIAL),
        Brand("Google", listOf("google"), listOf("google.com", "google.com.my", "goo.gl"), Kind.SOCIAL),
        Brand("YouTube", listOf("youtube"), listOf("youtube.com", "youtu.be"), Kind.SOCIAL),
        Brand("Netflix", listOf("netflix"), listOf("netflix.com"), Kind.SOCIAL),
        Brand("Telegram", listOf("telegram"), listOf("telegram.org", "t.me"), Kind.SOCIAL),
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

        // Laman di platform percuma: domain mungkin "dipercayai", tapi kandungannya dibuat sesiapa.
        val path = url.replace(Regex("(?i)^[a-z][a-z0-9+.-]*://"), "").substringAfter('/', "").substringBefore('#')
        val hosted = hostedPlatform(host, path.lowercase(Locale.ROOT))
        if (hosted != null) {
            val (platform, isForm) = hosted
            val inPath = brandInText(path)
            if (inPath != null) {
                val brand = inPath.brand.display
                add(
                    when (inPath.brand.kind) { Kind.BANK -> 70; Kind.OTHER -> 50; Kind.SOCIAL -> 30 },
                    "Laman percuma ($platform) guna nama \"$brand\" — $brand sebenar takkan guna laman macam ni.",
                    "A free $platform page using the name \"$brand\" — the real $brand never uses pages like this.",
                    "免费的 $platform 页面使用「$brand」的名字——真正的 $brand 不会使用这种页面。",
                )
            }
            if (isForm) {
                add(
                    35,
                    "Ini borang online ($platform). Jangan sekali-kali isi kata laluan, OTP/TAC, nombor kad atau IC.",
                    "This is an online form ($platform). Never enter passwords, OTP/TAC codes, card or IC numbers.",
                    "这是在线表格（$platform）。千万不要填写密码、OTP/TAC 验证码、银行卡或身份证号码。",
                )
            } else if (inPath == null) {
                add(
                    10,
                    "Laman ni dibuat di platform percuma ($platform) — sesiapa pun boleh buat laman macam ni.",
                    "This page is on a free platform ($platform) — anyone can make a page like this.",
                    "这个页面在免费平台（$platform）上——任何人都能做这样的页面。",
                )
            }
        } else if (isTrusted(host)) {
            return Verdict(url, host, score.coerceAtMost(100), reasons)
        }

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
        impersonatedBrand(host)?.let { hit ->
            val brand = hit.brand.display
            if (hit.exact) {
                add(
                    65,
                    "Guna nama \"$brand\" tapi BUKAN laman rasmi $brand.",
                    "Uses the name \"$brand\" but is NOT the official $brand site.",
                    "使用「$brand」的名字，但不是 $brand 的官方网站。",
                )
            } else {
                // Ejaan tipu: bank/e-wallet terus BAHAYA; yang lain MENCURIGAKAN (elak salah tangkap perkataan biasa)
                add(
                    if (hit.brand.kind == Kind.BANK) 60 else 40,
                    "Alamat ni hampir sama dengan \"$brand\" (ejaan tipu) — BUKAN laman rasmi $brand.",
                    "This address looks almost like \"$brand\" (trick spelling) — it is NOT the official $brand site.",
                    "这个地址和「$brand」几乎一样（假拼写）——不是 $brand 的官方网站。",
                )
            }
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

    // Platform laman / borang percuma — sesiapa boleh buat laman di sini, walaupun domainnya "dipercayai"
    // (cth sites.google.com). Nama laman / laluan dipilih sendiri oleh pembuat, jadi mesti disemak.
    private val SUBDOMAIN_HOSTS = mapOf(
        "blogspot.com" to "Blogspot", "wixsite.com" to "Wix", "weebly.com" to "Weebly", "wordpress.com" to "WordPress",
        "github.io" to "GitHub Pages", "netlify.app" to "Netlify", "vercel.app" to "Vercel", "web.app" to "Firebase",
        "firebaseapp.com" to "Firebase", "pages.dev" to "Cloudflare Pages", "glitch.me" to "Glitch",
        "notion.site" to "Notion", "carrd.co" to "Carrd", "godaddysites.com" to "GoDaddy", "square.site" to "Square",
        "canva.site" to "Canva", "webflow.io" to "Webflow", "framer.website" to "Framer",
    )

    /** (nama platform, borang?) kalau link ni laman di platform percuma; null kalau bukan. */
    private fun hostedPlatform(host: String, path: String): Pair<String, Boolean>? = when {
        host == "sites.google.com" -> "Google Sites" to false
        host == "docs.google.com" && path.startsWith("forms") -> "Google Forms" to true
        host == "forms.gle" -> "Google Forms" to true
        host == "forms.office.com" || host == "forms.microsoft.com" -> "Microsoft Forms" to true
        host == "jotform.com" || host.endsWith(".jotform.com") -> "Jotform" to true
        host.endsWith(".typeform.com") -> "Typeform" to true
        host == "linktr.ee" -> "Linktree" to false
        else -> SUBDOMAIN_HOSTS.entries.firstOrNull { host.endsWith(".${it.key}") }?.let { it.value to false }
    }

    private val PUBLIC_SUFFIXES = setOf(
        "com.my", "net.my", "org.my", "gov.my", "edu.my", "mil.my", "name.my", "biz.my",
        "co.uk", "com.sg", "com.au", "co.id", "com.ph", "co.th", "com.cn", "com.hk", "co.in",
    )

    // Kata kunci yang dekat dengan perkataan biasa (rakyat, payment, online…) — nama tepat sahaja, tanpa ejaan hampir.
    private val EXACT_ONLY = setOf("irakyat", "paynet", "myhasil", "ikwsp", "myjpj", "mytnb", "bigpay")

    private class Impersonation(val brand: Brand, val exact: Boolean)

    /**
     * Cari jenama yang ditiru. Nama tepat dalam alamat (cth "maybank-secure.xyz") = exact.
     * Ejaan hampir sama (cth "meybank2u", "meybenk2u", "cimbclick") = bukan exact.
     */
    private fun impersonatedBrand(host: String): Impersonation? {
        val labels = host.split('.').filter { it.isNotEmpty() }
        val suffixLen = if (labels.size >= 3 && labels.takeLast(2).joinToString(".") in PUBLIC_SUFFIXES) 2 else 1
        val core = labels.dropLast(suffixLen).ifEmpty { labels }
        val tokens = core.flatMap { it.split('-') }.filter { it.isNotEmpty() }.map { normalize(it) }
        val compact = normalize(core.joinToString("").replace("-", ""))
        return matchBrands(tokens, compact) { b -> b.officials.any { host == it || host.endsWith(".$it") } }
    }

    /**
     * Cari nama jenama dalam teks bebas — bahagian link yang pengguna laman boleh pilih sendiri
     * (cth "/view/maybank2u-login" di Google Sites). Token panjang (ID rawak borang) diabaikan.
     */
    private fun brandInText(text: String): Impersonation? {
        val tokens = text.lowercase(Locale.ROOT).split(Regex("[^a-z0-9]+"))
            .filter { it.isNotEmpty() && it.length < 20 }
            .map { normalize(it) }
        if (tokens.isEmpty()) return null
        return matchBrands(tokens, tokens.joinToString("")) { false }
    }

    private fun matchBrands(tokens: List<String>, compact: String, skip: (Brand) -> Boolean): Impersonation? {
        var fuzzy: Impersonation? = null
        for (b in BRANDS) {
            if (skip(b)) continue
            for (raw in b.keywords) {
                val k = normalize(raw)
                val exact = when {
                    k.length <= 3 -> tokens.any { it == k }
                    else -> compact.contains(k)
                }
                if (exact) return Impersonation(b, exact = true)
                if (fuzzy == null && b.kind != Kind.SOCIAL && k.length >= 5 && raw !in EXACT_ONLY) {
                    val maxDist = if (k.length >= 8) 2 else 1
                    if (nearMatch(compact, k, maxDist)) fuzzy = Impersonation(b, exact = false)
                }
            }
        }
        return fuzzy
    }

    /**
     * Ada bahagian [s] (panjang k±1) yang berbeza ≤ [maxDist] huruf daripada [k]?
     * Beza 2 huruf hanya dikira untuk huruf ditukar (panjang sama) dan huruf pertama sama — ejaan tipu
     * biasanya begitu (cth "meybenk2u") — elak salah tangkap seperti "rakyatdigital" ≈ "kafdigital".
     */
    private fun nearMatch(s: String, k: String, maxDist: Int): Boolean {
        for (len in (k.length - 1)..(k.length + 1)) {
            if (len <= 0 || len > s.length) continue
            for (i in 0..(s.length - len)) {
                val w = s.substring(i, i + len)
                val d = levenshtein(w, k)
                if (d <= 1 || (d <= maxDist && len == k.length && w[0] == k[0])) return true
            }
        }
        return false
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
