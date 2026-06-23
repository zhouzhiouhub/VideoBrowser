package com.example.videobrowser.functioncenter

/**
 * 初学者阅读提示：
 * 这个文件属于“功能中心模块”。
 * 文件名 RuleSubscriptionPage 可以拆开理解为“Rule Subscription Page”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：构建底部功能面板、设置页面、数据管理页面以及各种用户可点击的工具入口。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
import android.text.InputType
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.videobrowser.R
import com.example.videobrowser.rules.RuleEngineFactory
import com.example.videobrowser.rules.RuleFileLoader
import com.example.videobrowser.rules.RuleSubscriptionFetcher
import com.example.videobrowser.rules.RuleSubscriptionImportResult
import com.example.videobrowser.rules.RuleSubscriptionImporter
import java.io.File
import java.util.Properties

class RuleSubscriptionPage(
    private val host: FunctionCenterPageHost,
    private val filesDir: File,
    private val onRulesChanged: () -> Unit,
    private val showRootPage: () -> Unit
) {
    private val activity = host.activity
    private val ruleSubscriptionFetcher = RuleSubscriptionFetcher()
    private val cacheDirectory: File
        get() = RuleEngineFactory.ruleCacheDirectory(filesDir)

    /**
     * 函数 `show`：控制 `show` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param replaceCurrent 参数类型为 `Boolean`，表示函数执行 `replaceCurrent` 相关逻辑时需要读取或处理的输入。
     */
    fun show(replaceCurrent: Boolean = false) {
        host.showPage(
            title = activity.getString(R.string.title_rule_subscriptions),
            onBack = showRootPage,
            replaceCurrent = replaceCurrent
        ) { content ->
            addStatusSection(content)
            addActionSection(content)
        }
    }

    /**
     * 函数 `addStatusSection`：封装 `add Status Section` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param parent 参数类型为 `LinearLayout`，表示函数执行 `parent` 相关逻辑时需要读取或处理的输入。
     */
    private fun addStatusSection(parent: LinearLayout) {
        host.contentFactory.addFunctionSection(
            parent,
            activity.getString(R.string.function_center_section_records)
        ) { section ->
            val metadata = readMetadata()
            if (metadata.isEmpty) {
                host.contentFactory.addEmptyState(section, activity.getString(R.string.rule_subscription_empty))
                return@addFunctionSection
            }
            host.contentFactory.addInfoRow(
                parent = section,
                title = activity.getString(R.string.rule_subscription_source),
                summary = metadata.getProperty(RuleFileLoader.METADATA_SOURCE_LABEL).orEmpty()
            )
            host.contentFactory.addInfoRow(
                parent = section,
                title = activity.getString(R.string.rule_subscription_counts),
                summary = activity.getString(
                    R.string.rule_subscription_counts_summary,
                    metadata.getProperty("request_rule_count", "0"),
                    metadata.getProperty("css_rule_count", "0"),
                    metadata.getProperty("scriptlet_rule_count", "0"),
                    metadata.getProperty("removeparam_rule_count", "0"),
                    metadata.getProperty("skipped_rule_count", "0")
                )
            )
        }
    }

    /**
     * 函数 `addActionSection`：封装 `add Action Section` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param parent 参数类型为 `LinearLayout`，表示函数执行 `parent` 相关逻辑时需要读取或处理的输入。
     */
    private fun addActionSection(parent: LinearLayout) {
        host.contentFactory.addFunctionSection(
            parent,
            activity.getString(R.string.function_center_section_actions)
        ) { section ->
            host.contentFactory.addActionRow(
                parent = section,
                title = activity.getString(R.string.action_update_rule_subscription_url),
                summary = activity.getString(R.string.action_update_rule_subscription_url_summary)
            ) {
                showUpdateUrlDialog()
            }
            host.contentFactory.addActionRow(
                parent = section,
                title = activity.getString(R.string.action_import_rule_subscription_text),
                summary = activity.getString(R.string.action_import_rule_subscription_text_summary)
            ) {
                showImportTextDialog()
            }
            host.contentFactory.addDivider(section)
            host.contentFactory.addActionRow(
                parent = section,
                title = activity.getString(R.string.action_clear_rule_subscription_cache),
                summary = activity.getString(R.string.action_clear_rule_subscription_cache_summary),
                enabled = cacheDirectory.exists()
            ) {
                showClearCacheDialog()
            }
        }
    }

    /**
     * 函数 `showUpdateUrlDialog`：控制 `show Update Url Dialog` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun showUpdateUrlDialog() {
        val input = EditText(activity).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
            imeOptions = EditorInfo.IME_ACTION_DONE
            setSingleLine(true)
            hint = activity.getString(R.string.hint_rule_subscription_url)
        }
        AlertDialog.Builder(activity)
            .setTitle(R.string.action_update_rule_subscription_url)
            .setView(input)
            .setPositiveButton(R.string.action_update) { _, _ ->
                val url = input.text?.toString()?.trim().orEmpty()
                if (url.isNotEmpty()) {
                    runImport {
                        RuleSubscriptionImporter(cacheDirectory).update(
                            subscriptionId = RuleSubscriptionFetcher.subscriptionIdForUrl(url),
                            fetchText = { ruleSubscriptionFetcher.fetchText(url) }
                        )
                    }
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    /**
     * 函数 `showImportTextDialog`：控制 `show Import Text Dialog` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun showImportTextDialog() {
        val input = EditText(activity).apply {
            inputType = InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            minLines = 6
            maxLines = 12
            hint = activity.getString(R.string.hint_rule_subscription_text)
        }
        AlertDialog.Builder(activity)
            .setTitle(R.string.action_import_rule_subscription_text)
            .setView(input)
            .setPositiveButton(R.string.action_import) { _, _ ->
                val text = input.text?.toString().orEmpty()
                if (text.isNotBlank()) {
                    runImport {
                        RuleSubscriptionImporter(cacheDirectory).importText(
                            subscriptionId = "manual",
                            text = text
                        )
                    }
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    /**
     * 函数 `showClearCacheDialog`：控制 `show Clear Cache Dialog` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun showClearCacheDialog() {
        AlertDialog.Builder(activity)
            .setTitle(R.string.action_clear_rule_subscription_cache)
            .setMessage(R.string.dialog_clear_rule_subscription_cache_message)
            .setPositiveButton(R.string.action_clear) { _, _ ->
                if (RuleEngineFactory.clearRuleCache(filesDir)) {
                    Toast.makeText(activity, R.string.toast_rule_subscription_cache_cleared, Toast.LENGTH_SHORT).show()
                    onRulesChanged()
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    /**
     * 函数 `runImport`：封装 `run Import` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param importAction 参数类型为 `() -> RuleSubscriptionImportResult`，表示函数执行 `importAction` 相关逻辑时需要读取或处理的输入。
     */
    private fun runImport(importAction: () -> RuleSubscriptionImportResult) {
        Thread {
            val result = runCatching { importAction() }.getOrElse { error ->
                RuleSubscriptionImportResult(
                    updated = false,
                    usedExistingCache = cacheDirectory.exists(),
                    errorMessage = error.message ?: error::class.java.simpleName
                )
            }
            activity.runOnUiThread {
                if (result.updated) {
                    Toast.makeText(
                        activity,
                        activity.getString(
                            R.string.toast_rule_subscription_imported,
                            result.requestRuleCount,
                            result.cssRuleCount,
                            result.scriptletRuleCount,
                            result.removeParamRuleCount,
                            result.skippedRuleCount
                        ),
                        Toast.LENGTH_LONG
                    ).show()
                    onRulesChanged()
                } else {
                    Toast.makeText(
                        activity,
                        activity.getString(
                            R.string.toast_rule_subscription_update_failed,
                            result.errorMessage.orEmpty()
                        ),
                        Toast.LENGTH_LONG
                    ).show()
                    show(replaceCurrent = true)
                }
            }
        }.start()
    }

    /**
     * 函数 `readMetadata`：封装 `read Metadata` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun readMetadata(): Properties {
        val metadataFile = cacheDirectory.resolve(RuleFileLoader.RULE_CACHE_METADATA_FILE)
        if (!metadataFile.isFile) {
            return Properties()
        }
        return runCatching {
            metadataFile.inputStream().use { input ->
                Properties().apply { load(input) }
            }
        }.getOrElse { Properties() }
    }

}
