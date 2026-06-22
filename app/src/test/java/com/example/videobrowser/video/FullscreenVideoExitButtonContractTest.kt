package com.example.videobrowser.video

/**
 * 测试阅读提示：
 * 这个测试文件验证“Fullscreen Video Exit Button Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class FullscreenVideoExitButtonContractTest {
    /**
     * 测试函数 `fullscreenOverlayProvidesTopStartExitButton`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `fullscreen Overlay Provides Top Start Exit Button` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun fullscreenOverlayProvidesTopStartExitButton() {
        val source = projectFile(
            "src/main/java/com/example/videobrowser/video/FullscreenVideoGestureOverlay.kt"
        ).readText()

        assertTrue(source.contains("var onExitFullscreen: (() -> Unit)? = null"))
        assertTrue(source.contains("private val exitButton"))
        assertTrue(source.contains("setupExitButton()"))
        assertTrue(source.contains("gravity = Gravity.TOP or Gravity.START"))
        assertTrue(source.contains("onExitFullscreen?.invoke()"))
    }

    /**
     * 测试函数 `fullscreenExitButtonHasAccessibleLabel`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `fullscreen Exit Button Has Accessible Label` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun fullscreenExitButtonHasAccessibleLabel() {
        val strings = projectFile("src/main/res/values/strings.xml").readText()

        assertTrue(strings.contains("name=\"video_control_exit_fullscreen\""))
        assertTrue(strings.contains(">退出全屏<"))
    }

    /**
     * 测试函数 `fullscreenControllerWiresExitButtonToChromeClient`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `fullscreen Controller Wires Exit Button To Chrome Client` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun fullscreenControllerWiresExitButtonToChromeClient() {
        val source = projectFile(
            "src/main/java/com/example/videobrowser/video/FullscreenVideoController.kt"
        ).readText()
        val chromeClient = projectFile(
            "src/main/java/com/example/videobrowser/browser/ChromeClient.kt"
        ).readText()
        val fullscreenController = projectFile(
            "src/main/java/com/example/videobrowser/browser/ChromeFullscreenController.kt"
        ).readText()

        assertTrue(source.contains("onExitFullscreen = ::exitFullscreen"))
        assertTrue(source.contains("chromeClient()?.hideCustomView()"))
        assertTrue(source.contains("chromeClient()?.exitPageFullscreen()"))
        assertTrue(chromeClient.contains("ChromeFullscreenController("))
        assertTrue(chromeClient.contains("fullscreenController.showCustomView(view, callback)"))
        assertTrue(chromeClient.contains("fullscreenController.hideCustomView()"))
        assertTrue(fullscreenController.contains("CustomViewCallback"))
        assertTrue(fullscreenController.contains("ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE"))
    }

    /**
     * 测试函数 `nativePlayerWiresExitButtonToFinishActivity`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `native Player Wires Exit Button To Finish Activity` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun nativePlayerWiresExitButtonToFinishActivity() {
        val source = projectFile(
            "src/main/java/com/example/videobrowser/video/PlayerActivity.kt"
        ).readText()
        val binder = projectFile(
            "src/main/java/com/example/videobrowser/video/NativePlayerGestureOverlayBinder.kt"
        ).readText()

        assertTrue(source.contains("exitFullscreen = ::finish"))
        assertTrue(binder.contains("onExitFullscreen = exitFullscreen"))
    }

    /**
     * 测试函数 `projectFile`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `project File` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param path 参数类型为 `String`，表示函数执行 `path` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
