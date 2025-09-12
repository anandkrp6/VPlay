package com.bytecoder.vplay.settings

import android.content.Context
import android.content.SharedPreferences

object AppSettings {
    private const val PREFS = "vplay_settings"
    private const val KEY_SEEK_SENS = "seek_sens" // factor *100
    private const val KEY_VOL_SENS = "vol_sens"
    private const val KEY_BRIGHT_SENS = "bright_sens"

    private fun prefs(ctx: Context): SharedPreferences = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun getSeekSensitivity(ctx: Context): Float = (prefs(ctx).getInt(KEY_SEEK_SENS, 100)) / 100f
    fun getVolumeSensitivity(ctx: Context): Float = (prefs(ctx).getInt(KEY_VOL_SENS, 100)) / 100f
    fun getBrightnessSensitivity(ctx: Context): Float = (prefs(ctx).getInt(KEY_BRIGHT_SENS, 100)) / 100f

    fun setSeekSensitivity(ctx: Context, factor: Float) {
        prefs(ctx).edit().putInt(KEY_SEEK_SENS, (factor * 100).toInt().coerceIn(20, 200)).apply()
    }
    fun setVolumeSensitivity(ctx: Context, factor: Float) {
        prefs(ctx).edit().putInt(KEY_VOL_SENS, (factor * 100).toInt().coerceIn(20, 200)).apply()
    }
    fun setBrightnessSensitivity(ctx: Context, factor: Float) {
        prefs(ctx).edit().putInt(KEY_BRIGHT_SENS, (factor * 100).toInt().coerceIn(20, 200)).apply()
    }
}
