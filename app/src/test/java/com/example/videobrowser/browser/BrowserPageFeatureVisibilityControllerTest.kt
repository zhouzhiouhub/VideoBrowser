package com.example.videobrowser.browser

import org.junit.Assert.assertEquals
import org.junit.Test

class BrowserPageFeatureVisibilityControllerTest {
    @Test
    fun handlePageStarted_hidesMatchedPageUntilFeaturesInjected() {
        val alphaChanges = mutableListOf<Float>()
        val controller = controllerFor(alphaChanges)

        controller.handlePageStarted("https://m.baidu.com/s?word=%E4%BD%A0%E5%A5%BD")
        controller.handlePageFeaturesInjected("https://m.baidu.com/s?word=%E4%BD%A0%E5%A5%BD")

        assertEquals(listOf(0f, 1f), alphaChanges)
    }

    @Test
    fun handlePageStarted_revealsNormalPagesAndFailures() {
        val alphaChanges = mutableListOf<Float>()
        val controller = controllerFor(alphaChanges)

        controller.handlePageStarted("https://m.baidu.com/s?word=%E4%BD%A0%E5%A5%BD")
        controller.handlePageFailed("https://m.baidu.com/s?word=%E4%BD%A0%E5%A5%BD")
        controller.handlePageStarted("https://example.com/")

        assertEquals(listOf(0f, 1f, 1f), alphaChanges)
    }

    @Test
    fun handlePageFeaturesInjected_ignoresEarlierHiddenPageAfterNewHiddenPageStarts() {
        val alphaChanges = mutableListOf<Float>()
        val controller = controllerFor(alphaChanges)

        controller.handlePageStarted("https://m.baidu.com/s?word=first")
        controller.handlePageStarted("https://m.baidu.com/s?word=second")
        controller.handlePageFeaturesInjected("https://m.baidu.com/s?word=first")
        controller.handlePageFeaturesInjected("https://m.baidu.com/s?word=second")

        assertEquals(listOf(0f, 0f, 1f), alphaChanges)
    }

    private fun controllerFor(alphaChanges: MutableList<Float>): BrowserPageFeatureVisibilityController {
        return BrowserPageFeatureVisibilityController(
            setActiveWebViewAlpha = { alpha -> alphaChanges += alpha },
            shouldHideUntilPageFeaturesInjected = { url ->
                url?.startsWith("https://m.baidu.com/s?word=") == true
            }
        )
    }
}
