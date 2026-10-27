package com.jagalink

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.text.InputType
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast

/** Skrin tetapan — anak yang setup sekali, lepas tu bot jaga sendiri. */
class MainActivity : Activity() {

    private lateinit var statusBox: LinearLayout
    private lateinit var resultText: TextView
    private val grey = Color.rgb(97, 97, 97)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val scroll = ScrollView(this)
        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(24), dp(20), dp(48))
        }
        scroll.addView(col)

        col.addView(BotView(this, Level.SAFE), LinearLayout.LayoutParams(dp(120), dp(120)).apply {
            gravity = Gravity.CENTER_HORIZONTAL
        })
        col.addView(label("JagaLink", 30f, bold = true, center = true))
        col.addView(
            label(
                tr("Bot kecil yang jaga daripada link scam", "A little bot that guards against scam links", "防范诈骗链接的小机器人"),
                15f, color = grey, center = true
            )
        )

        if (Prefs.inSecondProfile()) {
            col.addView(
                label(
                    tr(
                        "ℹ️ Ini tetapan JagaLink untuk WhatsApp kedua (Dual Messenger). Isi nama & nombor anak di bahagian 2, kemudian tekan Simpan.",
                        "ℹ️ These are JagaLink settings for the second WhatsApp (Dual Messenger). Fill in the name & child's number in section 2, then tap Save.",
                        "ℹ️ 这是第二个 WhatsApp（双开）的 JagaLink 设置。请在第 2 部分填写称呼和孩子的号码，然后点击保存。",
                    ),
                    15f, color = Color.rgb(25, 118, 210)
                ).apply { setPadding(0, dp(16), 0, 0) }
            )
        }

        // Pilih bahasa — label tetap dalam tiga bahasa supaya sesiapa boleh jumpa
        col.addView(header("🌐 Bahasa / Language / 语言"))
        val langRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val current = Prefs.lang(this)
        for ((lang, name) in listOf(Lang.MS to "Melayu", Lang.EN to "English", Lang.ZH to "中文")) {
            langRow.addView(
                button(if (lang == current) "✓ $name" else name) {
                    if (lang != current) {
                        Prefs.setLang(this, lang)
                        recreate()
                    }
                }.apply { if (lang == current) setTypeface(typeface, Typeface.BOLD) },
                LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
            )
        }
        col.addView(langRow)

        col.addView(header(tr("1. Hidupkan pengawal", "1. Turn on protection", "1. 开启保护")))
        statusBox = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        col.addView(statusBox)
        col.addView(
            label(
                tr(
                    "Kalau keluar \"Tetapan terhad / Restricted setting\": buka Info Aplikasi JagaLink → ⋮ (atas kanan) → " +
                        "\"Benarkan tetapan terhad\", kemudian cuba lagi.",
                    "If you see \"Restricted setting\": open JagaLink's App info → ⋮ (top right) → " +
                        "\"Allow restricted settings\", then try again.",
                    "如果出现「受限设置 / Restricted setting」：打开 JagaLink 的应用信息 → ⋮（右上角）→ " +
                        "「允许受限设置」，然后再试一次。",
                ),
                13f, color = grey
            )
        )

        col.addView(header(tr("2. Tetapan", "2. Settings", "2. 设置")))
        val nameEt = edit(tr("Panggilan (cth: Mak)", "What to call them (e.g. Mum)", "称呼（例如：妈妈）"), Prefs.callName(this))
        val phoneEt = edit(
            tr("Nombor telefon anak", "Child's phone number", "孩子的电话号码"),
            Prefs.phone(this), InputType.TYPE_CLASS_PHONE
        )
        val voiceSw = Switch(this).apply {
            text = tr("Bot bercakap (suara amaran)", "Bot speaks (voice warning)", "机器人说话（语音警告）")
            textSize = 16f
            isChecked = Prefs.voice(this@MainActivity)
        }
        // Label tetap di atas kotak — hint hilang bila kotak dah ada nama
        col.addView(
            label(
                tr(
                    "Bot panggil pengguna sebagai (boleh tukar, cth: Mak, Ayah, Atuk, Nenek)",
                    "The bot calls the user (you can change it, e.g. Mum, Dad, Grandma)",
                    "机器人对用户的称呼（可以修改，例如：妈妈、爸爸、奶奶）",
                ),
                14f, color = grey
            )
        )
        col.addView(nameEt)
        col.addView(
            label(tr("Nombor telefon anak", "Child's phone number", "孩子的电话号码"), 14f, color = grey)
                .apply { setPadding(0, dp(12), 0, 0) }
        )
        col.addView(phoneEt)
        col.addView(voiceSw)
        col.addView(button(tr("Simpan", "Save", "保存")) {
            Prefs.save(this, nameEt.text.toString(), phoneEt.text.toString(), voiceSw.isChecked)
            toast(tr("Disimpan 👍", "Saved 👍", "已保存 👍"))
        })

        col.addView(header(tr("3. Uji", "3. Test", "3. 测试")))
        val testEt = edit(
            tr("Tampal link atau mesej di sini", "Paste a link or message here", "在这里粘贴链接或信息"), "",
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
        )
        col.addView(testEt)
        resultText = label("", 14f)
        col.addView(button(tr("Semak link", "Check link", "检查链接")) { runTest(testEt.text.toString()) })
        col.addView(button(tr("Tunjuk bot amaran (contoh)", "Show warning bot (example)", "显示警告机器人（示例）")) {
            val sample = tr(
                "Tahniah! Akaun anda akan disekat. Sahkan segera: http://maybank2u-secure.xyz/login",
                "Congratulations! Your account will be blocked. Verify now: http://maybank2u-secure.xyz/login",
                "恭喜！您的账户将被冻结。请立即验证：http://maybank2u-secure.xyz/login",
            )
            BotOverlay.show(
                this, LinkAnalyzer.analyzeMessage(sample, Prefs.lang(this)).first(),
                tr("Contoh", "Example", "示例")
            )
            if (!Settings.canDrawOverlays(this)) {
                toast(
                    tr(
                        "Hidupkan 'Pop-up bot' dulu — sekarang keluar sebagai notifikasi",
                        "Turn on 'Bot pop-up' first — showing as a notification for now",
                        "请先开启「机器人弹窗」——现在先以通知显示",
                    )
                )
            }
        })
        col.addView(resultText)

        if (Updater.enabled) {
            col.addView(header(tr("4. Kemas kini", "4. Updates", "4. 更新")))
            col.addView(label(tr("Versi sekarang: ", "Current version: ", "当前版本：") + Updater.currentVersion, 15f, color = grey))
            updateText = label("", 14f)
            col.addView(button(tr("Semak kemas kini", "Check for updates", "检查更新")) { checkUpdate(quiet = false) })
            col.addView(Switch(this).apply {
                text = tr("Semak sendiri setiap hari", "Check automatically every day", "每天自动检查")
                textSize = 16f
                isChecked = Prefs.autoUpdate(this@MainActivity)
                setOnCheckedChangeListener { _, on ->
                    Prefs.setAutoUpdate(this@MainActivity, on)
                    Updater.schedule(this@MainActivity)
                }
            })
            col.addView(updateText)

            Updater.schedule(this)
            val fromNotification = intent?.getBooleanExtra(EXTRA_CHECK_UPDATE, false) == true
            val stale = System.currentTimeMillis() - Prefs.lastUpdateCheck(this) > 12 * 60 * 60_000L
            if (fromNotification || (Prefs.autoUpdate(this) && stale)) checkUpdate(quiet = !fromNotification)
        }

        setContentView(scroll)
    }

    // ------------------------------------------------------------ kemas kini

    private var updateText: TextView? = null

    private fun checkUpdate(quiet: Boolean) {
        if (!quiet) updateText?.text = tr("Sedang semak…", "Checking…", "正在检查…")
        Updater.check { rel, err ->
            if (isFinishing) return@check
            if (err == null) Prefs.setLastUpdateCheck(this, System.currentTimeMillis())
            when {
                err != null -> if (!quiet) updateText?.text =
                    tr("Tak dapat semak (internet?): ", "Couldn't check (internet?): ", "无法检查（网络？）：") + err.message
                rel == null -> if (!quiet) updateText?.text =
                    tr("✅ Dah versi terkini.", "✅ You have the latest version.", "✅ 已是最新版本。")
                else -> {
                    updateText?.text = tr("Versi baru ada: ", "New version available: ", "有新版本：") + rel.version
                    showUpdateDialog(rel)
                }
            }
        }
    }

    private fun showUpdateDialog(rel: Updater.Release) {
        AlertDialog.Builder(this)
            .setTitle(tr("Versi baru ${rel.version}", "New version ${rel.version}", "新版本 ${rel.version}"))
            .setMessage(rel.notes.ifBlank { tr("Ada kemas kini untuk JagaLink.", "An update for JagaLink is available.", "JagaLink 有可用更新。") })
            .setPositiveButton(tr("Kemas kini", "Update", "更新")) { _, _ -> startInstall(rel) }
            .setNegativeButton(tr("Nanti", "Later", "稍后"), null)
            .show()
    }

    private fun startInstall(rel: Updater.Release) {
        if (!Updater.canInstall(this)) {
            toast(
                tr(
                    "Benarkan JagaLink pasang app, kemudian tekan Kemas kini sekali lagi",
                    "Allow JagaLink to install apps, then tap Update again",
                    "请允许 JagaLink 安装应用，然后再点一次更新",
                )
            )
            startActivity(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:$packageName")))
            return
        }
        updateText?.text = tr("Muat turun… 0%", "Downloading… 0%", "下载中… 0%")
        Updater.install(
            this, rel,
            onProgress = { pct -> updateText?.text = tr("Muat turun… $pct%", "Downloading… $pct%", "下载中… $pct%") },
            onError = { e -> updateText?.text = tr("Muat turun gagal: ", "Download failed: ", "下载失败：") + e.message },
        )
    }

    companion object {
        const val EXTRA_CHECK_UPDATE = "check_update"
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (Updater.enabled && intent.getBooleanExtra(EXTRA_CHECK_UPDATE, false)) checkUpdate(quiet = false)
    }

    override fun onResume() {
        super.onResume()
        refreshStatus()
    }

    // ------------------------------------------------------------ status

    private fun refreshStatus() {
        statusBox.removeAllViews()
        row(tr("Pop-up bot atas skrin", "Bot pop-up on screen", "屏幕上的机器人弹窗"), Settings.canDrawOverlays(this)) {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
        }
        row(
            tr(
                "Baca mesej masuk (WhatsApp, Telegram, SMS, emel)",
                "Read incoming messages (WhatsApp, Telegram, SMS, email)",
                "读取收到的信息（WhatsApp、Telegram、短信、电邮）",
            ),
            notifEnabled()
        ) {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }
        row(
            tr("Baca skrin (Kebolehcapaian → JagaLink)", "Read screen (Accessibility → JagaLink)", "读取屏幕（无障碍 → JagaLink）"),
            a11yEnabled()
        ) {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        row(
            tr("Pengawal link (jadikan browser utama)", "Link guard (set as default browser)", "链接守卫（设为默认浏览器）"),
            isDefaultBrowser()
        ) { requestBrowserRole() }
        row(tr("Jangan dimatikan di latar belakang", "Keep running in the background", "保持在后台运行"), ignoringBattery()) {
            try {
                startActivity(
                    Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:$packageName"))
                )
            } catch (_: Exception) {
                startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
            }
        }
        if (Build.VERSION.SDK_INT >= 33) {
            val ok = checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            row(tr("Notifikasi amaran", "Warning notifications", "警告通知"), ok) { requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1) }
        }
    }

    private fun notifEnabled(): Boolean =
        Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
            ?.contains(packageName) == true

    private fun a11yEnabled(): Boolean =
        Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
            ?.contains("$packageName/") == true

    private fun ignoringBattery(): Boolean =
        getSystemService(PowerManager::class.java).isIgnoringBatteryOptimizations(packageName)

    private fun isDefaultBrowser(): Boolean {
        if (Build.VERSION.SDK_INT >= 29) {
            return getSystemService(RoleManager::class.java).isRoleHeld(RoleManager.ROLE_BROWSER)
        }
        val r = packageManager.resolveActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse("https://example.com")),
            PackageManager.MATCH_DEFAULT_ONLY
        )
        return r?.activityInfo?.packageName == packageName
    }

    @Suppress("DEPRECATION")
    private fun requestBrowserRole() {
        try {
            if (Build.VERSION.SDK_INT >= 29) {
                val rm = getSystemService(RoleManager::class.java)
                if (rm.isRoleAvailable(RoleManager.ROLE_BROWSER)) {
                    startActivityForResult(rm.createRequestRoleIntent(RoleManager.ROLE_BROWSER), 2)
                    return
                }
            }
            startActivity(Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS))
        } catch (_: Exception) {
            startActivity(Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS))
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2 && !isDefaultBrowser()) {
            // Kalau dialog tak keluar (pernah ditolak), buka tetapan app lalai terus
            toast(tr("Pilih JagaLink sebagai 'Aplikasi pelayar'", "Choose JagaLink as the 'Browser app'", "请选择 JagaLink 作为「浏览器应用」"))
            startActivity(Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS))
        }
    }

    // ------------------------------------------------------------ uji

    private fun runTest(input: String) {
        val verdicts = LinkAnalyzer.analyzeMessage(input, Prefs.lang(this))
        if (verdicts.isEmpty()) {
            resultText.text = tr("Tiada link atau fail APK dijumpai.", "No link or APK file found.", "没有发现链接或 APK 文件。")
            return
        }
        resultText.text = verdicts.joinToString("\n\n") { v ->
            val icon = if (v.adult) tr("🚫 DISEKAT (lucah)", "🚫 BLOCKED (porn)", "🚫 已拦截（色情）") else when (v.level) {
                Level.DANGER -> tr("🔴 BAHAYA", "🔴 DANGER", "🔴 危险")
                Level.WARN -> tr("🟠 MENCURIGAKAN", "🟠 SUSPICIOUS", "🟠 可疑")
                Level.SAFE -> tr("🟢 Nampak selamat", "🟢 Looks safe", "🟢 看起来安全")
            }
            val score = tr("skor", "score", "分数")
            "$icon  ($score ${v.score}/100)\n${v.url}" +
                if (v.reasons.isEmpty()) "" else "\n" + v.reasons.joinToString("\n") { "• $it" }
        }
        verdicts.filter { it.level != Level.SAFE }.maxByOrNull { it.score }?.let {
            BotOverlay.show(this, it, tr("Ujian", "Test", "测试"))
        }
    }

    // ------------------------------------------------------------ ui helpers

    private fun row(title: String, ok: Boolean, onFix: () -> Unit) {
        val r = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(6), 0, dp(6))
        }
        r.addView(
            label((if (ok) "✅  " else "❌  ") + title, 16f),
            LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
        )
        if (!ok) r.addView(Button(this).apply { text = tr("Hidupkan", "Turn on", "开启"); setOnClickListener { onFix() } })
        statusBox.addView(r)
    }

    private fun header(s: String) = label(s, 20f, bold = true).apply { setPadding(0, dp(24), 0, dp(8)) }

    private fun label(s: String, sp: Float, bold: Boolean = false, color: Int = Color.rgb(33, 33, 33), center: Boolean = false) =
        TextView(this).apply {
            text = s
            textSize = sp
            setTextColor(color)
            if (bold) setTypeface(typeface, Typeface.BOLD)
            if (center) gravity = Gravity.CENTER
        }

    private fun edit(hint: String, value: String, type: Int = InputType.TYPE_CLASS_TEXT) =
        EditText(this).apply {
            this.hint = hint
            setText(value)
            inputType = type
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        }

    private fun button(s: String, onClick: () -> Unit) =
        Button(this).apply {
            text = s
            setOnClickListener { onClick() }
        }

    private fun toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
}
