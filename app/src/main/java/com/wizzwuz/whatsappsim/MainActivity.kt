package com.wizzwuz.whatsappsim

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.wizzwuz.whatsappsim.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var selectedAppType: AppType = AppType.WHATSAPP
    private var selectedDelaySeconds: Int = 5
    private var scheduledPendingIntent: PendingIntent? = null
    private var countDownTimer: CountDownTimer? = null

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Toast.makeText(this, "Notification permission granted!", Toast.LENGTH_SHORT).show()
            } else {
                Snackbar.make(
                    binding.root,
                    R.string.msg_permission_required,
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        NotificationHelper.createChannels(this)
        checkNotificationPermission()
        setupThemeToggle()
        setupAppTypeSelector()
        setupLivePreviewListeners()
        setupPresets()
        setupDelayChips()
        setupActionButtons()
        updateLivePreview()
    }

    private fun setupThemeToggle() {
        val prefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val isNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

        binding.btnThemeToggle.setImageResource(if (isNightMode) R.drawable.ic_light_mode else R.drawable.ic_dark_mode)

        binding.btnThemeToggle.setOnClickListener {
            val currentNight = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
            val newNight = !currentNight
            prefs.edit().putBoolean("dark_mode", newNight).apply()

            AppCompatDelegate.setDefaultNightMode(
                if (newNight) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun setupAppTypeSelector() {
        binding.chipGroupAppType.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.contains(R.id.chipAppGoogleMessages)) {
                switchToGoogleMessagesMode()
            } else {
                switchToWhatsAppMode()
            }
        }
    }

    private fun switchToWhatsAppMode() {
        selectedAppType = AppType.WHATSAPP

        binding.btnTestNow.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(this, R.color.wa_green))

        binding.chipSender1.text = "Mom ❤️"
        binding.chipSender2.text = "Boss 💼"
        binding.chipSender3.text = "Best Friend 🤝"
        binding.chipSender4.text = "Crush ✨"

        binding.chipMsgTemplate1.text = "Hey, are you free now?"
        binding.chipMsgTemplate2.text = "Where are you? We are waiting."
        binding.chipMsgTemplate3.text = "Important: Call me ASAP!"

        binding.etSender.setText("Mom ❤️")
        binding.etMessage.setText("Are you coming home for dinner tonight? Don't forget to call me!")

        updateLivePreview()
    }

    private fun switchToGoogleMessagesMode() {
        selectedAppType = AppType.GOOGLE_MESSAGES

        binding.btnTestNow.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(this, R.color.google_blue))

        binding.chipSender1.text = "Google"
        binding.chipSender2.text = "Uber"
        binding.chipSender3.text = "+1 (555) 019-2834"
        binding.chipSender4.text = "Bank Alert"

        binding.chipMsgTemplate1.text = "G-749201 is your Google verification code."
        binding.chipMsgTemplate2.text = "Your driver is arriving in 2 mins."
        binding.chipMsgTemplate3.text = "Security alert: New sign-in to your account."

        binding.etSender.setText("Google")
        binding.etMessage.setText("G-749201 is your Google verification code. Do not share this code with anyone.")

        updateLivePreview()
    }

    private fun setupLivePreviewListeners() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateLivePreview()
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        binding.etSender.addTextChangedListener(textWatcher)
        binding.etMessage.addTextChangedListener(textWatcher)
    }

    private fun updateLivePreview() {
        val sender = binding.etSender.text?.toString()?.trim()
            ?.ifEmpty { if (selectedAppType == AppType.WHATSAPP) "WhatsApp Contact" else "Messages" }
            ?: "Messages"
        val message = binding.etMessage.text?.toString()?.trim()
            ?.ifEmpty { "Notification message content..." }
            ?: "Notification message content..."

        binding.tvPreviewSender.text = sender
        binding.tvPreviewMessage.text = message

        when (selectedAppType) {
            AppType.WHATSAPP -> {
                binding.tvPreviewAppName.text = "WhatsApp"
                binding.ivPreviewSmallIcon.setImageResource(R.drawable.ic_whatsapp)
                binding.ivPreviewSmallIcon.imageTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.wa_green))
                binding.ivPreviewAvatar.setImageResource(R.drawable.ic_whatsapp_large)

                binding.tvPreviewAction1.text = "Reply"
                binding.tvPreviewAction1.setTextColor(ContextCompat.getColor(this, R.color.wa_teal))
                binding.tvPreviewAction2.visibility = View.VISIBLE
            }
            AppType.GOOGLE_MESSAGES -> {
                binding.tvPreviewAppName.text = "Messages"
                binding.ivPreviewSmallIcon.setImageResource(R.drawable.ic_google_messages)
                binding.ivPreviewSmallIcon.imageTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.google_blue))
                binding.ivPreviewAvatar.setImageResource(R.drawable.ic_google_messages_large)

                val hasCode = message.lowercase().let {
                    it.contains("code") || it.contains("otp") || it.contains("verification")
                }
                binding.tvPreviewAction1.text = if (hasCode) "Copy code" else "Reply"
                binding.tvPreviewAction1.setTextColor(ContextCompat.getColor(this, R.color.google_blue))
                binding.tvPreviewAction2.visibility = View.VISIBLE
            }
        }
    }

    private fun setupPresets() {
        val onSenderClicked = View.OnClickListener { v ->
            val chip = v as? com.google.android.material.chip.Chip
            chip?.text?.toString()?.let {
                binding.etSender.setText(it)
                updateLivePreview()
            }
        }
        binding.chipSender1.setOnClickListener(onSenderClicked)
        binding.chipSender2.setOnClickListener(onSenderClicked)
        binding.chipSender3.setOnClickListener(onSenderClicked)
        binding.chipSender4.setOnClickListener(onSenderClicked)

        val onMsgClicked = View.OnClickListener { v ->
            val chip = v as? com.google.android.material.chip.Chip
            chip?.text?.toString()?.let {
                binding.etMessage.setText(it)
                updateLivePreview()
            }
        }
        binding.chipMsgTemplate1.setOnClickListener(onMsgClicked)
        binding.chipMsgTemplate2.setOnClickListener(onMsgClicked)
        binding.chipMsgTemplate3.setOnClickListener(onMsgClicked)
    }

    private fun setupDelayChips() {
        binding.chipGroupDelay.setOnCheckedStateChangeListener { _, checkedIds ->
            when {
                checkedIds.contains(R.id.chipDelay5s) -> selectedDelaySeconds = 5
                checkedIds.contains(R.id.chipDelay10s) -> selectedDelaySeconds = 10
                checkedIds.contains(R.id.chipDelay30s) -> selectedDelaySeconds = 30
                checkedIds.contains(R.id.chipDelay1m) -> selectedDelaySeconds = 60
                checkedIds.contains(R.id.chipDelay5m) -> selectedDelaySeconds = 300
                else -> selectedDelaySeconds = 5
            }
        }
    }

    private fun setupActionButtons() {
        // Test Now (Immediate)
        binding.btnTestNow.setOnClickListener {
            val sender = binding.etSender.text?.toString()?.trim()
                ?.ifEmpty { if (selectedAppType == AppType.WHATSAPP) "WhatsApp Contact" else "Messages" }
                ?: "Messages"
            val message = binding.etMessage.text?.toString()?.trim()
                ?.ifEmpty { "Hello! This is a simulated notification." }
                ?: "Hello! This is a simulated notification."

            if (binding.switchAutoMinimize.isChecked) {
                // Minimize app to home screen so heads-up notification drops from the top
                moveTaskToBack(true)
                Handler(Looper.getMainLooper()).postDelayed({
                    NotificationHelper.showNotification(applicationContext, selectedAppType, sender, message)
                }, 400)
            } else {
                NotificationHelper.showNotification(this, selectedAppType, sender, message)
                Snackbar.make(binding.root, "Notification sent!", Snackbar.LENGTH_SHORT).show()
            }
        }

        // Schedule Notification
        binding.btnSchedule.setOnClickListener {
            scheduleNotification()
        }

        // Cancel Scheduled Notification
        binding.btnCancelSchedule.setOnClickListener {
            cancelScheduledNotification()
        }
    }

    private fun scheduleNotification() {
        val sender = binding.etSender.text?.toString()?.trim()
            ?.ifEmpty { if (selectedAppType == AppType.WHATSAPP) "WhatsApp Contact" else "Messages" }
            ?: "Messages"
        val message = binding.etMessage.text?.toString()?.trim()
            ?.ifEmpty { "Hello! This is a scheduled notification." }
            ?: "Hello! This is a scheduled notification."

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, NotificationPublisherReceiver::class.java).apply {
            putExtra(NotificationPublisherReceiver.EXTRA_APP_TYPE, selectedAppType.idName)
            putExtra(NotificationPublisherReceiver.EXTRA_SENDER, sender)
            putExtra(NotificationPublisherReceiver.EXTRA_MESSAGE, message)
        }

        val requestCode = 2002
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        scheduledPendingIntent = pendingIntent

        val triggerAtMillis = System.currentTimeMillis() + (selectedDelaySeconds * 1000L)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }

        val delayText = getString(R.string.msg_scheduled, selectedDelaySeconds)
        Toast.makeText(this, delayText, Toast.LENGTH_LONG).show()

        startCountdown(selectedDelaySeconds)

        if (binding.switchAutoMinimize.isChecked) {
            moveTaskToBack(true)
        }
    }

    private fun startCountdown(seconds: Int) {
        countDownTimer?.cancel()
        binding.tvScheduleStatus.visibility = View.VISIBLE
        binding.btnCancelSchedule.visibility = View.VISIBLE

        countDownTimer = object : CountDownTimer(seconds * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val secLeft = (millisUntilFinished / 1000L) + 1
                binding.tvScheduleStatus.text =
                    "⏳ Notification arriving in $secLeft seconds... (App can be closed/locked)"
            }

            override fun onFinish() {
                binding.tvScheduleStatus.text = "✅ Notification delivered!"
                binding.btnCancelSchedule.visibility = View.GONE
                Handler(Looper.getMainLooper()).postDelayed({
                    binding.tvScheduleStatus.visibility = View.GONE
                }, 3000)
            }
        }.start()
    }

    private fun cancelScheduledNotification() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        scheduledPendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
        scheduledPendingIntent = null
        countDownTimer?.cancel()

        binding.tvScheduleStatus.visibility = View.GONE
        binding.btnCancelSchedule.visibility = View.GONE
        Toast.makeText(this, R.string.msg_cancelled, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}
