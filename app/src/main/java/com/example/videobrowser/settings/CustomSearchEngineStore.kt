package com.example.videobrowser.settings

import com.example.videobrowser.site.SiteHost
import com.example.videobrowser.storage.PreferenceStore
import com.example.videobrowser.utils.SafeUriParser
import com.example.videobrowser.utils.TextWhitespaceNormalizer
import com.example.videobrowser.utils.Utf8UrlCodec
import java.lang.Integer.toUnsignedString
import java.util.Locale

internal class CustomSearchEngineStore(
    private val preferenceStore: PreferenceStore
) {
    private val lineStore = PreferenceLineStore(preferenceStore, KEY_CUSTOM_SEARCH_ENGINES)

    fun load(): List<CustomSearchEngine> {
        return lineStore.loadLines()
            .mapNotNull(::parseLine)
            .distinctBy { engine -> engine.id }
            .toList()
            .takeLast(MAX_CUSTOM_SEARCH_ENGINES)
    }

    fun add(name: String, searchUrlPrefix: String): Boolean {
        val searchTemplate = templateFromPrefix(searchUrlPrefix) ?: return false
        return add(
            name = name,
            displayUrl = displayUrlFromTemplate(searchTemplate) ?: searchUrlPrefix,
            searchTemplate = searchTemplate,
            queryParam = queryParamFromTemplate(searchTemplate).orEmpty(),
            domains = domainsFromTemplate(searchTemplate),
            hideCss = emptyList(),
            hidePageSearchBox = false
        )
    }

    fun add(
        name: String,
        displayUrl: String,
        searchTemplate: String,
        queryParam: String,
        domains: List<String>,
        hideCss: List<String>,
        hidePageSearchBox: Boolean
    ): Boolean {
        val engine = normalize(
            id = generatedId(name, displayUrl, searchTemplate),
            name = name,
            displayUrl = displayUrl,
            searchTemplate = searchTemplate,
            queryParam = queryParam,
            domains = domains,
            hideCss = hideCss,
            hidePageSearchBox = hidePageSearchBox
        ) ?: return false
        val engines = load()
            .filterNot { existing ->
                existing.id == engine.id || hasSameDefinition(existing, engine)
            }
            .plus(engine)
            .takeLast(MAX_CUSTOM_SEARCH_ENGINES)
        save(engines)
        return true
    }

    fun remove(engine: CustomSearchEngine): Boolean {
        val normalizedEngine = normalize(engine) ?: return false
        val engines = load()
        val remainingEngines = engines.filterNot { existing ->
            existing.id == normalizedEngine.id
        }
        if (remainingEngines.size == engines.size) {
            return false
        }

        save(remainingEngines)
        return true
    }

    fun update(engine: CustomSearchEngine, name: String, searchUrlPrefix: String): Boolean {
        val searchTemplate = templateFromPrefix(searchUrlPrefix) ?: return false
        return update(
            engine = engine,
            name = name,
            displayUrl = displayUrlFromTemplate(searchTemplate) ?: searchUrlPrefix,
            searchTemplate = searchTemplate,
            queryParam = queryParamFromTemplate(searchTemplate).orEmpty(),
            domains = domainsFromTemplate(searchTemplate),
            hideCss = emptyList(),
            hidePageSearchBox = false
        )
    }

    fun update(
        engine: CustomSearchEngine,
        name: String,
        displayUrl: String,
        searchTemplate: String,
        queryParam: String,
        domains: List<String>,
        hideCss: List<String>,
        hidePageSearchBox: Boolean
    ): Boolean {
        val normalizedEngine = normalize(engine) ?: return false
        val updatedEngine = normalize(
            id = normalizedEngine.id,
            name = name,
            displayUrl = displayUrl,
            searchTemplate = searchTemplate,
            queryParam = queryParam,
            domains = domains,
            hideCss = hideCss,
            hidePageSearchBox = hidePageSearchBox
        ) ?: return false
        val engines = load().toMutableList()
        val index = engines.indexOfFirst { existing -> existing.id == normalizedEngine.id }
        if (index < 0) {
            return false
        }
        if (engines.withIndex().any { (engineIndex, existing) ->
                engineIndex != index && hasSameDefinition(existing, updatedEngine)
            }
        ) {
            return false
        }

        engines[index] = updatedEngine
        save(engines)
        return true
    }

    private fun parseLine(line: String): CustomSearchEngine? {
        val fields = TabSeparatedLineCodec.splitFields(line)
        return when (fields.size) {
            LEGACY_FIELD_COUNT -> normalize(
                id = fields[0],
                name = fields[1],
                searchUrlPrefix = fields[2]
            )

            FIELD_COUNT -> normalize(
                id = fields[0],
                name = fields[1],
                displayUrl = fields[3],
                searchTemplate = fields[4],
                queryParam = fields[5],
                domains = splitList(fields[6]),
                hideCss = splitList(fields[7]),
                hidePageSearchBox = fields[8].toBooleanStrictOrNull() ?: false
            )

            else -> null
        }
    }

    private fun save(engines: List<CustomSearchEngine>) {
        val lines = engines
            .mapNotNull(::normalize)
            .distinctBy { engine -> engine.id }
            .takeLast(MAX_CUSTOM_SEARCH_ENGINES)
            .map { engine ->
                TabSeparatedLineCodec.joinFields(
                    listOf(
                        engine.id,
                        engine.name,
                        engine.searchUrlPrefix,
                        engine.displayUrl,
                        engine.searchTemplate,
                        engine.queryParam,
                        joinList(engine.domains),
                        joinList(engine.hideCss),
                        engine.hidePageSearchBox.toString()
                    )
                )
            }

        lineStore.saveLines(lines)
    }

    private fun hasSameDefinition(
        first: CustomSearchEngine,
        second: CustomSearchEngine
    ): Boolean {
        return first.name == second.name &&
            first.displayUrl == second.displayUrl &&
            first.searchTemplate == second.searchTemplate &&
            first.queryParam == second.queryParam
    }

    private fun normalize(engine: CustomSearchEngine): CustomSearchEngine? {
        return normalize(
            id = engine.id,
            name = engine.name,
            displayUrl = engine.displayUrl,
            searchTemplate = engine.searchTemplate,
            queryParam = engine.queryParam,
            domains = engine.domains,
            hideCss = engine.hideCss,
            hidePageSearchBox = engine.hidePageSearchBox
        )
    }

    private fun normalize(
        id: String,
        name: String,
        searchUrlPrefix: String
    ): CustomSearchEngine? {
        val searchTemplate = templateFromPrefix(searchUrlPrefix) ?: return null
        return normalize(
            id = id,
            name = name,
            displayUrl = displayUrlFromTemplate(searchTemplate) ?: searchUrlPrefix,
            searchTemplate = searchTemplate,
            queryParam = queryParamFromTemplate(searchTemplate).orEmpty(),
            domains = domainsFromTemplate(searchTemplate),
            hideCss = emptyList(),
            hidePageSearchBox = false
        )
    }

    private fun normalize(
        id: String,
        name: String,
        displayUrl: String,
        searchTemplate: String,
        queryParam: String,
        domains: List<String>,
        hideCss: List<String>,
        hidePageSearchBox: Boolean
    ): CustomSearchEngine? {
        val normalizedId = id.trim()
        val normalizedName = TextWhitespaceNormalizer.collapse(name)
        val normalizedTemplate = normalizeTemplate(searchTemplate) ?: return null
        val normalizedQueryParam = queryParam.trim().ifBlank {
            queryParamFromTemplate(normalizedTemplate).orEmpty()
        }
        val normalizedDisplayUrl = normalizeDisplayUrl(displayUrl)
            ?: displayUrlFromTemplate(normalizedTemplate)
            ?: return null
        val normalizedDomains = domains
            .mapNotNull(SiteHost::normalize)
            .ifEmpty { domainsFromTemplate(normalizedTemplate) }
            .distinct()
        val normalizedHideCss = hideCss
            .mapNotNull(SettingsCssSelectorNormalizer::normalize)
            .distinct()
        if (!CUSTOM_SEARCH_ENGINE_ID_REGEX.matches(normalizedId)) {
            return null
        }
        if (normalizedName.isEmpty() || TextWhitespaceNormalizer.hasTabOrLineBreak(normalizedName)) {
            return null
        }
        if (!QUERY_PARAM_REGEX.matches(normalizedQueryParam)) {
            return null
        }
        if (normalizedDomains.isEmpty()) {
            return null
        }
        return CustomSearchEngine(
            id = normalizedId,
            name = normalizedName,
            searchUrlPrefix = normalizedTemplate.substringBefore(KEYWORD_PLACEHOLDER),
            displayUrl = normalizedDisplayUrl,
            searchTemplate = normalizedTemplate,
            queryParam = normalizedQueryParam,
            domains = normalizedDomains,
            hideCss = normalizedHideCss,
            hidePageSearchBox = hidePageSearchBox && normalizedHideCss.isNotEmpty()
        )
    }

    private fun generatedId(name: String, displayUrl: String, searchTemplate: String): String {
        val normalizedName = TextWhitespaceNormalizer.collapse(name)
        val hash = "$normalizedName\t${displayUrl.trim()}\t${searchTemplate.trim()}".hashCode()
        return "custom_${toUnsignedString(hash, ID_RADIX)}"
    }

    private fun templateFromPrefix(searchUrlPrefix: String): String? {
        val trimmed = searchUrlPrefix.trim()
        if (trimmed.isEmpty()) {
            return null
        }
        val template = if (trimmed.contains(KEYWORD_PLACEHOLDER)) {
            trimmed
        } else {
            "$trimmed$KEYWORD_PLACEHOLDER"
        }
        return normalizeTemplate(template)
    }

    private fun normalizeTemplate(searchTemplate: String): String? {
        val trimmed = searchTemplate.trim()
        if (!trimmed.contains(KEYWORD_PLACEHOLDER)) {
            return null
        }
        val parseableTemplate = trimmed.replace(KEYWORD_PLACEHOLDER, TEMPLATE_PARSE_VALUE)
        if (!SettingsHttpUrlValidator.isHttpUrl(parseableTemplate)) {
            return null
        }
        return trimmed
    }

    private fun normalizeDisplayUrl(displayUrl: String): String? {
        val normalized = displayUrl.trim().trimEnd('/')
        if (!SettingsHttpUrlValidator.isHttpUrl(normalized)) {
            return null
        }
        return normalized
    }

    private fun displayUrlFromTemplate(searchTemplate: String): String? {
        val uri = parseTemplateUri(searchTemplate) ?: return null
        val scheme = uri.scheme?.lowercase(Locale.ROOT) ?: return null
        val host = SiteHost.normalize(uri.host) ?: return null
        return "$scheme://$host"
    }

    private fun domainsFromTemplate(searchTemplate: String): List<String> {
        val uri = parseTemplateUri(searchTemplate) ?: return emptyList()
        return listOfNotNull(SiteHost.normalize(uri.host))
    }

    private fun queryParamFromTemplate(searchTemplate: String): String? {
        val uri = parseTemplateUri(searchTemplate) ?: return null
        return uri.rawQuery
            ?.split("&")
            ?.firstNotNullOfOrNull { part ->
                val rawName = part.substringBefore("=")
                val rawValue = part.substringAfter("=", missingDelimiterValue = "")
                val decodedName = Utf8UrlCodec.decodeFormComponent(rawName)
                if (rawValue == TEMPLATE_PARSE_VALUE) decodedName else null
            }
            ?.takeIf { it.isNotBlank() }
    }

    private fun parseTemplateUri(searchTemplate: String) =
        SafeUriParser.parse(searchTemplate.replace(KEYWORD_PLACEHOLDER, TEMPLATE_PARSE_VALUE))

    private fun splitList(value: String): List<String> {
        return value.split(LIST_SEPARATOR).filter { item -> item.isNotBlank() }
    }

    private fun joinList(values: List<String>): String {
        return values.joinToString(separator = LIST_SEPARATOR)
    }

    private companion object {
        private const val LEGACY_FIELD_COUNT = 3
        private const val FIELD_COUNT = 9
        private const val ID_RADIX = 36
        private const val KEYWORD_PLACEHOLDER = "{keyword}"
        private const val TEMPLATE_PARSE_VALUE = "videobrowser_keyword"
        private const val LIST_SEPARATOR = "\u001F"
        private val CUSTOM_SEARCH_ENGINE_ID_REGEX = Regex("custom_[a-z0-9]+")
        private val QUERY_PARAM_REGEX = Regex("[A-Za-z0-9._~-]{1,64}")
    }
}
