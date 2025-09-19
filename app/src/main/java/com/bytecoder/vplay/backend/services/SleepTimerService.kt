package com.bytecoder.vplay.backend.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.bytecoder.vplay.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SleepTimerService : Service() {
    companion object {
        const val NOTIFICATION_ID = 2001
        const val CHANNEL_ID = "sleep_timer_channel"
        const val ACTION_START_TIMER = "START_TIMER"
        const val ACTION_STOP_TIMER = "STOP_TIMER"
        const val ACTION_EXTEND_TIMER = "EXTEND_TIMER"
        const val EXTRA_DURATION_MINUTES = "duration_minutes"
        
        private val _isRunning = MutableStateFlow(false)
        val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()
        
        private val _remainingTime = MutableStateFlow(0L)
        val remainingTime: StateFlow<Long> = _remainingTime.asStateFlow()
        
        fun startSleepTimer(context: Context, durationMinutes: Int) {
            val intent = Intent(context, SleepTimerService::class.java).apply {
                action = ACTION_START_TIMER
                putExtra(EXTRA_DURATION_MINUTES, durationMinutes)
            }
            context.startForegroundService(intent)
        }
        
        fun stopSleepTimer(context: Context) {
            val intent = Intent(context, SleepTimerService::class.java).apply {
                action = ACTION_STOP_TIMER
            }
            context.startService(intent)
        }
        
        fun extendSleepTimer(context: Context, additionalMinutes: Int) {
            val intent = Intent(context, SleepTimerService::class.java).apply {
                action = ACTION_EXTEND_TIMER
                putExtra(EXTRA_DURATION_MINUTES, additionalMinutes)
            }
            context.startService(intent)
        }
    }
    
    private var countDownTimer: CountDownTimer? = null
    private var totalDurationMs: Long = 0
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_TIMER -> {
                val durationMinutes = intent.getIntExtra(EXTRA_DURATION_MINUTES, 30)
                startTimer(durationMinutes)
            }
            ACTION_STOP_TIMER -> {
                stopTimer()
            }
            ACTION_EXTEND_TIMER -> {
                val additionalMinutes = intent.getIntExtra(EXTRA_DURATION_MINUTES, 15)
                extendTimer(additionalMinutes)
            }
        }
        return START_NOT_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    private fun startTimer(durationMinutes: Int) {
        totalDurationMs = durationMinutes * 60 * 1000L
        _remainingTime.value = totalDurationMs
        _isRunning.value = true
        
        countDownTimer = object : CountDownTimer(totalDurationMs, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _remainingTime.value = millisUntilFinished
                updateNotification(millisUntilFinished)
            }
            
            override fun onFinish() {
                onTimerFinished()
            }
        }.start()
        
        startForeground(NOTIFICATION_ID, createNotification(totalDurationMs))
    }
    
    private fun extendTimer(additionalMinutes: Int) {
        if (_isRunning.value) {
            countDownTimer?.cancel()
            val currentRemaining = _remainingTime.value
            val additionalMs = additionalMinutes * 60 * 1000L
            val newDuration = currentRemaining + additionalMs
            
            countDownTimer = object : CountDownTimer(newDuration, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    _remainingTime.value = millisUntilFinished
                    updateNotification(millisUntilFinished)
                }
                
                override fun onFinish() {
                    onTimerFinished()
                }
            }.start()
            
            updateNotification(newDuration)
        }
    }
    
    private fun stopTimer() {
        countDownTimer?.cancel()
        _isRunning.value = false
        _remainingTime.value = 0L
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
    
    private fun onTimerFinished() {
        _isRunning.value = false
        _remainingTime.value = 0L
        
        // Send broadcast to pause/stop media playback
        sendBroadcast(Intent("com.bytecoder.vplay.SLEEP_TIMER_FINISHED"))
        
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
    
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Sleep Timer",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows sleep timer countdown"
            setShowBadge(false)
            setSound(null, null)
        }
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
    
    private fun createNotification(remainingMs: Long): Notification {
        val remainingMinutes = (remainingMs / 1000 / 60).toInt()
        val remainingSeconds = ((remainingMs / 1000) % 60).toInt()
        val timeText = String.format("%d:%02d", remainingMinutes, remainingSeconds)
        
        val stopIntent = Intent(this, SleepTimerService::class.java).apply {
            action = ACTION_STOP_TIMER
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE
        )
        
        val extendIntent = Intent(this, SleepTimerService::class.java).apply {
            action = ACTION_EXTEND_TIMER
            putExtra(EXTRA_DURATION_MINUTES, 15)
        }
        val extendPendingIntent = PendingIntent.getService(
            this, 1, extendIntent, PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Sleep Timer")
            .setContentText("Music will stop in $timeText")
            .setSmallIcon(R.drawable.ic_sleep_timer)
            .setOngoing(true)
            .setAutoCancel(false)
            .addAction(R.drawable.ic_add, "+15 min", extendPendingIntent)
            .addAction(R.drawable.ic_stop, "Stop", stopPendingIntent)
            .build()
    }
    
    private fun updateNotification(remainingMs: Long) {
        val notification = createNotification(remainingMs)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}


