package com.viniciuscoscia.kmpfullstackplayground.foregroundservices

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.SystemClock
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.viniciuscoscia.kmpfullstackplayground.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.Locale

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
    private var tickerJob: Job? = null
    private var startedAtElapsedRealtimeMillis = 0L

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Actions.START.toString() -> start()
            Actions.STOP.toString() -> stop()
        }
        return START_NOT_STICKY
    }

    private fun start() {
        if (tickerJob?.isActive == true) return

        startedAtElapsedRealtimeMillis = SystemClock.elapsedRealtime()
        // Must call startForeground quickly after the service starts, or the system kills it.
        startForeground(NOTIFICATION_ID, buildNotification(elapsedSeconds = 0))

        tickerJob = serviceScope.launch {
            while (isActive) {
                delay(millisUntilNextSecond())
                notificationManager().notify(NOTIFICATION_ID, buildNotification(elapsedSeconds()))
            }
        }
    }

    private fun stop() {
        tickerJob?.cancel()
        tickerJob = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun elapsedSeconds(): Long =
        ((SystemClock.elapsedRealtime() - startedAtElapsedRealtimeMillis) / ONE_SECOND_MILLIS)
            .coerceAtLeast(0L)

    private fun millisUntilNextSecond(): Long {
        val elapsedMillis = (SystemClock.elapsedRealtime() - startedAtElapsedRealtimeMillis)
            .coerceAtLeast(0L)
        return ONE_SECOND_MILLIS - (elapsedMillis % ONE_SECOND_MILLIS)
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
        tickerJob?.cancel()
        tickerJob = null
        serviceScope.cancel()
    }

    enum class Actions { START, STOP }

    companion object {
        private const val ONE_SECOND_MILLIS = 1_000L
        const val CHANNEL_ID = "running_channel"
        const val NOTIFICATION_ID = 1
    }
}

/**
 * Formats elapsed stopwatch time as `mm:ss` for the foreground-service notification.
 */
internal fun formatElapsed(totalSeconds: Long): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}
