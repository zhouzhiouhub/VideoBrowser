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
        val reasonPhrase: String,
        val diagnostics: BrowserHttpErrorDiagnostics = BrowserHttpErrorDiagnostics()
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

data class BrowserHttpErrorDiagnostics(
    val finalUrl: String? = null,
    val currentPageUrl: String? = null,
    val userAgent: String? = null,
    val isSearchResultPage: Boolean = false
)

object BrowserErrorPage {
    /**
     * 函数 `render`：封装 `render` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param error 参数类型为 `BrowserPageError`，表示函数执行 `error` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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
        val explanation = error.explanationHtml()
        val diagnostics = error.diagnosticsHtml()
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
                .diagnostics {
                  margin-top: 18px;
                  padding: 14px;
                  border-radius: 8px;
                  background: #ffffff;
                  border: 1px solid #d9dee8;
                }
                .diagnostics h2 {
                  margin: 0 0 10px;
                  font-size: 16px;
                }
                .diagnostics dl {
                  margin: 0;
                }
                .diagnostics dt {
                  margin-top: 8px;
                  font-size: 13px;
                  font-weight: 700;
                  color: #2f3642;
                }
                .diagnostics dd {
                  margin: 3px 0 0;
                  font-size: 13px;
                  line-height: 1.45;
                  color: #5b6472;
                  word-break: break-word;
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
                $explanation
                $diagnostics
                <p class="url">${url.escapeHtml()}</p>
                <div class="actions">$retryAction</div>
              </main>
            </body>
            </html>
        """.trimIndent()
    }

    private fun BrowserPageError.explanationHtml(): String {
        if (this !is BrowserPageError.Http || statusCode != 401) {
            return ""
        }
        return """
            <p>HTTP 401 表示服务器要求认证或拒绝当前请求上下文，不是地址无法解析。</p>
        """.trimIndent()
    }

    private fun BrowserPageError.diagnosticsHtml(): String {
        if (this !is BrowserPageError.Http) {
            return ""
        }
        val rows = listOfNotNull(
            "状态码" to "HTTP $statusCode",
            diagnosticRow("最终 URL", diagnostics.finalUrl ?: url),
            diagnosticRow("当前页面", diagnostics.currentPageUrl),
            diagnosticRow("User-Agent", diagnostics.userAgent),
            "加载上下文" to if (diagnostics.isSearchResultPage) {
                "内置搜索结果页"
            } else {
                "普通页面或直接访问"
            }
        )
        val items = rows.joinToString(separator = "\n") { (label, value) ->
            """
                <dt>${label.escapeHtml()}</dt>
                <dd>${value.escapeHtml()}</dd>
            """.trimIndent()
        }
        return """
            <section class="diagnostics" aria-label="诊断信息">
              <h2>诊断信息</h2>
              <dl>
                $items
              </dl>
            </section>
        """.trimIndent()
    }

    private fun diagnosticRow(label: String, value: String?): Pair<String, String>? {
        return value
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let { label to it }
    }

    /**
     * 函数 `retryableUrl`：封装 `retryable Url` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param url 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun retryableUrl(url: String?): String? {
        val normalizedUrl = url?.trim()?.takeIf { it.isNotBlank() } ?: return null
        return normalizedUrl.takeIf {
            it.startsWith("http://", ignoreCase = true) ||
                it.startsWith("https://", ignoreCase = true)
        }
    }

    /**
     * 函数 `escapeHtml`：封装 `escape Html` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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
