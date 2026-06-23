package com.example.videobrowser.settings

import com.example.videobrowser.testutil.projectFile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PersistentSitePermissionStoreContractTest {
    @Test
    fun sitePermissionHostStorageKeysAreSharedByPermission() {
        val source = projectFile(
            "src/main/java/com/example/videobrowser/settings/PersistentSitePermissionStore.kt"
        ).readText()

        assertTrue(source.contains("private data class SitePermissionHostKeys("))
        assertTrue(source.contains("private val hostKeysByPermission"))
        assertTrue(source.contains("private fun hostKeys(permission: SitePermission)"))
        assertFalse(source.contains("private fun allowedKey("))
        assertFalse(source.contains("private fun blockedKey("))
        assertEquals(1, Regex("SitePermission\\.CAMERA").findAll(source).count())
        assertEquals(1, Regex("SitePermission\\.MICROPHONE").findAll(source).count())
        assertEquals(1, Regex("SitePermission\\.LOCATION").findAll(source).count())
    }
}
