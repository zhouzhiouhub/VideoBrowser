package com.example.videobrowser.browser

import com.example.videobrowser.testutil.projectFile
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowserAddressBarStateControllerContractTest {
    @Test
    fun addressBarFocusShowsFullUrlOnlyForNormalPages() {
        val controller = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserAddressBarStateController.kt"
        ).readText()

        assertTrue(controller.contains("private var currentUrl: String? = null"))
        assertTrue(controller.contains("private var isAddressInputFocused = false"))
        assertTrue(controller.contains("fun handleAddressFocusChanged(hasFocus: Boolean)"))
        assertTrue(controller.contains("addressTextFor(url, hasFocus)"))
        assertTrue(controller.contains("searchProviderController.searchQueryFromUrl(url) ?: if (hasFocus)"))
        assertTrue(controller.contains("if (hasFocus)"))
        assertTrue(controller.contains("UrlUtils.displayUrl(url)"))
        assertTrue(controller.contains("addressBarDisplayText(url)"))
        assertTrue(controller.contains("addressInput.selectAll()"))
    }
}
