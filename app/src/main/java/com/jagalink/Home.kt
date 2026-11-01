package com.jagalink

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.WindowInsets
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.util.Calendar

/**
 * Skrin utama untuk Mak (Konsep B "Kawan Bot"): bot bercakap dalam belon kata,
 * pilihan besar macam butang jawapan WhatsApp, dan jalur status di bawah.
 * Tetapan (untuk anak) di sebalik "Tetapan anak".
 */
object HomeScreen {

    private val INK = Color.rgb(17, 24, 39)
    private val GREEN = Color.rgb(27, 127, 59)
    private val GREEN_DARK = Color.rgb(20, 83, 45)
    private val BLUE = Color.rgb(11, 87, 208)
    private val RED = Color.rgb(179, 38, 30)
    private val RED_DARK = Color.rgb(138, 28, 22)
    private val ORANGE_DARK = Color.rgb(154, 52, 18)

    fun build(
        ctx: Context,
        protected: Boolean,
        onCheck: () -> Unit,
        onCall: () -> Unit,
        onScammed: () -> Unit,
        onSettings: () -> Unit,
    ): View {
        val name = Prefs.callName(ctx)
        val scroll = ScrollView(ctx).apply {
            isFillViewport = true
            setBackgroundColor(Color.rgb(234, 242, 251))
        }
        val col = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(ctx.dp(20), ctx.dp(28), ctx.dp(20), ctx.dp(22))
        }
        scroll.addView(col, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))
        if (Build.VERSION.SDK_INT >= 35) {
            scroll.setOnApplyWindowInsetsListener { _, insets ->
                val bars = insets.getInsets(WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout())
                col.setPadding(ctx.dp(20) + bars.left, ctx.dp(28) + bars.top, ctx.dp(20) + bars.right, ctx.dp(22) + bars.bottom)
                insets
            }
        }

        // Bot + belon kata
        val top = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.BOTTOM
        }
        top.addView(BotView(ctx, if (protected) Level.SAFE else Level.WARN), LinearLayout.LayoutParams(ctx.dp(104), ctx.dp(104)))
        val bubble = TextView(ctx).apply {
            textSize = 21f
            setTextColor(INK)
            setLineSpacing(0f, 1.15f)
            setPadding(ctx.dp(16), ctx.dp(14), ctx.dp(16), ctx.dp(14))
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                val r = ctx.dp(22).toFloat()
                val s = ctx.dp(6).toFloat()
                cornerRadii = floatArrayOf(r, r, r, r, r, r, s, s) // ekor belon di kiri bawah
            }
            text = if (protected) {
                val hello = ctx.tr("${greeting(ctx)}, $name! Saya sedang jaga phone $name. ", "${greeting(ctx)}, $name! I'm guarding your phone. ", "${greeting(ctx)}，${name}！我正在守护您的手机。")
                emphasize(hello, ctx.tr("Semua selamat.", "All is safe.", "一切安全。"), GREEN_DARK)
            } else {
                emphasize(
                    ctx.tr("$name, perlindungan belum siap. ", "$name, protection isn't set up yet. ", "${name}，保护尚未设置好。"),
                    ctx.tr("Minta anak tekan \"Tetapan anak\" di bawah.", "Ask your child to tap \"Child's settings\" below.", "请孩子点击下面的「孩子设置」。"),
                    ORANGE_DARK,
                )
            }
        }
        top.addView(bubble, LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f).apply { leftMargin = ctx.dp(10) })
        col.addView(top)

        col.addView(TextView(ctx).apply {
            text = ctx.tr("$name nak buat apa?", "What would you like to do?", "${name}想做什么？")
            textSize = 20f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(Color.rgb(31, 41, 55))
            setPadding(ctx.dp(4), ctx.dp(22), 0, ctx.dp(10))
        })

        col.addView(reply(ctx, "🔍", ctx.tr("Semak link ni selamat ke?", "Is this link safe?", "这个链接安全吗？"), BLUE, BLUE, Color.rgb(227, 236, 251), onCheck))
        col.addView(reply(ctx, "📞", ctx.tr("Telefon anak", "Call my child", "打电话给孩子"), GREEN, GREEN_DARK, Color.rgb(227, 244, 232), onCall), gap(ctx))
        col.addView(reply(ctx, "⚠️", ctx.tr("Saya rasa dah kena tipu", "I think I've been scammed", "我觉得我被骗了"), RED, RED_DARK, Color.rgb(252, 232, 230), onScammed), gap(ctx))

        // Ruang kosong — jalur status sentiasa di bawah
        col.addView(View(ctx), LinearLayout.LayoutParams(MATCH_PARENT, 0, 1f).apply { topMargin = ctx.dp(20) })

        val status = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(ctx.dp(18), ctx.dp(8), ctx.dp(8), ctx.dp(8))
            background = GradientDrawable().apply { setColor(Color.WHITE); cornerRadius = ctx.dp(18).toFloat() }
        }
        status.addView(View(ctx).apply {
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(if (protected) GREEN else Color.rgb(234, 88, 12))
            }
        }, LinearLayout.LayoutParams(ctx.dp(12), ctx.dp(12)))
        status.addView(TextView(ctx).apply {
            text = if (protected) ctx.tr("Perlindungan aktif", "Protection on", "保护已开启")
            else ctx.tr("Belum siap", "Not set up", "尚未设置")
            textSize = 17f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(if (protected) GREEN_DARK else ORANGE_DARK)
            setPadding(ctx.dp(10), 0, 0, 0)
        }, LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f))
        status.addView(TextView(ctx).apply {
            text = ctx.tr("Tetapan anak", "Child's settings", "孩子设置")
            textSize = 16f
            setTextColor(BLUE)
            paintFlags = paintFlags or android.graphics.Paint.UNDERLINE_TEXT_FLAG
            gravity = Gravity.CENTER
            minHeight = ctx.dp(48)
            setPadding(ctx.dp(12), 0, ctx.dp(12), 0)
            isClickable = true
            setOnClickListener { onSettings() }
        })
        col.addView(status)
        return scroll
    }

    /** Butang jawapan bergaya sembang: bulatan ikon + teks besar, sempadan berwarna. */
    private fun reply(ctx: Context, icon: String, label: String, stroke: Int, textColor: Int, iconBg: Int, onClick: () -> Unit): View =
        LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            minimumHeight = ctx.dp(80)
            setPadding(ctx.dp(16), ctx.dp(12), ctx.dp(18), ctx.dp(12))
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = ctx.dp(40).toFloat()
                setStroke(ctx.dp(3), stroke)
            }
            isClickable = true
            isFocusable = true
            contentDescription = label
            setOnClickListener { onClick() }
            addView(TextView(ctx).apply {
                text = icon
                textSize = 22f
                gravity = Gravity.CENTER
                background = GradientDrawable().apply { shape = GradientDrawable.OVAL; setColor(iconBg) }
            }, LinearLayout.LayoutParams(ctx.dp(50), ctx.dp(50)))
            addView(TextView(ctx).apply {
                text = label
                textSize = 22f
                setTypeface(typeface, Typeface.BOLD)
                setTextColor(textColor)
                setPadding(ctx.dp(14), 0, 0, 0)
            }, LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f))
        }

    private fun gap(ctx: Context) = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply { topMargin = ctx.dp(12) }

    private fun emphasize(plain: String, bold: String, color: Int): CharSequence =
        SpannableStringBuilder(plain).apply {
            val start = length
            append(bold)
            setSpan(StyleSpan(Typeface.BOLD), start, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(ForegroundColorSpan(color), start, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

    // "Petang" dalam BM sampai lewat petang; dalam English & 中文 dah "evening" selepas pukul 5
    private fun greeting(ctx: Context): String = when (val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 5..11 -> ctx.tr("Selamat pagi", "Good morning", "早上好")
        in 12..13 -> ctx.tr("Selamat tengah hari", "Good afternoon", "中午好")
        in 14..18 -> ctx.tr("Selamat petang", if (h >= 17) "Good evening" else "Good afternoon", if (h >= 17) "晚上好" else "下午好")
        else -> ctx.tr("Selamat malam", "Good evening", "晚上好")
    }
}
