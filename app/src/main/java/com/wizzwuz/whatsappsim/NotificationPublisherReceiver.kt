package com.wizzwuz.whatsappsim

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationPublisherReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_SENDER = "extra_sender"
        const val EXTRA_MESSAGE = "extra_message"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val sender = intent.getStringExtra(EXTRA_SENDER) ?: "WhatsApp"
        val message = intent.getStringExtra(EXTRA_MESSAGE) ?: "You have a new message"

        NotificationHelper.showNotification(context, sender, message)
    }
}
