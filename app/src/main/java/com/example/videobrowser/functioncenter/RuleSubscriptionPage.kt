package com.example.videobrowser.functioncenter

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

    private fun addStatusSection(parent: LinearLayout) {
        host.addFunctionSection(
            parent,
            activity.getString(R.string.function_center_section_records)
        ) { section ->
            val metadata = readMetadata()
            if (metadata.isEmpty) {
                host.addEmptyState(section, activity.getString(R.string.rule_subscription_empty))
                return@addFunctionSection
            }
            host.addInfoRow(
                parent = section,
                title = activity.getString(R.string.rule_subscription_source),
                summary = metadata.getProperty(RuleFileLoader.METADATA_SOURCE_LABEL).orEmpty()
            )
            host.addInfoRow(
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

    private fun addActionSection(parent: LinearLayout) {
        host.addFunctionSection(
            parent,
            activity.getString(R.string.function_center_section_actions)
        ) { section ->
            host.addActionRow(
                parent = section,
                title = activity.getString(R.string.action_update_rule_subscription_url),
                summary = activity.getString(R.string.action_update_rule_subscription_url_summary)
            ) {
                showUpdateUrlDialog()
            }
            host.addActionRow(
                parent = section,
                title = activity.getString(R.string.action_import_rule_subscription_text),
                summary = activity.getString(R.string.action_import_rule_subscription_text_summary)
            ) {
                showImportTextDialog()
            }
            host.addDivider(section)
            host.addActionRow(
                parent = section,
                title = activity.getString(R.string.action_clear_rule_subscription_cache),
                summary = activity.getString(R.string.action_clear_rule_subscription_cache_summary),
                enabled = cacheDirectory.exists()
            ) {
                showClearCacheDialog()
            }
        }
    }

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
