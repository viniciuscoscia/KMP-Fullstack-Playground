package com.viniciuscoscia.kmpfullstackplayground.foregroundservices

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.viniciuscoscia.kmpfullstackplayground.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Topic #8 — Foreground Services.
 *
 * A foreground service does user-visible work that must keep running even when no screen is open
 * (music, navigation, a workout timer). It is *required* to show an ongoing notification so the user
 * always knows it is alive. This one is a stopwatch: [start] posts the notification via
 * [startForeground], then a coroutine ticks once per second and updates the notification text.
 *
 * Control it with an explicit Intent carrying an [Actions] value (see [ForegroundServiceActivity]).
 */
class RunningService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Actions.START.toString() -> start()
            Actions.STOP.toString() -> stopSelf()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun start() {
        // Must call startForeground quickly after the service starts, or the system kills it.
        startForeground(NOTIFICATION_ID, buildNotification(elapsedSeconds = 0))

        serviceScope.launch {
            var seconds = 0L
            while (isActive) {
                delay(1000)
                seconds++
                notificationManager().notify(NOTIFICATION_ID, buildNotification(seconds))
            }
        }
    }

    private fun buildNotification(elapsedSeconds: Long) =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("Stopwatch running")
            .setContentText("Elapsed: ${formatElapsed(elapsedSeconds)}")
            .setOngoing(true)
            .build()

    private fun notificationManager() =
        getSystemService(NOTIFICATION_SERVICE) as NotificationManager

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    /**
     * Formats an elapsed duration in seconds as `mm:ss` for the notification
     * (e.g. `65` → `"01:05"`, `600` → `"10:00"`).
     */
    private fun formatElapsed(totalSeconds: Long): String {
        // TODO(human): return totalSeconds formatted as mm:ss — minutes and seconds, both
        //  zero-padded to two digits. Hint: minutes = totalSeconds / 60, seconds = totalSeconds % 60,
        //  and String.format("%02d", ...) zero-pads.
        return TODO("Implement mm:ss formatting")
    }

    enum class Actions { START, STOP }

    companion object {
        const val CHANNEL_ID = "running_channel"
        const val NOTIFICATION_ID = 1
    }
}
