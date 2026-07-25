package com.viniciuscoscia.kmpfullstackplayground.broadcastreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

/**
 * Topic #7 — a *custom* broadcast receiver. Unlike [AirPlaneModeReceiver] (which listens to a system
 * action), this one reacts to our own app-private action string. Registering it with
 * `RECEIVER_NOT_EXPORTED` keeps other apps from delivering to it — the safe default on Android 13+.
 */
class CustomReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == ACTION_CUSTOM) {
            val message = intent.getStringExtra(EXTRA_MESSAGE)
            Toast.makeText(context, "Custom broadcast: $message", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val ACTION_CUSTOM = "com.viniciuscoscia.kmpfullstackplayground.ACTION_CUSTOM"
        const val EXTRA_MESSAGE = "message"
    }
}
