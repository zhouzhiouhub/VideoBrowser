package com.example.videobrowser.testutil

import com.example.videobrowser.storage.PreferenceStore

class InMemoryPreferenceStore : PreferenceStore {
    private val values = mutableMapOf<String, Any>()

    override fun contains(key: String): Boolean {
        return values.containsKey(key)
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return values[key] as? Boolean ?: defaultValue
    }

    override fun putBoolean(key: String, value: Boolean) {
        values[key] = value
    }

    override fun getFloat(key: String, defaultValue: Float): Float {
        return values[key] as? Float ?: defaultValue
    }

    override fun putFloat(key: String, value: Float) {
        values[key] = value
    }

    override fun getString(key: String, defaultValue: String?): String? {
        return values[key] as? String ?: defaultValue
    }

    override fun putString(key: String, value: String) {
        values[key] = value
    }

    override fun remove(key: String) {
        values.remove(key)
    }

    override fun remove(keys: Iterable<String>, commit: Boolean): Boolean {
        keys.forEach { key -> values.remove(key) }
        return true
    }
}
