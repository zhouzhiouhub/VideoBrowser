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
    /**
     * 函数 `contains`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param key 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun contains(key: String): Boolean

    /**
     * 函数 `getBoolean`：从现有状态、缓存或输入对象中取得目标数据，并把结果交给调用方继续处理。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param key 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param defaultValue 参数类型为 `Boolean`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun getBoolean(key: String, defaultValue: Boolean): Boolean

    /**
     * 函数 `putBoolean`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param key 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param value 参数类型为 `Boolean`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     */
    fun putBoolean(key: String, value: Boolean)

    /**
     * 函数 `getFloat`：从现有状态、缓存或输入对象中取得目标数据，并把结果交给调用方继续处理。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param key 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param defaultValue 参数类型为 `Float`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun getFloat(key: String, defaultValue: Float): Float

    /**
     * 函数 `putFloat`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param key 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param value 参数类型为 `Float`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     */
    fun putFloat(key: String, value: Float)

    /**
     * 函数 `getString`：从现有状态、缓存或输入对象中取得目标数据，并把结果交给调用方继续处理。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param key 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param defaultValue 参数类型为 `String?`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun getString(key: String, defaultValue: String? = null): String?

    /**
     * 函数 `putString`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param key 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param value 参数类型为 `String`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     */
    fun putString(key: String, value: String)

    /**
     * 函数 `remove`：封装 `remove` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param key 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     */
    fun remove(key: String)

    /**
     * 函数 `remove`：封装 `remove` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param keys 参数类型为 `Iterable<String>`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param commit 参数类型为 `Boolean`，表示函数执行 `commit` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun remove(keys: Iterable<String>, commit: Boolean = false): Boolean

    companion object {
        const val FILE_NAME = "browser_preferences"

        /**
         * 函数 `from`：封装 `from` 这一段业务步骤，让调用方不用关心内部实现细节。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param context 参数类型为 `Context`，表示 Android 上下文，用来读取资源、启动系统服务或访问应用环境。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
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
    /**
     * 函数 `contains`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param key 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    override fun contains(key: String): Boolean {
        return preferences.contains(key)
    }

    /**
     * 函数 `getBoolean`：从现有状态、缓存或输入对象中取得目标数据，并把结果交给调用方继续处理。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param key 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param defaultValue 参数类型为 `Boolean`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return preferences.getBoolean(key, defaultValue)
    }

    /**
     * 函数 `putBoolean`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param key 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param value 参数类型为 `Boolean`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     */
    override fun putBoolean(key: String, value: Boolean) {
        preferences.edit().putBoolean(key, value).apply()
    }

    /**
     * 函数 `getFloat`：从现有状态、缓存或输入对象中取得目标数据，并把结果交给调用方继续处理。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param key 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param defaultValue 参数类型为 `Float`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    override fun getFloat(key: String, defaultValue: Float): Float {
        return preferences.getFloat(key, defaultValue)
    }

    /**
     * 函数 `putFloat`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param key 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param value 参数类型为 `Float`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     */
    override fun putFloat(key: String, value: Float) {
        preferences.edit().putFloat(key, value).apply()
    }

    /**
     * 函数 `getString`：从现有状态、缓存或输入对象中取得目标数据，并把结果交给调用方继续处理。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param key 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param defaultValue 参数类型为 `String?`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    override fun getString(key: String, defaultValue: String?): String? {
        return preferences.getString(key, defaultValue)
    }

    /**
     * 函数 `putString`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param key 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param value 参数类型为 `String`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     */
    override fun putString(key: String, value: String) {
        preferences.edit().putString(key, value).apply()
    }

    /**
     * 函数 `remove`：封装 `remove` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param key 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     */
    override fun remove(key: String) {
        preferences.edit().remove(key).apply()
    }

    /**
     * 函数 `remove`：封装 `remove` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param keys 参数类型为 `Iterable<String>`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param commit 参数类型为 `Boolean`，表示函数执行 `commit` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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
