package com.jagalink

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import java.util.Locale

fun Context.dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

/** Pilih teks ikut bahasa yang dipilih dalam tetapan. */
fun Context.tr(ms: String, en: String, zh: String): String = Prefs.lang(this).t(ms, en, zh)

/** Tetapan ringkas. */
object Prefs {
    private fun sp(c: Context) = c.getSharedPreferences("jagalink", Context.MODE_PRIVATE)
    fun lang(c: Context): Lang = Lang.of(sp(c).getString("lang", null))
    fun setLang(c: Context, lang: Lang) {
        sp(c).edit().putString("lang", lang.code).apply()
    }
    fun callName(c: Context): String =
        sp(c).getString("name", "").orEmpty().ifBlank { c.tr("Mak", "Mum", "妈妈") }
    fun phone(c: Context): String = sp(c).getString("phone", "").orEmpty()
    fun voice(c: Context): Boolean = sp(c).getBoolean("voice", true)
    fun isSetUp(c: Context): Boolean = sp(c).contains("name")

    fun autoUpdate(c: Context): Boolean = sp(c).getBoolean("auto_update", true)
    fun setAutoUpdate(c: Context, on: Boolean) {
        sp(c).edit().putBoolean("auto_update", on).apply()
    }
    fun notifiedVersion(c: Context): String = sp(c).getString("notified_version", "").orEmpty()
    fun setNotifiedVersion(c: Context, v: String) {
        sp(c).edit().putString("notified_version", v).apply()
    }
    fun lastUpdateCheck(c: Context): Long = sp(c).getLong("last_update_check", 0)
    fun setLastUpdateCheck(c: Context, t: Long) {
        sp(c).edit().putLong("last_update_check", t).apply()
    }

    /**
     * True dalam profil kedua (cth Dual Messenger Samsung, user 95). App dalam profil tu
     * buka link guna salinan JagaLink yang berasingan — tetapan pun berasingan.
     */
    fun inSecondProfile(): Boolean = android.os.Process.myUid() / 100_000 != 0
    fun save(c: Context, name: String, phone: String, voice: Boolean) {
        sp(c).edit()
            .putString("name", name.trim())
            .putString("phone", phone.trim())
            .putBoolean("voice", voice)
            .apply()
    }
}

/** App yang diawasi (mesti sama dengan senarai dalam res/xml/accessibility_config.xml). */
object Watched {
    val APPS = mapOf(
        "com.whatsapp" to "WhatsApp",
        "com.whatsapp.w4b" to "WhatsApp Business",
        "org.telegram.messenger" to "Telegram",
        "org.telegram.messenger.web" to "Telegram",
        "org.thunderdog.challegram" to "Telegram X",
        "com.google.android.apps.messaging" to "SMS",
        "com.samsung.android.messaging" to "SMS",
        "com.android.mms" to "SMS",
        "com.android.messaging" to "SMS",
        "com.facebook.katana" to "Facebook",
        "com.facebook.lite" to "Facebook Lite",
        "com.facebook.orca" to "Messenger",
        "com.facebook.mlite" to "Messenger Lite",
        "com.instagram.android" to "Instagram",
        "com.zhiliaoapp.musically" to "TikTok",
        "com.ss.android.ugc.trill" to "TikTok",
        "com.google.android.gm" to "Gmail",
        "com.microsoft.office.outlook" to "Outlook",
        "com.yahoo.mobile.client.android.mail" to "Yahoo Mail",
        "com.samsung.android.email.provider" to "Samsung Email",
        "jp.naver.line.android" to "LINE",
        "com.viber.voip" to "Viber",
        "com.tencent.mm" to "WeChat",
    )
}

/** Suara amaran ikut bahasa dipilih (Melayu → Indonesia jika tiada; English; 中文). */
object Speaker {
    private var tts: TextToSpeech? = null
    private var ready = false
    private var pending: String? = null
    private var pendingLang = Lang.MS

    fun say(ctx: Context, text: String) {
        val lang = Prefs.lang(ctx)
        val engine = tts
        if (engine != null && ready) {
            speak(engine, text, lang)
            return
        }
        pending = text
        pendingLang = lang
        if (engine == null) {
            val app = ctx.applicationContext
            tts = TextToSpeech(app) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts?.let { e ->
                        e.setSpeechRate(0.9f)
                        ready = true
                        pending?.let { msg -> speak(e, msg, pendingLang) }
                        pending = null
                    }
                } else {
                    // Tiada enjin suara (cth profil Dual Messenger Samsung) — bunyi amaran sebagai ganti,
                    // dan cuba semula lain kali.
                    tts?.shutdown()
                    tts = null
                    pending = null
                    Alerts.beep()
                }
            }
        }
    }

    private fun speak(e: TextToSpeech, text: String, lang: Lang) {
        val choices = when (lang) {
            Lang.MS -> listOf(Locale("ms", "MY"), Locale("id", "ID"))
            Lang.EN -> listOf(Locale("en", "GB"), Locale.US)
            Lang.ZH -> listOf(Locale.SIMPLIFIED_CHINESE, Locale.CHINESE)
        }
        val ok = choices.any {
            val r = e.setLanguage(it)
            r != TextToSpeech.LANG_MISSING_DATA && r != TextToSpeech.LANG_NOT_SUPPORTED
        }
        if (!ok || e.speak(text, TextToSpeech.QUEUE_FLUSH, null, "jagalink") != TextToSpeech.SUCCESS) {
            Alerts.beep()
        }
    }
}

object Alerts {
    @Suppress("DEPRECATION")
    fun buzz(ctx: Context) {
        try {
            val vib: Vibrator = if (Build.VERSION.SDK_INT >= 31) {
                ctx.getSystemService(VibratorManager::class.java).defaultVibrator
            } else {
                ctx.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            vib.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 300, 150, 300, 150, 500), -1))
        } catch (_: Exception) {
        }
    }

    /** Bunyi amaran pendek bila suara tak dapat keluar. */
    fun beep() {
        try {
            val tone = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
            tone.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 1500)
            Handler(Looper.getMainLooper()).postDelayed({ tone.release() }, 1800)
        } catch (_: Exception) {
        }
    }
}

/** Kalau pop-up tak dibenarkan, bagi notifikasi amaran sebagai ganti. */
object Notifier {
    private const val CH = "jagalink_warn"

    @SuppressLint("MissingPermission")
    fun warn(ctx: Context, v: Verdict, app: String?) {
        val nm = ctx.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(
                CH, ctx.tr("Amaran link scam", "Scam link warnings", "诈骗链接警告"),
                NotificationManager.IMPORTANCE_HIGH
            )
        )
        val pi = PendingIntent.getActivity(
            ctx, 0,
            Intent(ctx, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_IMMUTABLE
        )
        val where = if (app != null) " ($app)" else ""
        val body = v.url + "\n" + v.reasons.joinToString("\n") { "• $it" }
        val name = Prefs.callName(ctx)
        val title = if (v.adult) ctx.tr(
            "🚫 Link laman lucah disekat$where",
            "🚫 Porn site link blocked$where",
            "🚫 已拦截色情网站链接$where",
        ) else ctx.tr(
            "⚠️ $name, hati-hati! Link mungkin scam$where",
            "⚠️ $name, be careful! This link may be a scam$where",
            "⚠️ $name，小心！链接可能是诈骗$where",
        )
        val n = Notification.Builder(ctx, CH)
            .setSmallIcon(R.drawable.ic_bot_small)
            .setContentTitle(title)
            .setContentText(v.url)
            .setStyle(Notification.BigTextStyle().bigText(body))
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()
        try {
            nm.notify(v.url.hashCode(), n)
        } catch (_: SecurityException) {
        }
    }
}

/** Hantar link mencurigakan ke WhatsApp anak supaya dia boleh semak. */
object AskChild {

    /** "011-2345 678" / "+6011…" → "60112345678" (format antarabangsa tanpa +). */
    fun normalizePhone(raw: String): String {
        val digits = raw.filter { it.isDigit() }
        return when {
            digits.startsWith("60") -> digits
            digits.startsWith("0") -> "6$digits"
            else -> digits
        }
    }

    /**
     * Link "dijinakkan" — tak boleh ditekan dan WhatsApp tak buat preview
     * (preview akan buat phone Mak buka laman scam tu).
     */
    fun defang(url: String): String =
        url.replace(Regex("^(?i)http"), "hxxp").replace(".", "[.]")

    fun message(ctx: Context, v: Verdict, app: String?): String {
        val name = Prefs.callName(ctx)
        val where = app?.let { ctx.tr(" dalam $it", " in $it", "在 $it ") }.orEmpty()
        val what = when {
            v.adult -> ctx.tr("laman lucah (dah disekat)", "a porn site (blocked)", "色情网站（已拦截）")
            v.level == Level.DANGER -> ctx.tr("BAHAYA", "DANGEROUS", "很危险")
            else -> ctx.tr("mencurigakan", "suspicious", "可疑")
        }
        val link = defang(v.url)
        return ctx.tr(
            "Hai, $name dapat link ni$where. JagaLink kata ia $what. Boleh tolong semak? 🙏\n\n$link\n\n(Link sengaja diubah supaya tak terbuka bila ditekan.)",
            "Hi, $name got this link$where. JagaLink says it's $what. Can you check it? 🙏\n\n$link\n\n(The link was changed on purpose so it won't open when tapped.)",
            "你好，${name}${where}收到这个链接。JagaLink 说它$what。可以帮忙看看吗？🙏\n\n$link\n\n（链接已特意修改，点击不会打开。）",
        )
    }

    /** WhatsApp → WhatsApp Business → SMS. Pulangkan false kalau semua gagal. */
    fun send(ctx: Context, v: Verdict, app: String?): Boolean {
        val phone = normalizePhone(Prefs.phone(ctx))
        if (phone.isEmpty()) return false
        val text = message(ctx, v, app)
        val wa = Uri.parse("https://api.whatsapp.com/send?phone=$phone&text=${Uri.encode(text)}")
        for (pkg in listOf("com.whatsapp", "com.whatsapp.w4b")) {
            try {
                ctx.startActivity(Intent(Intent.ACTION_VIEW, wa).setPackage(pkg).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                return true
            } catch (_: Exception) {
            }
        }
        return try {
            ctx.startActivity(
                Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phone"))
                    .putExtra("sms_body", text)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            true
        } catch (_: Exception) {
            false
        }
    }
}

/** Cari browser sebenar (Chrome dsb) untuk buka link yang selamat. */
object Browsers {
    private val PREFERRED = listOf(
        "com.android.chrome", "com.sec.android.app.sbrowser", "com.mi.globalbrowser",
        "com.heytap.browser", "com.opera.browser", "org.mozilla.firefox",
        "com.microsoft.emmx", "com.brave.browser",
    )

    @Suppress("DEPRECATION")
    fun pick(ctx: Context): String? {
        val probe = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"))
            .addCategory(Intent.CATEGORY_BROWSABLE)
        val pkgs = ctx.packageManager.queryIntentActivities(probe, PackageManager.MATCH_ALL)
            .map { it.activityInfo.packageName }
            .filter { it != ctx.packageName }
            .distinct()
        return PREFERRED.firstOrNull { it in pkgs } ?: pkgs.firstOrNull()
    }
}
