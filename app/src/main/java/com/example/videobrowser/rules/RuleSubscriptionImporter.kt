package com.example.videobrowser.rules

import com.example.videobrowser.adguard.AdGuardRuleParser
import java.io.File
import java.util.Locale
import java.util.Properties

class RuleSubscriptionImporter(
    private val cacheDirectory: File,
    private val parser: AdGuardRuleParser = AdGuardRuleParser()
) {
    fun importText(subscriptionId: String, text: String): RuleSubscriptionImportResult {
        val normalizedId = normalizeSubscriptionId(subscriptionId)
        val parseResult = parser.parseSubscription(
            text = text,
            source = "subscription:$normalizedId"
        )
        cacheDirectory.mkdirs()

        val writes = listOf(
            RuleFileLoader.REQUEST_RULES_CACHE_FILE to parseResult.requestRuleLines,
            RuleFileLoader.CSS_RULES_CACHE_FILE to parseResult.cssRuleLines,
            RuleFileLoader.DOM_RULES_CACHE_FILE to emptyList<String>(),
            RuleFileLoader.SCRIPTLET_RULES_CACHE_FILE to parseResult.scriptletRuleLines,
            RuleFileLoader.REMOVE_PARAM_RULES_CACHE_FILE to parseResult.removeParamRuleLines
        )
        val tempFiles = writes.map { (fileName, lines) ->
            val tempFile = cacheDirectory.resolve("$fileName.tmp")
            tempFile.writeText(renderLines(lines), Charsets.UTF_8)
            fileName to tempFile
        }

        tempFiles.forEach { (fileName, tempFile) ->
            val targetFile = cacheDirectory.resolve(fileName)
            if (targetFile.exists()) {
                targetFile.delete()
            }
            tempFile.renameTo(targetFile)
        }
        writeMetadata(normalizedId, parseResult)

        return RuleSubscriptionImportResult(
            updated = true,
            requestRuleCount = parseResult.requestRules.size,
            cssRuleCount = parseResult.elementRules.size,
            scriptletRuleCount = parseResult.scriptletRules.size,
            removeParamRuleCount = parseResult.removeParamRules.size,
            skippedRuleCount = parseResult.skippedRules.size
        )
    }

    fun update(subscriptionId: String, fetchText: () -> String): RuleSubscriptionImportResult {
        return runCatching {
            importText(subscriptionId, fetchText())
        }.getOrElse { error ->
            RuleSubscriptionImportResult(
                updated = false,
                usedExistingCache = hasExistingCache(),
                errorMessage = error.message ?: error::class.java.simpleName
            )
        }
    }

    private fun writeMetadata(
        subscriptionId: String,
        parseResult: com.example.videobrowser.adguard.AdGuardParseResult
    ) {
        val properties = Properties().apply {
            setProperty(RuleFileLoader.METADATA_SOURCE_LABEL, "subscription:$subscriptionId")
            setProperty("request_rule_count", parseResult.requestRules.size.toString())
            setProperty("css_rule_count", parseResult.elementRules.size.toString())
            setProperty("scriptlet_rule_count", parseResult.scriptletRules.size.toString())
            setProperty("removeparam_rule_count", parseResult.removeParamRules.size.toString())
            setProperty("skipped_rule_count", parseResult.skippedRules.size.toString())
            setProperty("updated_at_epoch_ms", System.currentTimeMillis().toString())
        }
        cacheDirectory.resolve(RuleFileLoader.RULE_CACHE_METADATA_FILE)
            .outputStream()
            .use { output -> properties.store(output, "VideoBrowser rule subscription cache") }
    }

    private fun hasExistingCache(): Boolean {
        return listOf(
            RuleFileLoader.REQUEST_RULES_CACHE_FILE,
            RuleFileLoader.CSS_RULES_CACHE_FILE,
            RuleFileLoader.DOM_RULES_CACHE_FILE,
            RuleFileLoader.SCRIPTLET_RULES_CACHE_FILE,
            RuleFileLoader.REMOVE_PARAM_RULES_CACHE_FILE
        ).any { fileName -> cacheDirectory.resolve(fileName).isFile }
    }

    private fun renderLines(lines: List<String>): String {
        return if (lines.isEmpty()) {
            ""
        } else {
            lines.joinToString(separator = "\n", postfix = "\n")
        }
    }

    private fun normalizeSubscriptionId(subscriptionId: String): String {
        val normalized = subscriptionId
            .trim()
            .lowercase(Locale.US)
            .replace(Regex("\\s+"), "-")
            .filter { char -> char.isLetterOrDigit() || char == '-' || char == '_' || char == '.' }
            .trim('-', '_', '.')
        return normalized.ifBlank { "manual" }
    }
}

data class RuleSubscriptionImportResult(
    val updated: Boolean,
    val usedExistingCache: Boolean = false,
    val errorMessage: String? = null,
    val requestRuleCount: Int = 0,
    val cssRuleCount: Int = 0,
    val scriptletRuleCount: Int = 0,
    val removeParamRuleCount: Int = 0,
    val skippedRuleCount: Int = 0
)
