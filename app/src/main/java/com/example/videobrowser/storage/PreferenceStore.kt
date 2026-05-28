package com.example.videobrowser.storage

import android.content.Context
import android.content.SharedPreferences

interface PreferenceStore {
    fun contains(key: String): Boolean

    fun getBoolean(key: String, defaultValue: Boolean): Boolean

    fun putBoolean(key: String, value: Boolean)

    fun getFloat(key: String, defaultValue: Float): Float

    fun putFloat(key: String, value: Float)

    fun getString(key: String, defaultValue: String? = null): String?

    fun putString(key: String, value: String)

    fun remove(key: String)

    fun remove(keys: Iterable<String>, commit: Boolean = false): Boolean

    companion object {
        const val FILE_NAME = "browser_preferences"

        fun from(context: Context): PreferenceStore {
            return SharedPreferencesStore(
                context.applicationContext.getSharedPreferences(
                    FILE_NAME,
                    Context.MODE_PRIVATE
                )
            )
        }
    }
}

private class SharedPreferencesStore(
    private val preferences: SharedPreferences
) : PreferenceStore {
    override fun contains(key: String): Boolean {
        return preferences.contains(key)
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return preferences.getBoolean(key, defaultValue)
    }

    override fun putBoolean(key: String, value: Boolean) {
        preferences.edit().putBoolean(key, value).apply()
    }

    override fun getFloat(key: String, defaultValue: Float): Float {
        return preferences.getFloat(key, defaultValue)
    }

    override fun putFloat(key: String, value: Float) {
        preferences.edit().putFloat(key, value).apply()
    }

    override fun getString(key: String, defaultValue: String?): String? {
        return preferences.getString(key, defaultValue)
    }

    override fun putString(key: String, value: String) {
        preferences.edit().putString(key, value).apply()
    }

    override fun remove(key: String) {
        preferences.edit().remove(key).apply()
    }

    override fun remove(keys: Iterable<String>, commit: Boolean): Boolean {
        val editor = preferences.edit()
        keys.forEach { key -> editor.remove(key) }
        return if (commit) {
            editor.commit()
        } else {
            editor.apply()
            true
        }
    }
}
