package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器核心模块”。
 * 文件名 BrowserErrorPage 可以拆开理解为“Browser Error Page”，表示它只负责浏览器流程中的一个小职责。
 * 主要职责：封装 WebView 页面加载、标签页、导航安全、页面工具、权限回调或浏览器控件状态。
 * 阅读顺序：先看构造参数知道它依赖谁，再看公开函数知道外部如何调用，最后看 private 函数了解内部细节。
 */
sealed class BrowserPageError(
    open val url: String?
) {
    data class Network(
        override val url: String?,
        val code: Int,
        val description: String
    ) : BrowserPageError(url)

    data class Http(
        override val url: String?,
        val statusCode: Int,
        val reasonPhrase: String
    ) : BrowserPageError(url)

    data class Ssl(
        override val url: String?,
        val description: String
    ) : BrowserPageError(url)

    data class SafeBrowsing(
        override val url: String?,
        val threatType: Int,
        val description: String
    ) : BrowserPageError(url)

    data class RenderProcessGone(
        override val url: String?,
        val didCrash: Boolean
    ) : BrowserPageError(url)
}

object BrowserErrorPage {
    fun render(error: BrowserPageError): String {
        val title = when (error) {
            is BrowserPageError.SafeBrowsing -> "连接已被阻止"
            is BrowserPageError.Ssl -> "连接已被阻止"
            is BrowserPageError.RenderProcessGone -> "网页已崩溃"
            else -> "网页无法打开"
        }
        val detail = when (error) {
            is BrowserPageError.Network -> "${error.description} (${error.code})"
            is BrowserPageError.Http -> "HTTP ${error.statusCode} ${error.reasonPhrase}".trim()
            is BrowserPageError.Ssl -> error.description
            is BrowserPageError.SafeBrowsing -> error.description
            is BrowserPageError.RenderProcessGone -> if (error.didCrash) {
                "网页渲染进程已崩溃，浏览器已重新创建页面。"
            } else {
                "网页渲染进程已退出，浏览器已重新创建页面。"
            }
        }
        val url = error.url.orEmpty()
        val retryAction = if (error is BrowserPageError.SafeBrowsing) {
            ""
        } else {
            retryableUrl(error.url)
                ?.let { retryUrl ->
                    """<a class="button" href="${retryUrl.escapeHtml()}">重试</a>"""
                }
                .orEmpty()
        }
        return """
            <!doctype html>
            <html>
            <head>
              <meta charset="utf-8">
              <meta name="viewport" content="width=device-width, initial-scale=1">
              <title>${title.escapeHtml()}</title>
              <style>
                body {
                  margin: 0;
                  padding: 32px 24px;
                  background: #f7f8fb;
                  color: #20242c;
                  font-family: sans-serif;
                }
                main {
                  max-width: 680px;
                  margin: 0 auto;
                }
                h1 {
                  margin: 0 0 12px;
                  font-size: 24px;
                  font-weight: 700;
                }
                p {
                  margin: 8px 0;
                  font-size: 15px;
                  line-height: 1.55;
                  color: #4e5664;
                  word-break: break-word;
                }
                .url {
                  margin-top: 18px;
                  padding: 12px;
                  border-radius: 8px;
                  background: #eef1f6;
                  color: #1f2937;
                }
                .actions {
                  margin-top: 22px;
                }
                .button {
                  display: inline-block;
                  padding: 10px 18px;
                  border-radius: 8px;
                  background: #1769e0;
                  color: white;
                  font-size: 15px;
                  font-weight: 700;
                  text-decoration: none;
                }
              </style>
            </head>
            <body>
              <main>
                <h1>${title.escapeHtml()}</h1>
                <p>${detail.escapeHtml()}</p>
                <p class="url">${url.escapeHtml()}</p>
                <div class="actions">$retryAction</div>
              </main>
            </body>
            </html>
        """.trimIndent()
    }

    private fun retryableUrl(url: String?): String? {
        val normalizedUrl = url?.trim()?.takeIf { it.isNotBlank() } ?: return null
        return normalizedUrl.takeIf {
            it.startsWith("http://", ignoreCase = true) ||
                it.startsWith("https://", ignoreCase = true)
        }
    }

    private fun String.escapeHtml(): String {
        return buildString(length) {
            this@escapeHtml.forEach { char ->
                when (char) {
                    '&' -> append("&amp;")
                    '<' -> append("&lt;")
                    '>' -> append("&gt;")
                    '"' -> append("&quot;")
                    '\'' -> append("&#39;")
                    else -> append(char)
                }
            }
        }
    }
}
