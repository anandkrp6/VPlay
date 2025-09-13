package com.bytecoder.vplay.settings

import android.content.Context
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

object Bookmarks {
    private const val PREFS = "vplay_bookmarks"
    private const val KEY_URLS = "urls"

    private fun prefs(ctx: Context) = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun add(ctx: Context, url: String) {
        if (url.isBlank()) return
        val list = getAll(ctx).toMutableList()
        if (!list.contains(url)) list.add(0, url) else {
            // move to top
            list.remove(url); list.add(0, url)
        }
        save(ctx, list)
    }

    fun remove(ctx: Context, url: String) {
        val list = getAll(ctx).toMutableList(); list.remove(url); save(ctx, list)
    }

    fun getAll(ctx: Context): List<String> {
        val json = prefs(ctx).getString(KEY_URLS, null) ?: return emptyList()
        return try {
            Json.parseToJsonElement(json).jsonArray.map { it.jsonPrimitive.content }
        } catch (_: Exception) { emptyList() }
    }

    private fun save(ctx: Context, list: List<String>) {
        val arr = buildJsonArray { list.forEach { add(kotlinx.serialization.json.JsonPrimitive(it)) } }
        prefs(ctx).edit().putString(KEY_URLS, Json.encodeToString(JsonArray.serializer(), arr)).apply()
    }
}
