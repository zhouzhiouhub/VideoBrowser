package com.example.videobrowser.settings

/**
 * 测试阅读提示：
 * 这个测试文件验证“Session Site Permission Store Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionSitePermissionStoreTest {
    @Test
    fun allow_normalizesHostForCurrentSessionOnly() {
        val store = SessionSitePermissionStore()

        assertTrue(store.allow(" Camera.Example.Com. ", SitePermission.CAMERA))

        assertTrue(store.isAllowed("camera.example.com", SitePermission.CAMERA))
        assertFalse(store.isAllowed("camera.example.com", SitePermission.MICROPHONE))
        assertFalse(store.isAllowed("other.example.com", SitePermission.CAMERA))
    }

    @Test
    fun clear_removesSessionGrants() {
        val store = SessionSitePermissionStore()
        store.allow("maps.example.com", SitePermission.LOCATION)

        store.clear()

        assertFalse(store.isAllowed("maps.example.com", SitePermission.LOCATION))
    }

    @Test
    fun allow_rejectsMissingHost() {
        val store = SessionSitePermissionStore()

        assertFalse(store.allow(" ", SitePermission.CAMERA))
        assertFalse(store.isAllowed(" ", SitePermission.CAMERA))
    }
}
