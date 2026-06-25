package com.example.videobrowser.settings

import com.example.videobrowser.storage.PreferenceStore
import com.example.videobrowser.utils.TextWhitespaceNormalizer
import java.lang.Integer.toUnsignedString

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
        val engine = normalize(
            id = generatedId(name, searchUrlPrefix),
            name = name,
            searchUrlPrefix = searchUrlPrefix
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
        val normalizedEngine = normalize(
            id = engine.id,
            name = engine.name,
            searchUrlPrefix = engine.searchUrlPrefix
        ) ?: return false
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
        val normalizedEngine = normalize(
            id = engine.id,
            name = engine.name,
            searchUrlPrefix = engine.searchUrlPrefix
        ) ?: return false
        val updatedEngine = normalize(
            id = normalizedEngine.id,
            name = name,
            searchUrlPrefix = searchUrlPrefix
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
        if (fields.size != FIELD_COUNT) {
            return null
        }
        return normalize(
            id = fields[0],
            name = fields[1],
            searchUrlPrefix = fields[2]
        )
    }

    private fun save(engines: List<CustomSearchEngine>) {
        val lines = engines
            .mapNotNull { engine ->
                normalize(
                    id = engine.id,
                    name = engine.name,
                    searchUrlPrefix = engine.searchUrlPrefix
                )
            }
            .distinctBy { engine -> engine.id }
            .takeLast(MAX_CUSTOM_SEARCH_ENGINES)
            .map { engine ->
                TabSeparatedLineCodec.joinFields(
                    listOf(engine.id, engine.name, engine.searchUrlPrefix)
                )
            }

        lineStore.saveLines(lines)
    }

    private fun hasSameDefinition(
        first: CustomSearchEngine,
        second: CustomSearchEngine
    ): Boolean {
        return first.name == second.name &&
            first.searchUrlPrefix == second.searchUrlPrefix
    }

    private fun normalize(
        id: String,
        name: String,
        searchUrlPrefix: String
    ): CustomSearchEngine? {
        val normalizedId = id.trim()
        val normalizedName = TextWhitespaceNormalizer.collapse(name)
        val normalizedSearchUrlPrefix = searchUrlPrefix.trim()
        if (!CUSTOM_SEARCH_ENGINE_ID_REGEX.matches(normalizedId)) {
            return null
        }
        if (normalizedName.isEmpty() || TextWhitespaceNormalizer.hasTabOrLineBreak(normalizedName)) {
            return null
        }
        if (!SettingsHttpUrlValidator.isHttpUrl(normalizedSearchUrlPrefix)) {
            return null
        }
        return CustomSearchEngine(
            id = normalizedId,
            name = normalizedName,
            searchUrlPrefix = normalizedSearchUrlPrefix
        )
    }

    private fun generatedId(name: String, searchUrlPrefix: String): String {
        val normalizedName = TextWhitespaceNormalizer.collapse(name)
        val normalizedSearchUrlPrefix = searchUrlPrefix.trim()
        val hash = "$normalizedName\t$normalizedSearchUrlPrefix".hashCode()
        return "custom_${toUnsignedString(hash, ID_RADIX)}"
    }

    private companion object {
        private const val FIELD_COUNT = 3
        private const val ID_RADIX = 36
        private val CUSTOM_SEARCH_ENGINE_ID_REGEX = Regex("custom_[a-z0-9]+")
    }
}
