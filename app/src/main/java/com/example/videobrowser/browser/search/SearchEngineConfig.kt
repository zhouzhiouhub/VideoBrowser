package com.example.videobrowser.browser.search

/**
 * Describes one search engine as both a user-visible entry and an internal
 * URL/query/page-cleanup rule.
 */
data class SearchEngineConfig(
    val name: String,
    val displayUrl: String,
    val searchTemplate: String,
    val queryParam: String,
    val domains: List<String>,
    val hideCss: List<String> = emptyList(),
    val hidePageSearchBox: Boolean = false
)

