package com.example.videobrowser.browser

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BottomBarButtonVisibilityTest {
    @Test
    fun homePage_hidesNavigationToolsThatAreOnlyUsefulAfterOpeningPage() {
        val visibility = BottomBarButtonVisibility.forPageState(isHomePageVisible = true)

        assertFalse(visibility.showBack)
        assertFalse(visibility.showPageTools)
        assertFalse(visibility.showTabsHome)
        assertTrue(visibility.showWenxin)
        assertTrue(visibility.showProfile)
    }

    @Test
    fun browsingPage_showsNavigationToolsAgain() {
        val visibility = BottomBarButtonVisibility.forPageState(isHomePageVisible = false)

        assertTrue(visibility.showBack)
        assertTrue(visibility.showPageTools)
        assertTrue(visibility.showTabsHome)
        assertTrue(visibility.showWenxin)
        assertTrue(visibility.showProfile)
    }
}
