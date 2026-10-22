package com.wizzwuz.whatsappsim

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationPublisherReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_APP_TYPE = "extra_app_type"
        const val EXTRA_SENDER = "extra_sender"
        const val EXTRA_MESSAGE = "extra_message"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val appTypeId = intent.getStringExtra(EXTRA_APP_TYPE)
        val appType = AppType.fromId(appTypeId)
        val sender = intent.getStringExtra(EXTRA_SENDER) ?: "Messages"
        val message = intent.getStringExtra(EXTRA_MESSAGE) ?: "You have a new message"

        NotificationHelper.showNotification(context, appType, sender, message)
    }
}
