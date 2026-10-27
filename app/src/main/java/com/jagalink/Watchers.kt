package com.jagalink

import android.accessibilityservice.AccessibilityService
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/** Baca teks atas skrin bila mak buka app sembang / emel, dan bila dia TEKAN sesuatu. */
class ScreenWatcher : AccessibilityService() {

    private var lastScan = 0L

    override fun onAccessibilityEvent(e: AccessibilityEvent) {
        val pkg = e.packageName?.toString() ?: return
        val app = Watched.APPS[pkg] ?: return

        // Mak tekan mesej/link → semak serta-merta
        if (e.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            val sb = StringBuilder()
            e.text.forEach { sb.append(it).append('\n') }
            e.source?.let { n ->
                n.text?.let { sb.append(it).append('\n') }
                n.contentDescription?.let { sb.append(it).append('\n') }
            }
            WarnCenter.check(this, sb.toString(), app, WarnCenter.Source.CLICK)
            return
        }

        // Imbas skrin, paling kerap sekali setiap 1.5 saat (jimat bateri)
        val now = SystemClock.uptimeMillis()
        if (now - lastScan < 1500) return
        lastScan = now
        val root = rootInActiveWindow ?: return
        val sb = StringBuilder()
        collect(root, sb, 0)
        WarnCenter.check(this, sb.toString(), app, WarnCenter.Source.SCREEN)
    }

    private fun collect(n: AccessibilityNodeInfo?, sb: StringBuilder, depth: Int) {
        if (n == null || depth > 40 || sb.length > 20_000) return
        n.text?.let { sb.append(it).append('\n') }
        n.contentDescription?.let { if (it.length < 400) sb.append(it).append('\n') }
        for (i in 0 until n.childCount) collect(n.getChild(i), sb, depth + 1)
    }

    override fun onInterrupt() {}
}
