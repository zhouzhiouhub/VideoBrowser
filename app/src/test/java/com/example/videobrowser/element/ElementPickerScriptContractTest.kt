package com.example.videobrowser.element

/**
 * 测试阅读提示：
 * 这个测试文件验证“Element Picker Script Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ElementPickerScriptContractTest {
    @Test
    fun elementPickerKeepsPageClickSuppressionUntilNativeConfirmFinishes() {
        val script = projectFile("src/main/assets/scripts/common.js").readText()
        val selectionBody = functionBody(script, "handleElementPickerSelection")
        val stopBody = functionBody(script, "stopElementPicker")

        assertTrue(selectionBody.contains("picker.waitingForNative = true;"))
        assertFalse(
            "Element picker must keep capture listeners after a page element is selected so delayed clicks cannot navigate.",
            selectionBody.contains("detachElementPickerListeners(picker);")
        )
        assertTrue(stopBody.contains("detachElementPickerListeners(picker);"))
    }

    @Test
    fun elementPickerSuppressesPageEventsWhileWaitingForNativeConfirm() {
        val script = projectFile("src/main/assets/scripts/common.js").readText()
        val moveBody = functionBody(script, "handleElementPickerMove")
        val selectionBody = functionBody(script, "handleElementPickerSelection")

        assertTrue(moveBody.contains("if (picker.waitingForNative) {"))
        assertTrue(moveBody.contains("preventElementPickerEvent(event);"))
        assertTrue(selectionBody.contains("if (picker.waitingForNative) {"))
        assertTrue(selectionBody.contains("preventElementPickerEvent(event);"))
    }

    private fun functionBody(script: String, name: String): String {
        val functionStart = script.indexOf("function $name(")
        assertTrue("Missing function $name", functionStart >= 0)
        val bodyStart = script.indexOf('{', functionStart)
        assertTrue("Missing body for function $name", bodyStart >= 0)

        var depth = 0
        for (index in bodyStart until script.length) {
            when (script[index]) {
                '{' -> depth += 1
                '}' -> {
                    depth -= 1
                    if (depth == 0) {
                        return script.substring(bodyStart, index + 1)
                    }
                }
            }
        }
        error("Unclosed body for function $name")
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
