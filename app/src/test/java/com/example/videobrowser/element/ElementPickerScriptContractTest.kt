package com.example.videobrowser.element

import com.example.videobrowser.testutil.projectFile

/**
 * 测试阅读提示：
 * 这个测试文件验证“Element Picker Script Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ElementPickerScriptContractTest {
    /**
     * 测试函数 `elementPickerKeepsPageClickSuppressionUntilNativeConfirmFinishes`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `element Picker Keeps Page Click Suppression Until Native Confirm Finishes` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun elementPickerKeepsPageClickSuppressionUntilNativeConfirmFinishes() {
        val commonScript = projectFile("src/main/assets/scripts/common.js").readText()
        val runtimeScript = projectFile("src/main/assets/scripts/enhancer_runtime.js").readText()
        val selectorScript = projectFile("src/main/assets/scripts/element_picker_selector_tools.js").readText()
        val pickerScript = projectFile("src/main/assets/scripts/element_picker.js").readText()
        val nativeBridgeScript = projectFile("src/main/assets/scripts/native_bridge.js").readText()
        val scriptLoader = projectFile("src/main/java/com/example/videobrowser/inject/ScriptLoader.kt").readText()
        val commonAssetList = scriptLoader.substringAfter("val COMMON_SCRIPT_ASSETS = listOf(")
        val selectionBody = functionBody(pickerScript, "handleElementPickerSelection")
        val stopBody = functionBody(pickerScript, "stopElementPicker")

        assertTrue(selectorScript.contains("window.VideoBrowserElementPickerSelectorTools = tools"))
        assertTrue(selectorScript.contains("tools.buildSelector = tools.buildSelector || function (element)"))
        assertTrue(selectorScript.contains("tools.describeElement = tools.describeElement || function (element)"))
        assertTrue(pickerScript.contains("window.VideoBrowserElementPicker = pickerModule"))
        assertTrue(pickerScript.contains("const geometry = window.VideoBrowserGeometry || {};"))
        assertTrue(pickerScript.contains("const pickerSelectorTools = window.VideoBrowserElementPickerSelectorTools || {};"))
        assertTrue(commonScript.contains("const elementPicker = window.VideoBrowserElementPicker"))
        assertTrue(commonScript.contains("elementPicker: elementPicker"))
        assertTrue(runtimeScript.contains("return elementPicker.start(state);"))
        assertTrue(runtimeScript.contains("elementPicker.stop(state);"))
        assertFalse(commonScript.contains("return elementPicker.start(state);"))
        assertFalse(commonScript.contains("elementPicker.stop(state);"))
        assertFalse(commonScript.contains("function handleElementPickerSelection(state, event)"))
        assertTrue(scriptLoader.contains("ELEMENT_PICKER_SELECTOR_TOOLS_SCRIPT_ASSET"))
        assertTrue(
            commonAssetList.indexOf("ELEMENT_PICKER_SELECTOR_TOOLS_SCRIPT_ASSET") <
                commonAssetList.indexOf("ELEMENT_PICKER_SCRIPT_ASSET")
        )
        assertTrue(selectionBody.contains("picker.waitingForNative = true;"))
        assertTrue(pickerScript.contains("const nativeBridge = window.VideoBrowserNativeBridge || {};"))
        assertTrue(selectionBody.contains("pickerSelectorTools.buildSelector(element)"))
        assertTrue(selectionBody.contains("pickerSelectorTools.describeElement(element)"))
        assertTrue(selectionBody.contains("nativeBridge.requestElementBlock(selector, description)"))
        assertTrue(
            nativeBridgeScript.contains(
                "bridgeTools.requestElementBlock = bridgeTools.requestElementBlock || function (selector, description)"
            )
        )
        assertTrue(nativeBridgeScript.contains("bridgeTools.callNative('requestElementBlock', [selector, description])"))
        assertFalse(selectionBody.contains("window.VideoBrowserNative"))
        assertFalse(
            "Element picker must keep capture listeners after a page element is selected so delayed clicks cannot navigate.",
            selectionBody.contains("detachElementPickerListeners(picker);")
        )
        assertTrue(stopBody.contains("detachElementPickerListeners(picker);"))
        assertFalse(pickerScript.contains("function buildElementPickerSelector(element)"))
        assertFalse(pickerScript.contains("function describePickedElement(element)"))
        assertFalse(pickerScript.contains("function isStableSelectorToken(value)"))
        assertFalse(pickerScript.contains("getBoundingClientRect"))
    }

    /**
     * 测试函数 `elementPickerSuppressesPageEventsWhileWaitingForNativeConfirm`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `element Picker Suppresses Page Events While Waiting For Native Confirm` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun elementPickerSuppressesPageEventsWhileWaitingForNativeConfirm() {
        val pickerScript = projectFile("src/main/assets/scripts/element_picker.js").readText()
        val moveBody = functionBody(pickerScript, "handleElementPickerMove")
        val selectionBody = functionBody(pickerScript, "handleElementPickerSelection")

        assertTrue(moveBody.contains("if (picker.waitingForNative) {"))
        assertTrue(moveBody.contains("preventElementPickerEvent(event);"))
        assertTrue(selectionBody.contains("if (picker.waitingForNative) {"))
        assertTrue(selectionBody.contains("preventElementPickerEvent(event);"))
    }

    /**
     * 测试函数 `functionBody`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `function Body` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param script 参数类型为 `String`，表示函数执行 `script` 相关逻辑时需要读取或处理的输入。
     * @param name 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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

}
