package com.example.videobrowser.browser

import com.example.videobrowser.storage.PreferenceStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class BrowserTabSessionRepositoryTest {
    @Test
    fun saveAndRestoreRoundTrip() {
        val store = InMemoryPreferenceStore()
        val repository = BrowserTabSessionRepository(store)

        repository.save(
            tabs = listOf(
                BrowserTab(id = 1L, url = "https://a.example.com", title = "A"),
                BrowserTab(id = 2L, url = "https://b.example.com", title = "B")
            ),
            activeTabId = 2L
        )

        val restored = repository.restore()

        assertEquals(2L, restored?.activeTabId)
        assertEquals(2, restored?.tabs?.size)
        assertEquals("https://b.example.com", restored?.tabs?.last()?.url)
    }

    @Test
    fun saveIgnoresTabsWithoutUrls() {
        val store = InMemoryPreferenceStore()
        val repository = BrowserTabSessionRepository(store)

        repository.save(
            tabs = listOf(BrowserTab(id = 1L), BrowserTab(id = 2L, url = "https://example.com")),
            activeTabId = 1L
        )

        val restored = repository.restore()

        assertEquals(2L, restored?.activeTabId)
        assertEquals(1, restored?.tabs?.size)
        assertEquals("https://example.com", restored?.tabs?.single()?.url)
    }

    @Test
    fun saveIgnoresTabsWithNonWebUrls() {
        val store = InMemoryPreferenceStore()
        val repository = BrowserTabSessionRepository(store)

        repository.save(
            tabs = listOf(
                BrowserTab(id = 1L, url = "javascript:alert(1)", title = "Script"),
                BrowserTab(id = 2L, url = "file:///sdcard/page.html", title = "File"),
                BrowserTab(id = 3L, url = "about:blank", title = "About"),
                BrowserTab(id = 4L, url = "https:/missing-host", title = "Broken"),
                BrowserTab(id = 5L, url = " https://example.com/page ", title = " Example ")
            ),
            activeTabId = 1L
        )

        val restored = repository.restore()

        assertEquals(5L, restored?.activeTabId)
        assertEquals(listOf("https://example.com/page"), restored?.tabs?.map { tab -> tab.url })
        assertEquals("Example", restored?.tabs?.single()?.title)
    }

    @Test
    fun restoreDropsStoredNonWebUrls() {
        val store = InMemoryPreferenceStore()
        val repository = BrowserTabSessionRepository(store)
        store.putString(
            BrowserTabSessionRepository.KEY_STANDARD_TAB_SESSION,
            listOf(
                "1",
                "1\t100\tjavascript%3Aalert%281%29\tScript",
                "2\t100\thttps%3A%2F%2Fexample.com\tExample"
            ).joinToString(separator = "\n")
        )

        val restored = repository.restore()

        assertEquals(2L, restored?.activeTabId)
        assertEquals(listOf("https://example.com"), restored?.tabs?.map { tab -> tab.url })
    }

    @Test
    fun saveClearsEmptySession() {
        val store = InMemoryPreferenceStore()
        val repository = BrowserTabSessionRepository(store)
        store.putString(BrowserTabSessionRepository.KEY_STANDARD_TAB_SESSION, "old")

        repository.save(tabs = listOf(BrowserTab(id = 1L)), activeTabId = 1L)

        assertFalse(store.contains(BrowserTabSessionRepository.KEY_STANDARD_TAB_SESSION))
        assertNull(repository.restore())
    }

    @Test
    fun clearRemovesSavedSession() {
        val store = InMemoryPreferenceStore()
        val repository = BrowserTabSessionRepository(store)
        repository.save(listOf(BrowserTab(id = 1L, url = "https://example.com")), activeTabId = 1L)

        repository.clear()

        assertFalse(store.contains(BrowserTabSessionRepository.KEY_STANDARD_TAB_SESSION))
    }

    private class InMemoryPreferenceStore : PreferenceStore {
        private val values = mutableMapOf<String, Any>()

        override fun contains(key: String): Boolean {
            return values.containsKey(key)
        }

        override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
            return values[key] as? Boolean ?: defaultValue
        }

        override fun putBoolean(key: String, value: Boolean) {
            values[key] = value
        }

        override fun getFloat(key: String, defaultValue: Float): Float {
            return values[key] as? Float ?: defaultValue
        }

        override fun putFloat(key: String, value: Float) {
            values[key] = value
        }

        override fun getString(key: String, defaultValue: String?): String? {
            return values[key] as? String ?: defaultValue
        }

        override fun putString(key: String, value: String) {
            values[key] = value
        }

        override fun remove(key: String) {
            values.remove(key)
        }

        override fun remove(keys: Iterable<String>, commit: Boolean): Boolean {
            keys.forEach { key -> values.remove(key) }
            return true
        }
    }
}
