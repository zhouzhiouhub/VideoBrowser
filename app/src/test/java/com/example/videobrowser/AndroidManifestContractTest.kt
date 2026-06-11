package com.example.videobrowser

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element

class AndroidManifestContractTest {
    @Test
    fun mainActivityCanHandleExternalWebLinks() {
        val activity = manifest()
            .elements("activity")
            .first { it.androidAttribute("name") == ".MainActivity" }

        val webLinkFilter = activity
            .elements("intent-filter")
            .firstOrNull { filter ->
                filter.hasAction("android.intent.action.VIEW") &&
                    filter.hasCategory("android.intent.category.DEFAULT") &&
                    filter.hasCategory("android.intent.category.BROWSABLE")
            }

        assertTrue(
            "MainActivity should expose a VIEW/DEFAULT/BROWSABLE intent-filter for web links",
            webLinkFilter != null
        )
        val schemes = webLinkFilter!!.elements("data")
            .map { data -> data.androidAttribute("scheme") }
            .toSet()
        assertTrue("MainActivity should handle http links", "http" in schemes)
        assertTrue("MainActivity should handle https links", "https" in schemes)
    }

    @Test
    fun mainActivityLoadsExternalWebLinkIntents() {
        val mainActivity = projectFile(
            "src/main/java/com/example/videobrowser/MainActivity.kt"
        ).readText()

        assertTrue(mainActivity.contains("handleLaunchIntent(intent)"))
        assertTrue(mainActivity.contains("override fun onNewIntent(intent: Intent)"))
        assertTrue(mainActivity.contains("Intent.ACTION_VIEW"))
        assertTrue(mainActivity.contains("intent.dataString"))
    }

    private fun manifest(): Element {
        return DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = true
        }.newDocumentBuilder()
            .parse(projectFile("src/main/AndroidManifest.xml"))
            .documentElement
    }

    private fun Element.hasAction(name: String): Boolean {
        return elements("action").any { action -> action.androidAttribute("name") == name }
    }

    private fun Element.hasCategory(name: String): Boolean {
        return elements("category").any { category -> category.androidAttribute("name") == name }
    }

    private fun Element.elements(tagName: String): List<Element> {
        val nodes = getElementsByTagName(tagName)
        return List(nodes.length) { index -> nodes.item(index) }
            .filterIsInstance<Element>()
    }

    private fun Element.androidAttribute(name: String): String {
        return getAttributeNS(ANDROID_NAMESPACE, name)
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }

    private companion object {
        private const val ANDROID_NAMESPACE = "http://schemas.android.com/apk/res/android"
    }
}
