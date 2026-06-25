package com.example.videobrowser.browser.search

import java.io.ByteArrayInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchEngineTemplateProberTest {
    @Test
    fun probe_prefersOpenSearchDescriptionTemplates() {
        val requested = mutableListOf<Pair<String, Int>>()
        val prober = SearchEngineTemplateProber { url, maxBytes ->
            requested += url to maxBytes
            when (url) {
                "https://example.com" -> """
                    <html>
                      <link rel="search" type="application/opensearchdescription+xml" href="/osd.xml">
                    </html>
                """.trimIndent()

                "https://example.com/osd.xml" -> """
                    <OpenSearchDescription>
                      <ShortName>Example Search</ShortName>
                      <Url type="application/json" template="https://example.com/api?q={searchTerms}" />
                      <Url type="text/html" template="https://example.com/search?q={searchTerms}" />
                    </OpenSearchDescription>
                """.trimIndent()

                else -> null
            }
        }

        val result = prober.probe("https://example.com", name = "Ignored")

        assertEquals(SearchEngineTemplateProbeSource.OPENSEARCH, result?.source)
        assertEquals("Example Search", result?.config?.name)
        assertEquals("https://example.com", result?.config?.displayUrl)
        assertEquals("https://example.com/search?q={keyword}", result?.config?.searchTemplate)
        assertEquals("q", result?.config?.queryParam)
        assertEquals(listOf("example.com"), result?.config?.domains)
        assertEquals(listOf("/search"), result?.config?.resultPathRules)
        assertEquals(
            listOf(
                "https://example.com" to SearchEngineTemplateProber.MAX_HOME_HTML_BYTES,
                "https://example.com/osd.xml" to SearchEngineTemplateProber.MAX_OPENSEARCH_XML_BYTES
            ),
            requested
        )
    }

    @Test
    fun probe_fallsBackToGetSearchForms() {
        val prober = SearchEngineTemplateProber { url, _ ->
            if (url == "https://example.com") {
                """
                    <html>
                      <form action="/find">
                        <input type="text" name="keyword">
                        <button>Search</button>
                      </form>
                    </html>
                """.trimIndent()
            } else {
                null
            }
        }

        val result = prober.probe("https://example.com", name = "Example")

        assertEquals(SearchEngineTemplateProbeSource.FORM, result?.source)
        assertEquals("Example", result?.config?.name)
        assertEquals("https://example.com/find?keyword={keyword}", result?.config?.searchTemplate)
        assertEquals("keyword", result?.config?.queryParam)
        assertEquals(listOf("/find"), result?.config?.resultPathRules)
    }

    @Test
    fun probe_rejectsInvalidUrlsPostFormsAndUnknownForms() {
        var fetchCount = 0
        val prober = SearchEngineTemplateProber { _, _ ->
            fetchCount += 1
            """
                <form method="post" action="/search">
                  <input name="q">
                </form>
            """.trimIndent()
        }

        assertNull(prober.probe("javascript:alert(1)"))
        assertEquals(0, fetchCount)
        assertNull(prober.probe("https://example.com"))
        assertEquals(1, fetchCount)
    }

    @Test
    fun fetcher_configuresConnectionAndReadsLimitedUtf8() {
        lateinit var connection: FakeHttpURLConnection
        val fetcher = SearchEngineProbeFetcher(
            networkTimeoutMs = 1234,
            openConnection = { url ->
                connection = FakeHttpURLConnection(
                    url = url,
                    body = "你好".toByteArray(Charsets.UTF_8)
                )
                connection
            }
        )

        assertEquals("你好", fetcher.fetchText("https://example.com", maxBytes = 16))
        assertEquals(1234, connection.connectTimeout)
        assertEquals(1234, connection.readTimeout)
        assertEquals("GET", connection.requestMethod)
        assertEquals("text/html,application/xhtml+xml,application/xml,text/xml", connection.acceptHeader)
        assertTrue(connection.instanceFollowRedirects)
        assertTrue(connection.disconnected)
    }

    @Test
    fun fetcher_rejectsOversizedResponses() {
        val fetcher = SearchEngineProbeFetcher(
            openConnection = { url ->
                FakeHttpURLConnection(
                    url = url,
                    body = "abcd".toByteArray(Charsets.UTF_8),
                    contentLength = -1
                )
            }
        )

        assertNull(fetcher.fetchText("https://example.com", maxBytes = 3))
    }

    @Test
    fun parserOwnershipIsSharedWithQuickLinkLogoResolver() {
        val resolverSource = com.example.videobrowser.testutil.projectFile(
            "src/main/java/com/example/videobrowser/browser/search/QuickLinkLogoResolver.kt"
        ).readText()
        val openSearchSource = com.example.videobrowser.testutil.projectFile(
            "src/main/java/com/example/videobrowser/browser/search/OpenSearchDescriptionResolver.kt"
        ).readText()
        val formSource = com.example.videobrowser.testutil.projectFile(
            "src/main/java/com/example/videobrowser/browser/search/SearchFormTemplateAnalyzer.kt"
        ).readText()

        assertTrue(resolverSource.contains("HtmlTagAttributeParser.tags(html, \"link\")"))
        assertTrue(openSearchSource.contains("HtmlTagAttributeParser.tags(html, \"link\")"))
        assertTrue(formSource.contains("HtmlTagAttributeParser.tags(formHtml, \"input\")"))
        assertFalse(openSearchSource.contains("Regex(\"<link"))
    }

    private class FakeHttpURLConnection(
        url: URL,
        private val statusCode: Int = 200,
        private val body: ByteArray = ByteArray(0),
        private val contentLength: Long = body.size.toLong()
    ) : HttpURLConnection(url) {
        var disconnected = false
            private set
        val acceptHeader: String?
            get() = requestProperties["Accept"]?.singleOrNull()

        override fun disconnect() {
            disconnected = true
        }

        override fun usingProxy(): Boolean {
            return false
        }

        override fun connect() {
            connected = true
        }

        override fun getResponseCode(): Int {
            return statusCode
        }

        override fun getInputStream(): InputStream {
            return ByteArrayInputStream(body)
        }

        override fun getContentLengthLong(): Long {
            return contentLength
        }
    }
}
