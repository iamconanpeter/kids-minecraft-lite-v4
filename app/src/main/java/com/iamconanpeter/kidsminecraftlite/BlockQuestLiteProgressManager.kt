package com.iamconanpeter.kidsminecraftlite

import android.content.SharedPreferences

class BlockQuestLiteProgressManager(
    private val prefs: SharedPreferences
) {
    companion object {
        private const val KEY_STATE = "v4_state"
    }

    fun load(): String? = prefs.getString(KEY_STATE, null)

    fun save(payload: String) {
        prefs.edit().putString(KEY_STATE, payload).apply()
    }
}
