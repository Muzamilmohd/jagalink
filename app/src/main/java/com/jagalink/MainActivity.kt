package com.jagalink

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.WindowInsets
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
    private var scrollView: ScrollView? = null
    private var nameField: EditText? = null
    private val grey = Color.rgb(97, 97, 97)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Blocklist.ensureLoaded(this)
        Blocklist.refreshIfStale(this)
        val scroll = ScrollView(this)
        scrollView = scroll
        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(24), dp(20), dp(48))
        }
        scroll.addView(col)
        // Android 15+ lukis app sampai bawah status bar — tolak kandungan supaya tak bertindih
        if (Build.VERSION.SDK_INT >= 35) {
            scroll.setOnApplyWindowInsetsListener { _, insets ->
                val bars = insets.getInsets(WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout())
                col.setPadding(dp(20) + bars.left, dp(24) + bars.top, dp(20) + bars.right, dp(48) + bars.bottom)
                insets
            }
        }

        col.addView(BotView(this, Level.SAFE), LinearLayout.LayoutParams(dp(120), dp(120)).apply {
            gravity = Gravity.CENTER_HORIZONTAL
        })
        col.addView(label("JagaPMA", 30f, bold = true, center = true))
        col.addView(label("Jaga Phone Mak Ayah", 17f, bold = true, color = Color.rgb(46, 125, 50), center = true))
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
                        "ℹ️ Ini tetapan JagaPMA untuk WhatsApp kedua (Dual Messenger). Isi nama & nombor anak di bahagian 2, kemudian tekan Simpan.",
                        "ℹ️ These are JagaPMA settings for the second WhatsApp (Dual Messenger). Fill in the name & child's number in section 2, then tap Save.",
                        "ℹ️ 这是第二个 WhatsApp（双开）的 JagaPMA 设置。请在第 2 部分填写称呼和孩子的号码，然后点击保存。",
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
        // "Tetapan terhad" hanya kena pada Baca skrin, yang tiada dalam versi Play
        if (!BuildConfig.PLAY) col.addView(
            label(
                tr(
                    "Kalau keluar \"Tetapan terhad / Restricted setting\": buka Info Aplikasi JagaPMA → ⋮ (atas kanan) → " +
                        "\"Benarkan tetapan terhad\", kemudian cuba lagi.",
                    "If you see \"Restricted setting\": open JagaPMA's App info → ⋮ (top right) → " +
                        "\"Allow restricted settings\", then try again.",
                    "如果出现「受限设置 / Restricted setting」：打开 JagaPMA 的应用信息 → ⋮（右上角）→ " +
                        "「允许受限设置」，然后再试一次。",
                ),
                13f, color = grey
            )
        )

        col.addView(header(tr("2. Tetapan", "2. Settings", "2. 设置")))
        val nameEt = edit(tr("Panggilan (cth: Mak)", "What to call them (e.g. Mum)", "称呼（例如：妈妈）"), Prefs.callName(this))
        nameField = nameEt
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
        if (SafeBrowsing.available()) {
            col.addView(Switch(this).apply {
                text = tr("Semak juga dengan Google Safe Browsing", "Also check with Google Safe Browsing", "同时用 Google 安全浏览检查")
                textSize = 16f
                isChecked = Prefs.safeBrowsing(this@MainActivity)
                setOnCheckedChangeListener { _, on -> Prefs.setSafeBrowsing(this@MainActivity, on) }
            })
            col.addView(
                label(
                    tr(
                        "Bila link ditekan, cap jari ringkas (hash) link dihantar ke Google untuk semak virus & penipuan — bukan link penuh. Perlu internet.",
                        "When a link is tapped, a short fingerprint (hash) of it is sent to Google to check for malware & scams — never the full link. Needs internet.",
                        "点击链接时，只会把链接的简短指纹（哈希）发送给 Google 检查病毒和诈骗——不会发送完整链接。需要网络。",
                    ),
                    13f, color = grey
                )
            )
        }
        col.addView(button(tr("Simpan", "Save", "保存")) {
            Prefs.save(this, nameEt.text.toString(), phoneEt.text.toString(), voiceSw.isChecked)
            toast(tr("Disimpan 👍", "Saved 👍", "已保存 👍"))
            refreshStatus()
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
            showBot(LinkAnalyzer.analyzeMessage(sample, Prefs.lang(this)).first(), tr("Contoh", "Example", "示例"))
            if (!BuildConfig.PLAY && !Settings.canDrawOverlays(this)) {
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

        // Penafian — JagaPMA alat bantuan pencegahan, bukan jaminan
        col.addView(header(tr("Penafian", "Disclaimer", "免责声明")).apply { textSize = 16f })
        col.addView(
            label(
                tr(
                    "JagaPMA ialah alat bantuan untuk MENCEGAH dan mengurangkan risiko penipuan dalam talian. " +
                        "Ia tidak dapat mengesan semua link, laman atau mesej scam, dan amaran yang diberi mungkin tidak tepat sepenuhnya.\n\n" +
                        "JagaPMA dan pembangunnya tidak bertanggungjawab atas sebarang kerugian wang, kerosakan atau penipuan yang berlaku " +
                        "semasa atau selepas menggunakan app ini. Keputusan untuk membuka link, memasang app atau berkongsi maklumat " +
                        "adalah tanggungjawab pengguna sendiri.\n\n" +
                        "Jangan sekali-kali kongsi OTP/TAC, kata laluan atau maklumat bank dengan sesiapa. " +
                        "Jika terkena scam, segera hubungi NSRC 997 dan bank anda.\n\n" +
                        "JagaPMA tidak berkaitan dengan mana-mana bank, agensi kerajaan atau syarikat yang disebut dalam app ini.",
                    "JagaPMA is a helper tool to PREVENT and reduce the risk of online scams. " +
                        "It cannot detect every scam link, site or message, and its warnings may not always be accurate.\n\n" +
                        "JagaPMA and its developer are not responsible for any financial loss, damage or fraud that happens " +
                        "while or after using this app. The decision to open a link, install an app or share information " +
                        "is the user's own responsibility.\n\n" +
                        "Never share OTP/TAC codes, passwords or bank details with anyone. " +
                        "If you are scammed, contact NSRC 997 and your bank immediately.\n\n" +
                        "JagaPMA is not affiliated with any bank, government agency or company mentioned in this app.",
                    "JagaPMA 是一个用于预防和降低网络诈骗风险的辅助工具。" +
                        "它无法检测所有诈骗链接、网站或信息，提醒也不一定完全准确。\n\n" +
                        "JagaPMA 及其开发者不对使用本应用期间或之后发生的任何金钱损失、损害或诈骗负责。" +
                        "是否打开链接、安装应用或分享资料，由用户自行负责。\n\n" +
                        "千万不要把 OTP/TAC 验证码、密码或银行资料告诉任何人。" +
                        "如果被骗，请立即联系 NSRC 997 和您的银行。\n\n" +
                        "JagaPMA 与应用中提到的任何银行、政府机构或公司均无关联。",
                ),
                13f, color = grey
            )
        )

        col.addView(
            label(
                "JagaPMA v${BuildConfig.VERSION_NAME} · " +
                    tr("Dibangunkan oleh Muzamil Mohd", "Developed by Muzamil Mohd", "开发者：Muzamil Mohd"),
                12f, color = Color.rgb(158, 158, 158), center = true
            ).apply { setPadding(0, dp(32), 0, 0) }
        )

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
            .setMessage(Updater.shortNotes(rel.notes).ifBlank { tr("Ada kemas kini untuk JagaPMA.", "An update for JagaPMA is available.", "JagaPMA 有可用更新。") })
            .setPositiveButton(tr("Kemas kini", "Update", "更新")) { _, _ -> startInstall(rel) }
            .setNegativeButton(tr("Nanti", "Later", "稍后"), null)
            .show()
    }

    private fun startInstall(rel: Updater.Release) {
        if (!Updater.canInstall(this)) {
            toast(
                tr(
                    "Benarkan JagaPMA pasang app, kemudian tekan Kemas kini sekali lagi",
                    "Allow JagaPMA to install apps, then tap Update again",
                    "请允许 JagaPMA 安装应用，然后再点一次更新",
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
        Health.schedule(this)
        Health.check(this) // padam amaran "tak aktif" bila dah dibetulkan
    }

    // ------------------------------------------------------------ status

    /** Satu langkah setup wajib — dipakai oleh panduan langkah demi langkah dan senarai semak. */
    private class Step(val row: String, val title: String, val why: String, val done: Boolean, val action: () -> Unit)

    private fun steps(): List<Step> {
        val list = mutableListOf<Step>()
        if (!BuildConfig.PLAY) list += Step(
            tr("Pop-up bot atas skrin", "Bot pop-up on screen", "屏幕上的机器人弹窗"),
            tr("Benarkan pop-up bot", "Allow the bot pop-up", "允许机器人弹窗"),
            tr(
                "Supaya bot boleh keluar di atas WhatsApp dan app lain bila ada link bahaya.",
                "So the bot can appear over WhatsApp and other apps when there's a dangerous link.",
                "这样当出现危险链接时，机器人可以显示在 WhatsApp 和其他应用上方。",
            ),
            Settings.canDrawOverlays(this),
        ) { startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))) }
        list += Step(
            tr("Pengawal link (jadikan browser utama)", "Link guard (set as default browser)", "链接守卫（设为默认浏览器）"),
            tr("Jadikan JagaPMA browser utama", "Make JagaPMA the default browser", "把 JagaPMA 设为默认浏览器"),
            tr(
                "Ini perlindungan utama — setiap link yang ditekan disemak dulu. Link selamat tetap dibuka dalam Chrome. Pilih JagaPMA bila ditanya.",
                "This is the main protection — every tapped link is checked first. Safe links still open in Chrome. Choose JagaPMA when asked.",
                "这是主要保护——每个被点击的链接都会先检查。安全的链接仍会在 Chrome 中打开。被询问时请选择 JagaPMA。",
            ),
            isDefaultBrowser(),
        ) { requestBrowserRole() }
        list += Step(
            tr("Jangan dimatikan di latar belakang", "Keep running in the background", "保持在后台运行"),
            tr("Jangan biar phone tutup JagaPMA", "Don't let the phone close JagaPMA", "不要让手机关闭 JagaPMA"),
            tr(
                "Supaya JagaPMA terus berjaga walaupun lama tak dibuka. Pilih \"Benarkan\" atau \"Tidak dioptimumkan\".",
                "So JagaPMA keeps guarding even if it isn't opened for a long time. Choose \"Allow\" or \"Not optimised\".",
                "这样即使很久没打开，JagaPMA 也会持续守护。请选择「允许」或「不优化」。",
            ),
            ignoringBattery(),
        ) { openBatterySettings() }
        if (Build.VERSION.SDK_INT >= 33) list += Step(
            tr("Notifikasi amaran", "Warning notifications", "警告通知"),
            tr("Benarkan notifikasi", "Allow notifications", "允许通知"),
            tr(
                "Untuk amaran penting, contohnya bila perlindungan terputus.",
                "For important alerts, for example when protection gets switched off.",
                "用于重要提醒，例如保护被关闭时。",
            ),
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED,
        ) { requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1) }
        list += Step(
            "",
            tr("Isi nama & nombor anak", "Enter the name & your number", "填写称呼和孩子的号码"),
            tr(
                "Supaya bot panggil nama yang betul, dan boleh hantar link mencurigakan kepada anak untuk disemak. Isi di bahagian 2, kemudian tekan Simpan.",
                "So the bot uses the right name and can send suspicious links to you to check. Fill in section 2, then tap Save.",
                "这样机器人会用正确的称呼，并能把可疑链接发给孩子检查。请填写第 2 部分，然后点击保存。",
            ),
            Prefs.isSetUp(this) && Prefs.phone(this).isNotBlank(),
        ) { nameField?.let { f -> scrollView?.post { scrollView?.smoothScrollTo(0, f.top - dp(80)); f.requestFocus() } } }
        return list
    }

    private fun openBatterySettings() {
        // Versi Play tiada kebenaran REQUEST_IGNORE_BATTERY_OPTIMIZATIONS — buka senarai tetapan sahaja
        try {
            if (BuildConfig.PLAY) {
                toast(tr("Cari JagaPMA → pilih \"Tidak dioptimumkan\"", "Find JagaPMA → choose \"Not optimised\"", "找到 JagaPMA → 选择「不优化」"))
                startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
            } else {
                startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:$packageName")))
            }
        } catch (_: Exception) {
            startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
        }
    }

    /** Kad besar di atas: satu langkah pada satu masa, atau "semua siap". */
    private fun guideCard(steps: List<Step>): View {
        val next = steps.indexOfFirst { !it.done }
        val doneAll = next < 0
        val accent = if (doneAll) Color.rgb(46, 125, 50) else Color.rgb(25, 118, 210)
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(16), dp(18), dp(16))
            background = GradientDrawable().apply {
                setColor(if (doneAll) Color.rgb(232, 245, 233) else Color.rgb(227, 242, 253))
                cornerRadius = dp(18).toFloat()
                setStroke(dp(2), accent)
            }
        }
        if (doneAll) {
            box.addView(label(tr("✅ Semua siap!", "✅ All set!", "✅ 全部完成！"), 22f, bold = true, color = accent))
            box.addView(
                label(
                    tr(
                        "JagaPMA sedang menjaga ${Prefs.callName(this)}. Link yang ditekan akan disemak dulu.",
                        "JagaPMA is guarding ${Prefs.callName(this)}. Tapped links are checked first.",
                        "JagaPMA 正在守护${Prefs.callName(this)}。点击的链接会先被检查。",
                    ),
                    16f
                ).apply { setPadding(0, dp(6), 0, 0) }
            )
        } else {
            val s = steps[next]
            box.addView(
                label(tr("Langkah ${next + 1} daripada ${steps.size}", "Step ${next + 1} of ${steps.size}", "第 ${next + 1} 步，共 ${steps.size} 步"), 14f, color = grey)
            )
            box.addView(label(s.title, 22f, bold = true, color = accent).apply { setPadding(0, dp(4), 0, 0) })
            box.addView(label(s.why, 16f).apply { setPadding(0, dp(6), 0, dp(10)) })
            box.addView(BotUi.bigButton(this, tr("Teruskan  ➜", "Continue  ➜", "继续  ➜"), accent) { s.action() })
        }
        return box
    }

    private fun refreshStatus() {
        statusBox.removeAllViews()
        val steps = steps()
        statusBox.addView(guideCard(steps), LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply { bottomMargin = dp(12) })
        for (s in steps) if (s.row.isNotEmpty()) row(s.row, s.done) { s.action() }

        // Pilihan — tanpa ni, bot masih semak setiap link yang ditekan (pengawal link).
        // Versi Play tiada Baca skrin langsung.
        if (BuildConfig.PLAY) return
        statusBox.addView(label(tr("Pilihan", "Optional", "可选"), 15f, bold = true, color = grey).apply {
            setPadding(0, dp(14), 0, 0)
        })
        row(
            tr("Baca skrin (Kebolehcapaian → JagaPMA)", "Read screen (Accessibility → JagaPMA)", "读取屏幕（无障碍 → JagaPMA）"),
            a11yEnabled(), optional = true,
            onOff = {
                if (ScreenWatcher.turnOff()) {
                    toast(tr("Baca skrin dimatikan", "Read screen turned off", "已关闭读取屏幕"))
                    statusBox.postDelayed({ refreshStatus() }, 600)
                } else {
                    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                }
            }
        ) { showA11yDisclosure() }
        statusBox.addView(
            label(
                tr(
                    "Bot keluar sebaik link scam atau fail APK nampak di skrin, sebelum ditekan. Tanpa ni, bot tetap semak setiap link yang ditekan.",
                    "The bot warns as soon as a scam link or APK file shows on screen, before it's tapped. Without it, the bot still checks every link that is tapped.",
                    "诈骗链接或 APK 文件一出现在屏幕上，机器人就会提醒，不用等点击。不开启的话，机器人仍会检查每个被点击的链接。",
                ),
                13f, color = grey
            )
        )
    }

    /** Pendedahan jelas (prominent disclosure) sebelum minta Kebolehcapaian — syarat polisi Google. */
    private fun showA11yDisclosure() {
        AlertDialog.Builder(this)
            .setTitle(tr("Benarkan Baca skrin?", "Allow Read screen?", "允许读取屏幕？"))
            .setMessage(
                tr(
                    "JagaPMA guna kebenaran Kebolehcapaian untuk membaca teks yang nampak di skrin dalam WhatsApp, Telegram, SMS, Facebook, Instagram, TikTok dan app emel, supaya bot boleh beri amaran bila ada link scam atau fail APK.\n\n" +
                        "• Semua semakan dibuat dalam phone ini sahaja.\n• Tiada teks, mesej atau link disimpan, dikongsi atau dihantar ke mana-mana.\n• Boleh dimatikan bila-bila masa di skrin ini.",
                    "JagaPMA uses the Accessibility permission to read the text shown on screen in WhatsApp, Telegram, SMS, Facebook, Instagram, TikTok and email apps, so the bot can warn about scam links or APK files.\n\n" +
                        "• All checks happen on this phone only.\n• No text, messages or links are stored, shared or sent anywhere.\n• You can turn it off any time on this screen.",
                    "JagaPMA 使用无障碍权限读取 WhatsApp、Telegram、短信、Facebook、Instagram、TikTok 和电邮应用屏幕上显示的文字，以便在出现诈骗链接或 APK 文件时提醒。\n\n" +
                        "• 所有检查只在这部手机里进行。\n• 不会保存、分享或发送任何文字、信息或链接。\n• 可随时在此页面关闭。",
                )
            )
            .setPositiveButton(tr("Setuju", "Agree", "同意")) { _, _ ->
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
            .setNegativeButton(tr("Tak sekarang", "Not now", "暂不"), null)
            .show()
    }

    private fun a11yEnabled(): Boolean =
        Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
            ?.contains("$packageName/") == true

    private fun ignoringBattery(): Boolean =
        getSystemService(PowerManager::class.java).isIgnoringBatteryOptimizations(packageName)

    private fun isDefaultBrowser(): Boolean = Health.isDefaultBrowser(this)

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
            toast(tr("Pilih JagaPMA sebagai 'Aplikasi pelayar'", "Choose JagaPMA as the 'Browser app'", "请选择 JagaPMA 作为「浏览器应用」"))
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
            showBot(it, tr("Ujian", "Test", "测试"))
        }
    }

    /** Versi Play tiada pop-up atas app lain — tunjuk kad amaran melalui pengawal link. */
    private fun showBot(v: Verdict, app: String) {
        if (!BuildConfig.PLAY) {
            BotOverlay.show(this, v, app)
            return
        }
        // Fail APK (bukan link) — keputusan dah tertulis dalam kotak ujian
        if (!v.url.startsWith("http", ignoreCase = true)) return
        startActivity(Intent(this, LinkGuardActivity::class.java).setData(Uri.parse(v.url)))
    }

    // ------------------------------------------------------------ ui helpers

    private fun row(
        title: String,
        ok: Boolean,
        optional: Boolean = false,
        onOff: (() -> Unit)? = null,
        onFix: () -> Unit,
    ) {
        val r = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(6), 0, dp(6))
        }
        val icon = when {
            ok -> "✅  "
            optional -> "⚪  "
            else -> "❌  "
        }
        r.addView(
            label(icon + title, 16f),
            LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
        )
        if (!ok) r.addView(Button(this).apply { text = tr("Hidupkan", "Turn on", "开启"); setOnClickListener { onFix() } })
        else if (onOff != null) r.addView(Button(this).apply { text = tr("Matikan", "Turn off", "关闭"); setOnClickListener { onOff() } })
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
