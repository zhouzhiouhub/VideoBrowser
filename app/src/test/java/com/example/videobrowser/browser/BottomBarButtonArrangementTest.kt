package com.example.videobrowser.browser

import org.junit.Assert.assertEquals
import org.junit.Test

class BottomBarButtonArrangementTest {
    @Test
    fun homePage_centersPrimaryActionsInSeparateHalves() {
        val visibility = BottomBarButtonVisibility.forPageState(isHomePageVisible = true)

        assertEquals(BottomBarButtonArrangement.HomeSplit, BottomBarButtonArrangement.forVisibility(visibility))
    }

    @Test
    fun browsingPage_evenlySpacesVisibleActionsAcrossTheFullBar() {
        val visibility = BottomBarButtonVisibility.forPageState(isHomePageVisible = false)

        assertEquals(BottomBarButtonArrangement.BrowsingEvenlySpaced, BottomBarButtonArrangement.forVisibility(visibility))
    }
}
