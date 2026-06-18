package com.example.videobrowser.rules

import com.example.videobrowser.site.SiteHost

internal class RuleScriptletQuery(
    private val compiledRules: CompiledRuleSet
) {
    fun windowOpenBlockedKeywordsFor(pageUrl: String?): List<String> {
        return argumentsFor(
            pageUrl = pageUrl,
            hookName = ScriptletRegistry.HOOK_WINDOW_OPEN_BLOCK_KEYWORD
        )
    }

    fun fetchBlockedKeywordsFor(pageUrl: String?): List<String> {
        return argumentsFor(
            pageUrl = pageUrl,
            hookName = ScriptletRegistry.HOOK_FETCH_BLOCK_KEYWORD
        )
    }

    fun isSkipButtonsEnabledFor(pageUrl: String?): Boolean {
        return hooksFor(pageUrl).any { capability ->
            capability.hookName == ScriptletRegistry.HOOK_CLICK_SKIP_BUTTONS
        }
    }

    fun isVideoControlsEnabledFor(pageUrl: String?): Boolean {
        return hooksFor(pageUrl).any { capability ->
            capability.hookName == ScriptletRegistry.HOOK_ENABLE_VIDEO_CONTROLS
        }
    }

    private fun argumentsFor(pageUrl: String?, hookName: String): List<String> {
        return hooksFor(pageUrl)
            .filter { capability -> capability.hookName == hookName }
            .flatMap { capability -> capability.arguments }
            .distinct()
    }

    private fun hooksFor(pageUrl: String?): List<RuleCapability.SafeHook> {
        val pageHost = SiteHost.fromUrl(pageUrl)
        return compiledRules.safeHookCapabilities.filter { capability ->
            capability.domainScope.matches(pageHost)
        }
    }

}
