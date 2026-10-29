package com.jagalink

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.app.role.RoleManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build

/**
 * Semak sekali sehari: adakah pengawal link (browser utama) masih aktif?
 * Android / Xiaomi kadang-kadang buang tetapan ni selepas update — tanpa amaran, Mak tak dilindungi.
 * Hanya beri amaran kalau pengawal PERNAH aktif (elak ganggu sebelum setup siap).
 */
object Health {
    private const val JOB_ID = 4244
    private const val NOTIF_ID = 4244
    private const val CH = "jagalink_health"

    fun isDefaultBrowser(ctx: Context): Boolean {
        if (Build.VERSION.SDK_INT >= 29) {
            return ctx.getSystemService(RoleManager::class.java).isRoleHeld(RoleManager.ROLE_BROWSER)
        }
        val r = ctx.packageManager.resolveActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse("https://example.com")),
            PackageManager.MATCH_DEFAULT_ONLY
        )
        return r?.activityInfo?.packageName == ctx.packageName
    }

    fun schedule(ctx: Context) {
        if (Prefs.inSecondProfile()) return
        try {
            val js = ctx.getSystemService(JobScheduler::class.java)
            if (js.getPendingJob(JOB_ID) != null) return
            js.schedule(
                JobInfo.Builder(JOB_ID, ComponentName(ctx, HealthJob::class.java))
                    .setPeriodic(24 * 60 * 60_000L)
                    .setPersisted(true)
                    .build()
            )
        } catch (_: Exception) {
        }
    }

    /** Dipanggil oleh job harian dan bila app dibuka. */
    @SuppressLint("MissingPermission")
    fun check(ctx: Context) {
        val nm = ctx.getSystemService(NotificationManager::class.java)
        if (isDefaultBrowser(ctx)) {
            Prefs.setGuardWasOn(ctx)
            nm.cancel(NOTIF_ID)
            return
        }
        if (!Prefs.guardWasOn(ctx)) return
        nm.createNotificationChannel(
            NotificationChannel(CH, ctx.tr("Status perlindungan", "Protection status", "保护状态"), NotificationManager.IMPORTANCE_HIGH)
        )
        val pi = PendingIntent.getActivity(
            ctx, 2,
            Intent(ctx, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val n = Notification.Builder(ctx, CH)
            .setSmallIcon(R.drawable.ic_bot_small)
            .setContentTitle(ctx.tr("⚠️ JagaPMA tak aktif", "⚠️ JagaPMA is not active", "⚠️ JagaPMA 未启用"))
            .setContentText(
                ctx.tr(
                    "Pengawal link dah dimatikan. Tekan untuk hidupkan semula.",
                    "The link guard was turned off. Tap to turn it back on.",
                    "链接守卫已被关闭。点击重新开启。",
                )
            )
            .setStyle(
                Notification.BigTextStyle().bigText(
                    ctx.tr(
                        "Pengawal link dah dimatikan, jadi link yang ditekan tak disemak. Tekan di sini dan hidupkan semula — minta tolong anak kalau perlu.",
                        "The link guard was turned off, so tapped links aren't checked. Tap here to turn it back on — ask your child for help if needed.",
                        "链接守卫已被关闭，点击的链接不会被检查。点击这里重新开启——需要时请孩子帮忙。",
                    )
                )
            )
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()
        try {
            nm.notify(NOTIF_ID, n)
        } catch (_: SecurityException) {
        }
    }
}

class HealthJob : JobService() {
    override fun onStartJob(params: JobParameters): Boolean {
        Health.check(this)
        return false
    }

    override fun onStopJob(params: JobParameters): Boolean = true
}
