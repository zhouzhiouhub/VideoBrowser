package com.example.videobrowser.settings

import com.example.videobrowser.storage.PreferenceStore
import com.example.videobrowser.utils.TextWhitespaceNormalizer

internal class CustomShortcutStore(
    private val preferenceStore: PreferenceStore
) {
    private val lineStore = PreferenceLineStore(preferenceStore, KEY_CUSTOM_SHORTCUTS)

    fun load(): List<CustomShortcut> {
        return lineStore.loadLines()
            .mapNotNull(::parseLine)
            .distinct()
            .toList()
            .takeLast(MAX_CUSTOM_SHORTCUTS)
    }

    fun add(name: String, url: String): Boolean {
        val shortcut = normalize(name, url) ?: return false
        val shortcuts = load()
            .filterNot { existing -> existing == shortcut }
            .plus(shortcut)
            .takeLast(MAX_CUSTOM_SHORTCUTS)
        save(shortcuts)
        return true
    }

    fun remove(shortcut: CustomShortcut): Boolean {
        val normalizedShortcut = normalize(shortcut.name, shortcut.url) ?: return false
        val shortcuts = load()
        val remainingShortcuts = shortcuts.filterNot { existing -> existing == normalizedShortcut }
        if (remainingShortcuts.size == shortcuts.size) {
            return false
        }
        save(remainingShortcuts)
        return true
    }

    fun update(shortcut: CustomShortcut, name: String, url: String): Boolean {
        val normalizedShortcut = normalize(shortcut.name, shortcut.url) ?: return false
        val updatedShortcut = normalize(name, url) ?: return false
        val shortcuts = load().toMutableList()
        val index = shortcuts.indexOf(normalizedShortcut)
        if (index < 0) {
            return false
        }

        shortcuts[index] = updatedShortcut
        save(shortcuts.distinct())
        return true
    }

    private fun parseLine(line: String): CustomShortcut? {
        val fields = TabSeparatedLineCodec.splitPair(line) ?: return null
        return normalize(
            fields.first,
            fields.second
        )
    }

    private fun save(shortcuts: List<CustomShortcut>) {
        val lines = shortcuts
            .mapNotNull { shortcut -> normalize(shortcut.name, shortcut.url) }
            .distinct()
            .takeLast(MAX_CUSTOM_SHORTCUTS)
            .map { shortcut -> TabSeparatedLineCodec.joinPair(shortcut.name, shortcut.url) }

        lineStore.saveLines(lines)
    }

    private fun normalize(name: String, url: String): CustomShortcut? {
        val normalizedName = TextWhitespaceNormalizer.collapse(name)
        val normalizedUrl = url.trim()
        if (normalizedName.isEmpty() || TextWhitespaceNormalizer.hasTabOrLineBreak(normalizedName)) {
            return null
        }
        if (!SettingsHttpUrlValidator.isHttpUrl(normalizedUrl)) {
            return null
        }
        return CustomShortcut(name = normalizedName, url = normalizedUrl)
    }
}
