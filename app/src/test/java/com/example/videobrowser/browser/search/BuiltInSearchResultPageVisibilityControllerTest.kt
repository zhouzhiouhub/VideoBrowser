package com.example.videobrowser.browser.search

import org.junit.Assert.assertEquals
import org.junit.Test

class BuiltInSearchResultPageVisibilityControllerTest {
    @Test
    fun handlePageStarted_hidesBuiltInSearchResultUntilFeaturesInjected() {
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
    fun handlePageFeaturesInjected_ignoresEarlierSearchResultAfterNewSearchStarts() {
        val alphaChanges = mutableListOf<Float>()
        val controller = controllerFor(alphaChanges)

        controller.handlePageStarted("https://m.baidu.com/s?word=first")
        controller.handlePageStarted("https://m.baidu.com/s?word=second")
        controller.handlePageFeaturesInjected("https://m.baidu.com/s?word=first")
        controller.handlePageFeaturesInjected("https://m.baidu.com/s?word=second")

        assertEquals(listOf(0f, 0f, 1f), alphaChanges)
    }

    private fun controllerFor(alphaChanges: MutableList<Float>): BuiltInSearchResultPageVisibilityController {
        return BuiltInSearchResultPageVisibilityController(
            setActiveWebViewAlpha = { alpha -> alphaChanges += alpha },
            isBuiltInSearchResultUrl = { url ->
                url?.startsWith("https://m.baidu.com/s?word=") == true
            }
        )
    }
}
