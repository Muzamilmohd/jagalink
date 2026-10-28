package com.jagalink

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator

/**
 * Bot kecil JagaPMA — dilukis terus (tiada gambar), melambai & berkelip.
 * Merah = bahaya, oren = mencurigakan, hijau = selamat.
 */
class BotView(context: Context, var mood: Level = Level.DANGER) : View(context) {

    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = RectF()
    private var t = 0f

    private val anim = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 700
        repeatCount = ValueAnimator.INFINITE
        repeatMode = ValueAnimator.REVERSE
        interpolator = AccelerateDecelerateInterpolator()
        addUpdateListener {
            t = it.animatedValue as Float
            invalidate()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        anim.start()
    }

    override fun onDetachedFromWindow() {
        anim.cancel()
        super.onDetachedFromWindow()
    }

    override fun onDraw(c: Canvas) {
        super.onDraw(c)
        val s = minOf(width, height) / 100f
        if (s <= 0f) return
        val bob = (t - 0.5f) * 5f
        c.save()
        c.translate((width - 100 * s) / 2f, (height - 100 * s) / 2f)
        c.scale(s, s)
        c.translate(0f, bob)

        val accent = when (mood) {
            Level.DANGER -> Color.rgb(229, 57, 53)
            Level.WARN -> Color.rgb(255, 160, 0)
            Level.SAFE -> Color.rgb(67, 160, 71)
        }
        val outline = Color.rgb(69, 90, 100)
        val waving = mood != Level.SAFE

        // Tangan melambai
        p.style = Paint.Style.STROKE
        p.strokeWidth = 4f
        p.strokeCap = Paint.Cap.ROUND
        p.color = outline
        val armY = if (waving) 38f - 12f * t else 58f
        c.drawLine(20f, 56f, 7f, armY, p)
        c.drawLine(80f, 56f, 93f, if (waving) 38f - 12f * (1f - t) else 58f, p)
        p.style = Paint.Style.FILL
        p.color = accent
        c.drawCircle(7f, armY, 5f, p)
        c.drawCircle(93f, if (waving) 38f - 12f * (1f - t) else 58f, 5f, p)

        // Badan
        p.color = Color.rgb(207, 216, 220)
        rect.set(33f, 70f, 67f, 96f); c.drawRoundRect(rect, 8f, 8f, p)
        p.style = Paint.Style.STROKE; p.strokeWidth = 3f; p.color = outline
        c.drawRoundRect(rect, 8f, 8f, p)
        p.style = Paint.Style.FILL; p.color = accent
        c.drawCircle(50f, 83f, 8f, p)
        p.color = Color.WHITE
        p.textSize = 13f
        p.textAlign = Paint.Align.CENTER
        p.isFakeBoldText = true
        c.drawText(if (mood == Level.SAFE) "✓" else "!", 50f, 88f, p)

        // Antena + lampu berkelip
        p.style = Paint.Style.STROKE; p.strokeWidth = 3f; p.color = outline
        c.drawLine(50f, 20f, 50f, 9f, p)
        p.style = Paint.Style.FILL; p.color = accent
        p.alpha = if (mood == Level.SAFE) 255 else (90 + 165 * t).toInt()
        c.drawCircle(50f, 7f, 5.5f, p)
        p.alpha = 255

        // Kepala
        p.color = Color.rgb(236, 239, 241)
        rect.set(18f, 20f, 82f, 72f); c.drawRoundRect(rect, 18f, 18f, p)
        p.style = Paint.Style.STROKE; p.strokeWidth = 3f; p.color = outline
        c.drawRoundRect(rect, 18f, 18f, p)

        // Skrin muka
        p.style = Paint.Style.FILL; p.color = Color.rgb(38, 50, 56)
        rect.set(25f, 29f, 75f, 64f); c.drawRoundRect(rect, 11f, 11f, p)

        // Mata (berkelip sekali-sekala)
        val eyeH = if (t > 0.93f) 1f else 6f
        p.color = Color.WHITE
        rect.set(33f, 46f - eyeH, 43f, 46f + eyeH); c.drawOval(rect, p)
        rect.set(57f, 46f - eyeH, 67f, 46f + eyeH); c.drawOval(rect, p)

        // Kening & mulut ikut mood
        p.style = Paint.Style.STROKE; p.strokeWidth = 2.5f; p.color = accent
        if (mood == Level.SAFE) {
            rect.set(42f, 50f, 58f, 60f); c.drawArc(rect, 20f, 140f, false, p)
        } else {
            c.drawLine(32f, 38f, 43f, 34f, p)   // kening risau
            c.drawLine(57f, 34f, 68f, 38f, p)
            c.drawCircle(50f, 57f, 3.5f, p)      // mulut "O"
        }
        c.restore()
    }
}
