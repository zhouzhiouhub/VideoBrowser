package com.example.videobrowser.storage

/**
 * 初学者阅读提示：
 * 这个文件属于“收藏与历史存储模块”。
 * 文件名 PreferenceStore 可以拆开理解为“Preference Store”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：读写收藏夹、浏览历史、导入导出数据，并提供搜索和过滤能力。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
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
