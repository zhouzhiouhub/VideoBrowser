package com.example.videobrowser.settings

import com.example.videobrowser.storage.PreferenceStore

internal class CustomShortcutStore(
    private val preferenceStore: PreferenceStore
) {
    fun load(): List<CustomShortcut> {
        return preferenceStore.getString(KEY_CUSTOM_SHORTCUTS, null)
            ?.lineSequence()
            ?.mapNotNull(::parseLine)
            ?.distinct()
            ?.toList()
            ?.takeLast(MAX_CUSTOM_SHORTCUTS)
            ?: emptyList()
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
        val separatorIndex = line.indexOf('\t')
        if (separatorIndex <= 0 || separatorIndex >= line.lastIndex) {
            return null
        }
        return normalize(
            line.substring(0, separatorIndex),
            line.substring(separatorIndex + 1)
        )
    }

    private fun save(shortcuts: List<CustomShortcut>) {
        val lines = shortcuts
            .mapNotNull { shortcut -> normalize(shortcut.name, shortcut.url) }
            .distinct()
            .takeLast(MAX_CUSTOM_SHORTCUTS)
            .map { shortcut -> "${shortcut.name}\t${shortcut.url}" }

        if (lines.isEmpty()) {
            preferenceStore.remove(KEY_CUSTOM_SHORTCUTS)
        } else {
            preferenceStore.putString(KEY_CUSTOM_SHORTCUTS, lines.joinToString(separator = "\n"))
        }
    }

    private fun normalize(name: String, url: String): CustomShortcut? {
        val normalizedName = name.trim().replace(WHITESPACE_SEQUENCE, " ")
        val normalizedUrl = url.trim()
        if (normalizedName.isEmpty() || normalizedName.any { it == '\t' || it == '\n' || it == '\r' }) {
            return null
        }
        if (!SettingsHttpUrlValidator.isHttpUrl(normalizedUrl)) {
            return null
        }
        return CustomShortcut(name = normalizedName, url = normalizedUrl)
    }

    private companion object {
        private val WHITESPACE_SEQUENCE = Regex("\\s+")
    }
}

