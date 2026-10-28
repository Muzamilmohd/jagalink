package com.jagalink

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
