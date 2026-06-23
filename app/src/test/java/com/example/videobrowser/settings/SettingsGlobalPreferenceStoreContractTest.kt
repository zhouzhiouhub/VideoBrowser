package com.example.videobrowser.settings

import com.example.videobrowser.testutil.projectFile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsGlobalPreferenceStoreContractTest {
    @Test
    fun booleanPreferenceAccessUsesSharedReaderAndWriter() {
        val source = projectFile(
            "src/main/java/com/example/videobrowser/settings/SettingsGlobalPreferenceStore.kt"
        ).readText()

        assertTrue(source.contains("private data class BooleanPreference("))
        assertTrue(source.contains("private fun getBooleanPreference(preference: BooleanPreference): Boolean"))
        assertTrue(source.contains("private fun setBooleanPreference(preference: BooleanPreference, value: Boolean)"))
        assertEquals(9, Regex("= BooleanPreference\\(").findAll(source).count())
        assertEquals(1, Regex("preferenceStore\\.getBoolean\\(").findAll(source).count())
        assertEquals(1, Regex("preferenceStore\\.putBoolean\\(").findAll(source).count())
    }

    @Test
    fun floatPreferenceAccessUsesSharedReaderAndWriter() {
        val source = projectFile(
            "src/main/java/com/example/videobrowser/settings/SettingsGlobalPreferenceStore.kt"
        ).readText()

        assertTrue(source.contains("private data class FloatPreference("))
        assertTrue(source.contains("private fun getFloatPreference(preference: FloatPreference): Float"))
        assertTrue(source.contains("private fun setFloatPreference(preference: FloatPreference, value: Float)"))
        assertEquals(2, Regex("= FloatPreference\\(").findAll(source).count())
        assertEquals(1, Regex("preferenceStore\\.getFloat\\(").findAll(source).count())
        assertEquals(1, Regex("preferenceStore\\.putFloat\\(").findAll(source).count())
    }

    @Test
    fun stringPreferenceAccessUsesSharedReaderAndWriter() {
        val source = projectFile(
            "src/main/java/com/example/videobrowser/settings/SettingsGlobalPreferenceStore.kt"
        ).readText()

        assertTrue(source.contains("private data class StringPreference("))
        assertTrue(source.contains("private fun getStringPreference(preference: StringPreference): String?"))
        assertTrue(source.contains("private fun setStringPreference(preference: StringPreference, value: String)"))
        assertEquals(2, Regex("= StringPreference\\(").findAll(source).count())
        assertEquals(1, Regex("preferenceStore\\.getString\\(").findAll(source).count())
        assertEquals(1, Regex("preferenceStore\\.putString\\(").findAll(source).count())
    }
}
