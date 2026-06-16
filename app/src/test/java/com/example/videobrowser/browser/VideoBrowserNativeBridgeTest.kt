package com.example.videobrowser.browser

/**
 * 测试阅读提示：
 * 这个测试文件验证“Video Browser Native Bridge Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertEquals
import org.junit.Test

class VideoBrowserNativeBridgeTest {
    /**
     * 测试函数 `updatePlaybackTimelineIgnoresInvalidValues`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `update Playback Timeline Ignores Invalid Values` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun updatePlaybackTimelineIgnoresInvalidValues() {
        val timelineUpdates = mutableListOf<Pair<Double, Double>>()
        val bridge = bridge(
            updatePlaybackTimeline = { positionMs, durationMs ->
                timelineUpdates += positionMs to durationMs
            }
        )

        bridge.updatePlaybackTimeline(Double.NaN, 1000.0)
        bridge.updatePlaybackTimeline(1000.0, Double.POSITIVE_INFINITY)
        bridge.updatePlaybackTimeline(-1.0, 1000.0)
        bridge.updatePlaybackTimeline(1000.0, -1.0)

        assertEquals(emptyList<Pair<Double, Double>>(), timelineUpdates)
    }

    /**
     * 测试函数 `updatePlaybackTimelineClampsLargeValues`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `update Playback Timeline Clamps Large Values` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun updatePlaybackTimelineClampsLargeValues() {
        val timelineUpdates = mutableListOf<Pair<Double, Double>>()
        val bridge = bridge(
            updatePlaybackTimeline = { positionMs, durationMs ->
                timelineUpdates += positionMs to durationMs
            }
        )

        bridge.updatePlaybackTimeline(100_000_000.0, 200_000_000.0)

        assertEquals(listOf(86_400_000.0 to 86_400_000.0), timelineUpdates)
    }

    /**
     * 测试函数 `elementBlockCallbacksSanitizeSelectorsAndDescriptions`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `element Block Callbacks Sanitize Selectors And Descriptions` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun elementBlockCallbacksSanitizeSelectorsAndDescriptions() {
        val requests = mutableListOf<Pair<String, String>>()
        val blockedSelectors = mutableListOf<String>()
        val bridge = bridge(
            requestElementBlock = { selector, description -> requests += selector to description },
            blockSelectedElement = { selector -> blockedSelectors += selector }
        )

        bridge.requestElementBlock("  div > .ad\n  ", "banner\nsponsor")
        bridge.blockSelectedElement("  ${"a".repeat(700)}  ")

        assertEquals(listOf("div > .ad" to "banner sponsor"), requests)
        assertEquals(listOf("a".repeat(500)), blockedSelectors)
    }

    /**
     * 测试函数 `elementBlockCallbacksIgnoreBlankSelectors`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `element Block Callbacks Ignore Blank Selectors` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun elementBlockCallbacksIgnoreBlankSelectors() {
        val requests = mutableListOf<Pair<String, String>>()
        val blockedSelectors = mutableListOf<String>()
        val bridge = bridge(
            requestElementBlock = { selector, description -> requests += selector to description },
            blockSelectedElement = { selector -> blockedSelectors += selector }
        )

        bridge.requestElementBlock("\n\t", "description")
        bridge.blockSelectedElement("   ")

        assertEquals(emptyList<Pair<String, String>>(), requests)
        assertEquals(emptyList<String>(), blockedSelectors)
    }

    /**
     * 测试函数 `logVideoEventSanitizesAndPostsMessageToLogger`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `log Video Event Sanitizes And Posts Message To Logger` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun logVideoEventSanitizesAndPostsMessageToLogger() {
        val messages = mutableListOf<String>()
        val bridge = bridge(
            logVideoEvent = { message -> messages += message }
        )

        bridge.logVideoEvent("controls\nremoved\rfor\tvideo")

        assertEquals(listOf("controls removed for video"), messages)
    }

    /**
     * 测试函数 `bridge`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `bridge` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param updatePlaybackTimeline 参数类型为 `(Double, Double) -> Unit`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     * @param requestElementBlock 参数类型为 `(String, String) -> Unit`，表示一次请求或响应，函数会检查它的内容并决定如何继续处理。
     * @param blockSelectedElement 参数类型为 `(String) -> Unit`，表示一个开关状态，用来决定函数内部走启用还是停用分支。
     * @param logVideoEvent 参数类型为 `(String) -> Unit`，表示函数执行 `logVideoEvent` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun bridge(
        updatePlaybackTimeline: (Double, Double) -> Unit = { _, _ -> },
        requestElementBlock: (String, String) -> Unit = { _, _ -> },
        blockSelectedElement: (String) -> Unit = {},
        logVideoEvent: (String) -> Unit = {}
    ): VideoBrowserNativeBridge {
        return VideoBrowserNativeBridge(
            postToUi = { action -> action() },
            enterFullscreen = {},
            exitFullscreen = {},
            updatePlaybackTimeline = updatePlaybackTimeline,
            requestElementBlock = requestElementBlock,
            blockSelectedElement = blockSelectedElement,
            cancelElementPicker = {},
            logVideoEvent = logVideoEvent
        )
    }
}
