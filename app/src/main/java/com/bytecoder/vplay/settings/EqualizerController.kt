package com.bytecoder.vplay.settings

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.bytecoder.vplay.media.PlayerManager

object EqualizerController {
    private var eq: Equalizer? = null
    private var bass: BassBoost? = null
    private var virt: Virtualizer? = null
    private var sessionId: Int = 0

    @OptIn(UnstableApi::class)
    fun attach(context: android.content.Context) {
        val player = PlayerManager.getOrCreate(context)
        val id = player.audioSessionId
        if (id == sessionId && eq != null) return
        release()
        sessionId = id
        try {
            eq = Equalizer(0, sessionId).apply { enabled = true }
            bass = BassBoost(0, sessionId).apply { enabled = true }
            virt = Virtualizer(0, sessionId).apply { enabled = true }
            // Restore simple settings
            val prefs = context.getSharedPreferences("vplay_eq", android.content.Context.MODE_PRIVATE)
            val bassStr = prefs.getInt("bass", 0).toShort()
            val virtStr = prefs.getInt("virt", 0).toShort()
            bass?.setStrength(bassStr)
            virt?.setStrength(virtStr)
            val bandCount = eq?.numberOfBands?.toInt() ?: 0
            for (i in 0 until bandCount) {
                val level = prefs.getInt("band_$i", 0).toShort()
                val range = eq?.bandLevelRange ?: shortArrayOf(-1500, 1500)
                val clamped = level.coerceIn(range[0], range[1])
                eq?.setBandLevel(i.toShort(), clamped)
            }
        } catch (e: Throwable) {
            Log.w("EqualizerController", "Failed to attach EQ", e)
        }
    }

    fun getBandCount(): Int = eq?.numberOfBands?.toInt() ?: 0
    fun getBandLevelRange(): ShortArray = eq?.bandLevelRange ?: shortArrayOf(-1500, 1500)
    fun getCenterFreq(band: Int): Int = eq?.getCenterFreq(band.toShort()) ?: 0
    fun getBandLevel(band: Int): Short = eq?.getBandLevel(band.toShort()) ?: 0
    fun setBandLevel(band: Short, level: Short) {
        try {
            eq?.setBandLevel(band, level)
        } catch (e: Exception) {
            Log.w("EqualizerController", "Failed to set band level", e)
        }
    }
    
    fun setEnabled(enabled: Boolean) {
        try {
            eq?.enabled = enabled
            bass?.enabled = enabled
            virt?.enabled = enabled
        } catch (e: Exception) {
            Log.w("EqualizerController", "Failed to set enabled state", e)
        }
    }
    
    fun isEnabled(): Boolean = eq?.enabled ?: false
    
    fun setBandLevel(context: android.content.Context, band: Int, level: Short) {
        eq?.setBandLevel(band.toShort(), level)
        context.getSharedPreferences("vplay_eq", android.content.Context.MODE_PRIVATE)
            .edit().putInt("band_$band", level.toInt()).apply()
    }

    fun setBass(context: android.content.Context, strength: Short) {
        bass?.setStrength(strength)
        context.getSharedPreferences("vplay_eq", android.content.Context.MODE_PRIVATE)
            .edit().putInt("bass", strength.toInt()).apply()
    }

    fun setVirtualizer(context: android.content.Context, strength: Short) {
        virt?.setStrength(strength)
        context.getSharedPreferences("vplay_eq", android.content.Context.MODE_PRIVATE)
            .edit().putInt("virt", strength.toInt()).apply()
    }

    fun release() {
        eq?.release(); eq = null
        bass?.release(); bass = null
        virt?.release(); virt = null
        sessionId = 0
    }
}
