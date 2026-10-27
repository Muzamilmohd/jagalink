package com.jagalink

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

/** Tentukan bila bot perlu keluar (elak amaran berulang untuk link yang sama). */
object WarnCenter {
    enum class Source { NOTIFICATION, SCREEN, CLICK }

    private val seen = HashMap<String, Long>()

    fun check(ctx: Context, text: String, app: String?, source: Source) {
        if (text.isBlank()) return
        val worst = LinkAnalyzer.analyzeMessage(text, Prefs.lang(ctx)).maxByOrNull { it.score } ?: return
        if (worst.level == Level.SAFE) return

        val window = when (source) {
            Source.NOTIFICATION -> 30 * 60_000L      // 30 minit
            Source.SCREEN -> 6 * 60 * 60_000L        // 6 jam (chat lama tak ganggu berulang)
            Source.CLICK -> 5_000L                   // tekan link = sentiasa beri amaran
        }
        val now = System.currentTimeMillis()
        synchronized(seen) {
            val last = seen[worst.url]
            if (last != null && now - last < window) return
            if (seen.size > 500) seen.clear()
            seen[worst.url] = now
        }
        BotOverlay.show(ctx.applicationContext, worst, app)
    }
}

/** Pop-up bot di atas app lain. */
object BotOverlay {
    private var current: View? = null
    private val main = Handler(Looper.getMainLooper())

    fun show(ctx: Context, v: Verdict, app: String?) {
        main.post {
            if (current != null) return@post
            if (!Settings.canDrawOverlays(ctx)) {
                Notifier.warn(ctx, v, app)
                return@post
            }
            val wm = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val view = BotUi.build(ctx, v, app, onDismiss = { dismiss(wm) })
            val lp = WindowManager.LayoutParams(
                MATCH_PARENT, MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            )
            try {
                wm.addView(view, lp)
                current = view
            } catch (_: Exception) {
                Notifier.warn(ctx, v, app)
                return@post
            }
            Alerts.buzz(ctx)
            if (Prefs.voice(ctx)) Speaker.say(ctx, BotUi.spoken(ctx, v))
        }
    }

    private fun dismiss(wm: WindowManager) {
        current?.let { try { wm.removeView(it) } catch (_: Exception) { } }
        current = null
    }
}

/** Kad amaran bot — dikongsi oleh pop-up dan skrin pengawal link. */
object BotUi {

    fun spoken(ctx: Context, v: Verdict): String {
        val name = Prefs.callName(ctx)
        return if (v.adult) ctx.tr(
            "$name, laman ni tak elok. Bot dah sekat ya.",
            "$name, this site is not appropriate. The bot has blocked it.",
            "$name，这个网站不好，机器人已经拦截了。",
        ) else if (v.level == Level.DANGER) ctx.tr(
            "$name, hati-hati! Link ni bahaya, mungkin scam. Jangan tekan ya.",
            "$name, be careful! This link is dangerous, maybe a scam. Please don't tap it.",
            "$name，小心！这个链接很危险，可能是诈骗。不要点击。",
        ) else ctx.tr(
            "$name, tunggu dulu. Link ni nampak mencurigakan.",
            "$name, wait a moment. This link looks suspicious.",
            "$name，等一下。这个链接看起来可疑。",
        )
    }

    fun build(
        ctx: Context,
        v: Verdict,
        app: String?,
        onDismiss: () -> Unit,
        onProceed: (() -> Unit)? = null,
        onSetup: (() -> Unit)? = null,
    ): View {
        val name = Prefs.callName(ctx)
        val danger = v.level == Level.DANGER
        val accent = if (danger) Color.rgb(211, 47, 47) else Color.rgb(239, 108, 0)
        val tint = if (danger) Color.rgb(255, 235, 238) else Color.rgb(255, 243, 224)

        val root = FrameLayout(ctx).apply {
            setBackgroundColor(Color.argb(190, 0, 0, 0))
            isClickable = true
        }
        val scroll = ScrollView(ctx).apply { isFillViewport = false }
        root.addView(
            scroll,
            FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT, Gravity.CENTER).apply {
                setMargins(ctx.dp(18), ctx.dp(24), ctx.dp(18), ctx.dp(24))
            }
        )

        val card = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(ctx.dp(20), ctx.dp(14), ctx.dp(20), ctx.dp(20))
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = ctx.dp(26).toFloat()
                setStroke(ctx.dp(4), accent)
            }
        }
        scroll.addView(card, FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))

        card.addView(BotView(ctx, v.level), LinearLayout.LayoutParams(ctx.dp(130), ctx.dp(130)))

        val title = if (v.adult) ctx.tr("🚫 Link disekat", "🚫 Link blocked", "🚫 链接已拦截")
        else ctx.tr("⚠️ $name, hati-hati!", "⚠️ $name, be careful!", "⚠️ $name，小心！")
        card.addView(text(ctx, title, 26f, accent, bold = true))

        val bubbleMsg = if (v.adult) ctx.tr(
            "Link ni ke laman lucah (tak senonoh). Bot dah sekat supaya $name tak terbuka.",
            "This link goes to a pornographic site. The bot has blocked it so $name won't open it by accident.",
            "这个链接是色情网站。机器人已经拦截，以免${name}不小心打开。",
        ) else if (danger) ctx.tr(
            "Link ni BAHAYA. Mungkin scammer nak tipu $name. Jangan tekan, jangan isi apa-apa.",
            "This link is DANGEROUS. A scammer may be trying to trick $name. Don't tap it, don't fill in anything.",
            "这个链接很危险！可能有骗子想骗$name。不要点击，也不要填写任何资料。",
        ) else ctx.tr(
            "Link ni nampak mencurigakan. Jangan tekan dulu ya — tanya anak kalau tak pasti.",
            "This link looks suspicious. Don't tap it yet — ask your child if you're not sure.",
            "这个链接看起来可疑。先不要点击——不确定的话问问孩子。",
        )
        card.addView(text(ctx, bubbleMsg, 18f, Color.rgb(33, 33, 33)).apply {
            setPadding(ctx.dp(14), ctx.dp(12), ctx.dp(14), ctx.dp(12))
            background = GradientDrawable().apply { setColor(tint); cornerRadius = ctx.dp(16).toFloat() }
        }, spaced(ctx, 10))

        if (app != null) {
            val found = ctx.tr("Dijumpai dalam: $app", "Found in: $app", "发现于：$app")
            card.addView(text(ctx, found, 14f, Color.GRAY), spaced(ctx, 8))
        }

        card.addView(text(ctx, v.url, 13f, Color.rgb(97, 97, 97)).apply {
            typeface = Typeface.MONOSPACE
            maxLines = 2
            ellipsize = TextUtils.TruncateAt.END
        }, spaced(ctx, 4))

        if (v.reasons.isNotEmpty()) {
            card.addView(
                text(ctx, v.reasons.take(3).joinToString("\n") { "• $it" }, 15f, Color.rgb(66, 66, 66))
                    .apply { gravity = Gravity.START },
                spaced(ctx, 10)
            )
        }

        val okLabel = ctx.tr("👍  OK, saya tak tekan", "👍  OK, I won't tap it", "👍  好的，我不点击")
        card.addView(bigButton(ctx, okLabel, Color.rgb(46, 125, 50)) { onDismiss() }, spaced(ctx, 16))

        val phone = Prefs.phone(ctx)
        if (phone.isNotBlank()) {
            val askLabel = ctx.tr("📞  Tanya anak dulu", "📞  Ask my child first", "📞  先问孩子")
            card.addView(bigButton(ctx, askLabel, Color.rgb(25, 118, 210)) {
                try {
                    ctx.startActivity(
                        Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                } catch (_: Exception) {
                }
                onDismiss()
            }, spaced(ctx, 10))
        }

        if (onSetup != null) {
            val setupLabel = ctx.tr(
                "⚙️ Isi nama & nombor anak untuk WhatsApp kedua",
                "⚙️ Set name & child's number for the second WhatsApp",
                "⚙️ 为第二个 WhatsApp 设置称呼和孩子的号码",
            )
            card.addView(text(ctx, setupLabel, 15f, Color.rgb(25, 118, 210), bold = true).apply {
                setPadding(0, ctx.dp(10), 0, ctx.dp(4))
                setOnClickListener { onSetup() }
            }, spaced(ctx, 12))
        }

        if (onProceed != null && !v.adult) {
            val openLabel = ctx.tr(
                "Buka juga (tekan lama 2 saat)",
                "Open anyway (press and hold 2 seconds)",
                "仍然打开（长按 2 秒）",
            )
            card.addView(text(ctx, openLabel, 13f, Color.GRAY).apply {
                setPadding(0, ctx.dp(10), 0, ctx.dp(4))
                setOnClickListener {
                    val hint = ctx.tr(
                        "Tekan lama kalau betul-betul yakin link ni selamat",
                        "Press and hold only if you're really sure this link is safe",
                        "只有确定这个链接安全才长按",
                    )
                    Toast.makeText(ctx, hint, Toast.LENGTH_SHORT).show()
                }
                setOnLongClickListener { onProceed(); true }
            }, spaced(ctx, 12))
        }
        return root
    }

    private fun spaced(ctx: Context, topDp: Int) =
        LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply { topMargin = ctx.dp(topDp) }

    fun text(ctx: Context, s: String, sp: Float, color: Int, bold: Boolean = false) =
        TextView(ctx).apply {
            text = s
            textSize = sp
            setTextColor(color)
            gravity = Gravity.CENTER
            if (bold) setTypeface(typeface, Typeface.BOLD)
        }

    fun bigButton(ctx: Context, label: String, color: Int, onClick: () -> Unit) =
        TextView(ctx).apply {
            text = label
            textSize = 20f
            setTextColor(Color.WHITE)
            setTypeface(typeface, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(ctx.dp(16), ctx.dp(14), ctx.dp(16), ctx.dp(14))
            background = GradientDrawable().apply { setColor(color); cornerRadius = ctx.dp(18).toFloat() }
            setOnClickListener { onClick() }
        }
}
