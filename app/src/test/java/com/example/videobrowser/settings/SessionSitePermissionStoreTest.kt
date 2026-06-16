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
    /**
     * 测试函数 `allow_normalizesHostForCurrentSessionOnly`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `allow normalizes Host For Current Session Only` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun allow_normalizesHostForCurrentSessionOnly() {
        val store = SessionSitePermissionStore()

        assertTrue(store.allow(" Camera.Example.Com. ", SitePermission.CAMERA))

        assertTrue(store.isAllowed("camera.example.com", SitePermission.CAMERA))
        assertFalse(store.isAllowed("camera.example.com", SitePermission.MICROPHONE))
        assertFalse(store.isAllowed("other.example.com", SitePermission.CAMERA))
    }

    /**
     * 测试函数 `clear_removesSessionGrants`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `clear removes Session Grants` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun clear_removesSessionGrants() {
        val store = SessionSitePermissionStore()
        store.allow("maps.example.com", SitePermission.LOCATION)

        store.clear()

        assertFalse(store.isAllowed("maps.example.com", SitePermission.LOCATION))
    }

    /**
     * 测试函数 `allow_rejectsMissingHost`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `allow rejects Missing Host` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun allow_rejectsMissingHost() {
        val store = SessionSitePermissionStore()

        assertFalse(store.allow(" ", SitePermission.CAMERA))
        assertFalse(store.isAllowed(" ", SitePermission.CAMERA))
    }
}
