package com.bytecoder.vplay.onboarding

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.bytecoder.vplay.MainActivity
import com.bytecoder.vplay.R

class OnboardingActivity : AppCompatActivity() {
    companion object {
        private const val PREFS = "vplay_prefs"
        private const val KEY_ONBOARDED = "onboarded"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        if (prefs.getBoolean(KEY_ONBOARDED, false)) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }
        setContentView(R.layout.activity_onboarding)
        findViewById<Button>(R.id.btn_get_started).setOnClickListener {
            prefs.edit().putBoolean(KEY_ONBOARDED, true).apply()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
