package com.example.videobrowser.utils

/**
 * 测试阅读提示：
 * 这个测试文件验证“Url Utils Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UrlUtilsTest {
    private val searchUrlPrefix = "https://m.baidu.com/s?ie=utf-8&word="

    /**
     * 测试函数 `resolveAddressInput_keepsHttpAndHttpsUrls`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `resolve Address Input keeps Http And Https Urls` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun resolveAddressInput_keepsHttpAndHttpsUrls() {
        assertEquals(
            "http://example.com/path",
            UrlUtils.resolveAddressInput("http://example.com/path", searchUrlPrefix)
        )
        assertEquals(
            "https://example.com/path",
            UrlUtils.resolveAddressInput("  https://example.com/path  ", searchUrlPrefix)
        )
    }

    /**
     * 测试函数 `resolveAddressInput_normalizesUrlHosts`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `resolve Address Input normalizes Url Hosts` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun resolveAddressInput_normalizesUrlHosts() {
        assertEquals(
            "https://example.com/path",
            UrlUtils.resolveAddressInput("HTTPS://Example.COM./path", searchUrlPrefix)
        )
        assertEquals(
            "https://xn--fsqu00a.xn--0zwm56d/path",
            UrlUtils.resolveAddressInput("https://例子.测试/path", searchUrlPrefix)
        )
    }

    /**
     * 测试函数 `resolveAddressInput_convertsDomainToHttpsUrl`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `resolve Address Input converts Domain To Https Url` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun resolveAddressInput_convertsDomainToHttpsUrl() {
        assertEquals(
            "https://example.com",
            UrlUtils.resolveAddressInput("example.com", searchUrlPrefix)
        )
        assertEquals(
            "https://example.com/video?q=1",
            UrlUtils.resolveAddressInput("example.com/video?q=1", searchUrlPrefix)
        )
        assertEquals(
            "https://example.com:8443/video",
            UrlUtils.resolveAddressInput("example.com:8443/video", searchUrlPrefix)
        )
        assertEquals(
            "https://example.com/path%20with%20space?q=hello%20world",
            UrlUtils.resolveAddressInput(
                "example.com/path with space?q=hello world",
                searchUrlPrefix
            )
        )
    }

    /**
     * 测试函数 `resolveAddressInput_convertsSchemeRelativeDomainToHttpsUrl`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `resolve Address Input converts Scheme Relative Domain To Https Url` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun resolveAddressInput_convertsSchemeRelativeDomainToHttpsUrl() {
        assertEquals(
            "https://example.com/player",
            UrlUtils.resolveAddressInput("//example.com/player", searchUrlPrefix)
        )
    }

    /**
     * 测试函数 `resolveAddressInput_convertsLocalAndIpAddressesToHttpUrl`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `resolve Address Input converts Local And Ip Addresses To Http Url` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun resolveAddressInput_convertsLocalAndIpAddressesToHttpUrl() {
        assertEquals(
            "http://localhost:3000/watch",
            UrlUtils.resolveAddressInput("localhost:3000/watch", searchUrlPrefix)
        )
        assertEquals(
            "http://10.0.2.2:8080",
            UrlUtils.resolveAddressInput("10.0.2.2:8080", searchUrlPrefix)
        )
        assertEquals(
            "http://127.0.0.1/test",
            UrlUtils.resolveAddressInput("127.0.0.1/test", searchUrlPrefix)
        )
    }

    /**
     * 测试函数 `resolveAddressInput_handlesIpv6Urls`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `resolve Address Input handles Ipv6 Urls` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun resolveAddressInput_handlesIpv6Urls() {
        assertEquals(
            "http://[::1]:8080/test",
            UrlUtils.resolveAddressInput("[::1]:8080/test", searchUrlPrefix)
        )
        assertEquals(
            "https://[2001:db8::1]/video",
            UrlUtils.resolveAddressInput("https://[2001:db8::1]/video", searchUrlPrefix)
        )
        assertEquals(
            "http://[::1]",
            UrlUtils.resolveAddressInput("::1", searchUrlPrefix)
        )
    }

    /**
     * 测试函数 `resolveAddressInput_convertsTextToSearchUrl`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `resolve Address Input converts Text To Search Url` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun resolveAddressInput_convertsTextToSearchUrl() {
        assertEquals(
            "https://m.baidu.com/s?ie=utf-8&word=hello+world",
            UrlUtils.resolveAddressInput("hello world", searchUrlPrefix)
        )
        assertEquals(
            "https://m.baidu.com/s?ie=utf-8&word=user%40example.com",
            UrlUtils.resolveAddressInput("user@example.com", searchUrlPrefix)
        )
        assertEquals(
            "https://m.baidu.com/s?ie=utf-8&word=256.1.1.1",
            UrlUtils.resolveAddressInput("256.1.1.1", searchUrlPrefix)
        )
        assertEquals(
            "https://m.baidu.com/s?ie=utf-8&word=ftp%3A%2F%2Fexample.com",
            UrlUtils.resolveAddressInput("ftp://example.com", searchUrlPrefix)
        )
        assertEquals(
            "https://m.baidu.com/s?ie=utf-8&word=.example.com",
            UrlUtils.resolveAddressInput(".example.com", searchUrlPrefix)
        )
        assertEquals(
            "https://m.baidu.com/s?ie=utf-8&word=https%3A%2F%2Fexa+mple.com",
            UrlUtils.resolveAddressInput("https://exa mple.com", searchUrlPrefix)
        )
    }

    /**
     * 测试函数 `resolveAddressInput_encodesChineseSearchTextAsUtf8`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `resolve Address Input encodes Chinese Search Text As Utf8` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun resolveAddressInput_encodesChineseSearchTextAsUtf8() {
        assertEquals(
            "https://m.baidu.com/s?ie=utf-8&word=%E5%A4%9A%E5%B0%91%E9%92%B1",
            UrlUtils.resolveAddressInput("多少钱", searchUrlPrefix)
        )
        assertEquals(
            "https://m.baidu.com/s?ie=utf-8&word=" +
                "%E6%B7%B1%E5%9C%B3%E8%81%8C%E4%B8%9A%E6%8A%80%E6%9C%AF" +
                "%E5%AD%A6%E9%99%A2%E5%AD%A6%E8%B4%B9%E4%B8%80%E5%B9%B4" +
                "%E5%A4%9A%E5%B0%91%E9%92%B1",
            UrlUtils.resolveAddressInput("深圳职业技术学院学费一年多少钱", searchUrlPrefix)
        )
    }

    /**
     * 测试函数 `searchQueryFromUrl_decodesChineseSearchText`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `search Query From Url decodes Chinese Search Text` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun searchQueryFromUrl_decodesChineseSearchText() {
        assertEquals(
            "多少钱",
            UrlUtils.searchQueryFromUrl(
                "https://m.baidu.com/s?ie=utf-8&word=%E5%A4%9A%E5%B0%91%E9%92%B1",
                searchUrlPrefix
            )
        )
        assertEquals(
            "深圳职业技术学院学费一年多少钱",
            UrlUtils.searchQueryFromUrl(
                "https://m.baidu.com/s?ie=utf-8&word=" +
                    "%E6%B7%B1%E5%9C%B3%E8%81%8C%E4%B8%9A%E6%8A%80%E6%9C%AF" +
                    "%E5%AD%A6%E9%99%A2%E5%AD%A6%E8%B4%B9%E4%B8%80%E5%B9%B4" +
                    "%E5%A4%9A%E5%B0%91%E9%92%B1",
                searchUrlPrefix
            )
        )
    }

    /**
     * 测试函数 `searchQueryFromUrl_decodesSearchTextWithSpacesAndExtraParameters`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `search Query From Url decodes Search Text With Spaces And Extra Parameters` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun searchQueryFromUrl_decodesSearchTextWithSpacesAndExtraParameters() {
        assertEquals(
            "hello world",
            UrlUtils.searchQueryFromUrl(
                "https://m.baidu.com/s?ie=utf-8&word=hello+world&from=app",
                searchUrlPrefix
            )
        )
    }

    @Test
    fun searchQueryFromUrl_decodesSogouMobileResultKeyword() {
        assertEquals(
            "你好1983",
            UrlUtils.searchQueryFromUrl(
                "https://m.sogou.com/web/searchList.jsp?s_from=pcsearch&keyword=你好1983",
                "https://m.sogou.com/web/searchList.jsp?s_from=pcsearch&keyword="
            )
        )
    }

    /**
     * 测试函数 `searchQueryFromUrl_returnsNullForOtherUrls`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `search Query From Url returns Null For Other Urls` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun searchQueryFromUrl_returnsNullForOtherUrls() {
        assertNull(
            UrlUtils.searchQueryFromUrl(
                "https://example.com/s?ie=utf-8&word=%E5%A4%9A%E5%B0%91%E9%92%B1",
                searchUrlPrefix
            )
        )
        assertNull(
            UrlUtils.searchQueryFromUrl(
                "https://m.baidu.com/other?ie=utf-8&word=%E5%A4%9A%E5%B0%91%E9%92%B1",
                searchUrlPrefix
            )
        )
    }

    /**
     * 测试函数 `displayUrl_decodesPercentEncodedUrlForReadability`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `display Url decodes Percent Encoded Url For Readability` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun displayUrl_decodesPercentEncodedUrlForReadability() {
        assertEquals(
            "https://example.com/视频?q=多少钱",
            UrlUtils.displayUrl(
                "https://example.com/%E8%A7%86%E9%A2%91?q=%E5%A4%9A%E5%B0%91%E9%92%B1"
            )
        )
    }

    /**
     * 测试函数 `displayUrl_decodesBaiduLandingContextWithoutChangingLoadUrl`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `display Url decodes Baidu Landing Context Without Changing Load Url` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun displayUrl_decodesBaiduLandingContextWithoutChangingLoadUrl() {
        assertEquals(
            "https://mbd.baidu.com/newspage/data/landingsuper?context={\"nid\":\"news_1\"}",
            UrlUtils.displayUrl(
                "https://mbd.baidu.com/newspage/data/landingsuper?" +
                    "context=%7B%22nid%22%3A%22news_1%22%7D"
            )
        )
    }

    /**
     * 测试函数 `resolveAddressInput_reEncodesDecodedUrlTailUnsafeCharacters`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `resolve Address Input re Encodes Decoded Url Tail Unsafe Characters` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun resolveAddressInput_reEncodesDecodedUrlTailUnsafeCharacters() {
        assertEquals(
            "https://mbd.baidu.com/newspage/data/landingsuper?context=%7B%22nid%22:%22news_1%22%7D",
            UrlUtils.resolveAddressInput(
                "https://mbd.baidu.com/newspage/data/landingsuper?context={\"nid\":\"news_1\"}",
                searchUrlPrefix
            )
        )
    }

    /**
     * 测试函数 `displayUrl_keepsInvalidPercentEscapes`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `display Url keeps Invalid Percent Escapes` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun displayUrl_keepsInvalidPercentEscapes() {
        assertEquals(
            "https://example.com/%zz?q=100%",
            UrlUtils.displayUrl("https://example.com/%zz?q=100%")
        )
    }

    /**
     * 测试函数 `resolveAddressInput_keepsSafeAboutUrls`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `resolve Address Input keeps Safe About Urls` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun resolveAddressInput_keepsSafeAboutUrls() {
        assertEquals(
            "about:blank",
            UrlUtils.resolveAddressInput("about:blank", searchUrlPrefix)
        )
        assertEquals(
            "about:blank",
            UrlUtils.resolveAddressInput("ABOUT:blank", searchUrlPrefix)
        )
        assertEquals(
            "https://m.baidu.com/s?ie=utf-8&word=about%3A+blank",
            UrlUtils.resolveAddressInput("about: blank", searchUrlPrefix)
        )
        assertEquals(
            "https://m.baidu.com/s?ie=utf-8&word=about%3Aversion",
            UrlUtils.resolveAddressInput("about:version", searchUrlPrefix)
        )
    }

    /**
     * 测试函数 `resolveAddressInput_returnsNullForBlankInput`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `resolve Address Input returns Null For Blank Input` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun resolveAddressInput_returnsNullForBlankInput() {
        assertNull(UrlUtils.resolveAddressInput("", searchUrlPrefix))
        assertNull(UrlUtils.resolveAddressInput("   ", searchUrlPrefix))
    }
}
