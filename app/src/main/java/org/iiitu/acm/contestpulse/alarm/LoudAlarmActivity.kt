package org.iiitu.acm.contestpulse.alarm

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import org.iiitu.acm.contestpulse.databinding.ActivityLoudAlarmBinding

class LoudAlarmActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoudAlarmBinding
    private var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Turn on screen & show over lock screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        )

        binding = ActivityLoudAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val title = intent.getStringExtra(AlarmScheduler.EXTRA_CONTEST_TITLE) ?: "Coding Contest"
        val platform = intent.getStringExtra(AlarmScheduler.EXTRA_CONTEST_PLATFORM) ?: "CODEFORCES"
        val startTime = intent.getLongExtra(AlarmScheduler.EXTRA_START_TIME, System.currentTimeMillis() + 15 * 60 * 1000L)

        binding.tvContestTitle.text = title
        binding.tvPlatformBadge.text = platform.uppercase()

        when (platform.uppercase()) {
            "LEETCODE" -> binding.tvPlatformBadge.setBackgroundColor(Color.parseColor("#F59E0B"))
            "CODECHEF" -> binding.tvPlatformBadge.setBackgroundColor(Color.parseColor("#8B5CF6"))
            "CUSTOM" -> binding.tvPlatformBadge.setBackgroundColor(Color.parseColor("#06B6D4"))
            else -> binding.tvPlatformBadge.setBackgroundColor(Color.parseColor("#EF4444"))
        }

        // Live Countdown
        startCountdown(startTime)

        binding.btnDismiss.setOnClickListener {
            stopAlarmService()
            finish()
        }

        binding.btnSnooze.setOnClickListener {
            stopAlarmService()
            finish()
        }
    }

    private fun startCountdown(startTime: Long) {
        val remainingMs = startTime - System.currentTimeMillis()
        countDownTimer?.cancel()

        countDownTimer = object : CountDownTimer(remainingMs, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val mins = (millisUntilFinished / 1000) / 60
                val secs = (millisUntilFinished / 1000) % 60
                binding.tvCountdown.text = String.format("Starts in %02dm %02ds", mins, secs)
            }

            override fun onFinish() {
                binding.tvCountdown.text = "CONTEST HAS STARTED!"
            }
        }.start()
    }

    private fun stopAlarmService() {
        val stopServiceIntent = Intent(this, LoudAlarmService::class.java).apply {
            action = LoudAlarmService.ACTION_STOP_ALARM
        }
        startService(stopServiceIntent)
    }

    override fun onDestroy() {
        countDownTimer?.cancel()
        super.onDestroy()
    }
}
