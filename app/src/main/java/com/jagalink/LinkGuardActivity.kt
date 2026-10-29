package com.jagalink

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.widget.Toast

/**
 * Bila JagaPMA jadi browser utama, SETIAP link yang mak tekan (WhatsApp, SMS, FB…)
 * masuk sini dulu. Selamat → terus buka di Chrome. Mencurigakan → bot keluar.
 */
class LinkGuardActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val uri = intent?.data
        if (uri == null) {
            finish()
            return
        }
        val v = LinkAnalyzer.analyzeUrl(uri.toString(), lang = Prefs.lang(this))
        // Dah pasti bahaya, atau semakan Google dimatikan → terus guna keputusan dalam phone
        if (v.level == Level.DANGER || !SafeBrowsing.enabled(this)) {
            decide(uri, v)
            return
        }
        // Tanya Google Safe Browsing (hash sahaja). Tunjuk "sedang semak" kalau lambat.
        val waiting = Runnable {
            setContentView(
                BotUi.text(this, tr("🔍 Sedang semak link…", "🔍 Checking link…", "🔍 正在检查链接…"), 20f, Color.WHITE)
                    .apply { setBackgroundColor(Color.argb(170, 0, 0, 0)) }
            )
        }
        val main = Handler(mainLooper)
        main.postDelayed(waiting, 400)
        Thread {
            val threat = SafeBrowsing.check(this, uri.toString())
            runOnUiThread {
                main.removeCallbacks(waiting)
                if (isFinishing || isDestroyed) return@runOnUiThread
                if (threat == null) {
                    decide(uri, v)
                } else {
                    val why = tr(
                        "Google Safe Browsing kenal pasti ni sebagai ${SafeBrowsing.describe(this, threat)}.",
                        "Google Safe Browsing flags this as ${SafeBrowsing.describe(this, threat)}.",
                        "Google 安全浏览将其识别为${SafeBrowsing.describe(this, threat)}。",
                    )
                    decide(uri, Verdict(v.url, v.host, 100, listOf(why) + v.reasons))
                }
            }
        }.start()
    }

    /** Selamat → buka browser sebenar. Lain → kad amaran bot. */
    private fun decide(uri: Uri, v: Verdict) {
        if (v.level == Level.SAFE) {
            openInRealBrowser(uri)
            finish()
            return
        }
        Alerts.buzz(this)
        if (Prefs.voice(this)) Speaker.say(this, BotUi.spoken(this, v))
        setContentView(
            BotUi.build(
                this, v, app = null,
                onDismiss = { finish() },
                onProceed = {
                    openInRealBrowser(uri)
                    finish()
                },
                onSetup = if (Prefs.inSecondProfile() && !Prefs.isSetUp(this)) {
                    {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                } else null,
            )
        )
    }

    private fun openInRealBrowser(uri: Uri) {
        val pkg = Browsers.pick(this)
        if (pkg == null) {
            val msg = tr(
                "Tiada browser lain dijumpai (cth Chrome).",
                "No other browser found (e.g. Chrome).",
                "找不到其他浏览器（例如 Chrome）。",
            )
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
            return
        }
        try {
            startActivity(
                Intent(Intent.ACTION_VIEW, uri)
                    .setPackage(pkg)
                    .addCategory(Intent.CATEGORY_BROWSABLE)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (_: Exception) {
            Toast.makeText(this, tr("Tak dapat buka link.", "Couldn't open the link.", "无法打开链接。"), Toast.LENGTH_SHORT).show()
        }
    }
}
