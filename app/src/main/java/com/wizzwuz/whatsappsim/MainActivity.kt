package com.wizzwuz.whatsappsim

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.wizzwuz.whatsappsim.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
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

        NotificationHelper.createNotificationChannel(this)
        checkNotificationPermission()
        setupPresets()
        setupDelayChips()
        setupActionButtons()
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

    private fun setupPresets() {
        // Sender Presets
        binding.chipMom.setOnClickListener { binding.etSender.setText(getString(R.string.preset_mom)) }
        binding.chipBoss.setOnClickListener { binding.etSender.setText(getString(R.string.preset_boss)) }
        binding.chipBank.setOnClickListener { binding.etSender.setText(getString(R.string.preset_bank)) }
        binding.chipCrush.setOnClickListener { binding.etSender.setText(getString(R.string.preset_crush)) }

        // Message Presets
        binding.chipMsg1.setOnClickListener {
            binding.etMessage.setText(binding.chipMsg1.text.toString())
        }
        binding.chipMsg2.setOnClickListener {
            binding.etMessage.setText(binding.chipMsg2.text.toString())
        }
        binding.chipMsg3.setOnClickListener {
            binding.etMessage.setText(binding.chipMsg3.text.toString())
        }
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
                ?.ifEmpty { "WhatsApp Contact" } ?: "WhatsApp Contact"
            val message = binding.etMessage.text?.toString()?.trim()
                ?.ifEmpty { "Hello! This is a test WhatsApp message." }
                ?: "Hello! This is a test WhatsApp message."

            if (binding.switchAutoMinimize.isChecked) {
                // Minimize app to home screen so heads-up notification drops from the top
                moveTaskToBack(true)
                Handler(Looper.getMainLooper()).postDelayed({
                    NotificationHelper.showNotification(applicationContext, sender, message)
                }, 400)
            } else {
                NotificationHelper.showNotification(this, sender, message)
                Snackbar.make(binding.root, "WhatsApp notification sent!", Snackbar.LENGTH_SHORT).show()
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
            ?.ifEmpty { "WhatsApp Contact" } ?: "WhatsApp Contact"
        val message = binding.etMessage.text?.toString()?.trim()
            ?.ifEmpty { "Hello! This is a scheduled WhatsApp message." }
            ?: "Hello! This is a scheduled WhatsApp message."

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, NotificationPublisherReceiver::class.java).apply {
            putExtra(NotificationPublisherReceiver.EXTRA_SENDER, sender)
            putExtra(NotificationPublisherReceiver.EXTRA_MESSAGE, message)
        }

        val requestCode = 2001
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
            // Fallback for strict exact alarm restrictions
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
