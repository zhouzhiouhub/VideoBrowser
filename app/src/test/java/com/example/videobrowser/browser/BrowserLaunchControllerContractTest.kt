package com.example.videobrowser.browser

import com.example.videobrowser.testutil.projectFile
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowserLaunchControllerContractTest {
    @Test
    fun homePageEntryShowsAppHomeInsteadOfLoadingSearchProviderHomeUrl() {
        val launchController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserLaunchController.kt"
        ).readText()
        val navigationAssembly = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserNavigationAssemblyController.kt"
        ).readText()

        assertTrue(launchController.contains("showHomePage: () -> Unit"))
        assertTrue(openHomePageBody(launchController).contains("showHomePage()"))
        assertTrue(openHomePageBody(launchController).contains("runWithSuggestionsSuppressed"))
        assertFalse(launchController.contains("homeUrl: () -> String"))
        assertFalse(launchController.contains("loadUrl(homeUrl())"))

        assertTrue(navigationAssembly.contains("showHomePage = {"))
        assertTrue(navigationAssembly.contains("closeFunctionCenter()"))
        assertTrue(navigationAssembly.contains("browserKeyboardController.hideKeyboard()"))
        assertTrue(
            navigationAssembly.contains(
                "browserSessionStateController.currentSessionController().reset()"
            )
        )
    }

    @Test
    fun initialStandardPageUsesAppHomeWhenThereIsNoRestoredTabUrl() {
        val launchController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserLaunchController.kt"
        ).readText()
        val initialPageBody = functionBody(launchController, "fun openInitialStandardPage()")

        assertTrue(initialPageBody.contains("val restoredUrl = activeStandardTabUrl()"))
        assertTrue(initialPageBody.contains("if (restoredUrl.isNullOrBlank())"))
        assertTrue(initialPageBody.contains("openHomePage()"))
        assertTrue(initialPageBody.contains("loadUrl(restoredUrl)"))
        assertFalse(initialPageBody.contains("homeUrl"))
    }

    @Test
    fun loadedUrlsAlwaysEnterWebContentState() {
        val navigationController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserNavigationController.kt"
        ).readText()
        val sessionController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserSessionController.kt"
        ).readText()
        val shellUiController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserShellUiController.kt"
        ).readText()
        val addressBarStateController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserAddressBarStateController.kt"
        ).readText()

        assertTrue(navigationController.contains("showHomeContent(false)"))
        assertFalse(navigationController.contains("isProviderHomeUrl"))
        assertFalse(sessionController.contains("isProviderHomeUrl"))
        assertTrue(sessionController.contains("isHomePageVisible = false"))
        assertTrue(sessionController.contains("isHomePageVisible = url.isNullOrBlank()"))
        assertTrue(sessionController.contains("isHomePageVisible = true"))
        assertTrue(shellUiController.contains("activeWebView().visibility = if (show) View.GONE else View.VISIBLE"))
        assertTrue(addressBarStateController.contains("addressInput.setText(\"\")"))
    }

    @Test
    fun homePageShowsOnlySearchChromeWithoutUrlText() {
        val shellUiController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserShellUiController.kt"
        ).readText()
        val shellAssembly = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserShellAssemblyController.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()

        assertTrue(shellAssembly.contains("rootView = views.rootView"))
        assertTrue(shellAssembly.contains("topBar = views.topBar"))
        assertTrue(shellAssembly.contains("bottomBar = views.bottomBar"))
        assertTrue(shellUiController.contains("bottomBar.visibility = if (show) View.GONE else View.VISIBLE"))
        assertTrue(shellUiController.contains("setVerticalBias(topBar.id, HOME_SEARCH_VERTICAL_BIAS)"))
        assertTrue(shellUiController.contains("topBar.setBackgroundColor(Color.TRANSPARENT)"))
        assertTrue(strings.contains("<string name=\"hint_address_bar\">搜索</string>"))
        assertFalse(strings.contains("输入网址或搜索"))
    }

    private fun openHomePageBody(source: String): String {
        return functionBody(source, "fun openHomePage()")
    }

    private fun functionBody(source: String, signature: String): String {
        val start = source.indexOf(signature)
        assertTrue(start >= 0)
        val bodyStart = source.indexOf('{', start)
        assertTrue(bodyStart >= 0)
        var depth = 0
        for (index in bodyStart until source.length) {
            when (source[index]) {
                '{' -> depth += 1
                '}' -> {
                    depth -= 1
                    if (depth == 0) {
                        return source.substring(bodyStart, index + 1)
                    }
                }
            }
        }
        error("Unclosed body for $signature")
    }
}
