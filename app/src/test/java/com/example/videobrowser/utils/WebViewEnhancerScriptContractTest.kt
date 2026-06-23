package com.example.videobrowser.utils

import com.example.videobrowser.testutil.projectFile
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WebViewEnhancerScriptContractTest {
    @Test
    fun enhancerScriptCallsAreGeneratedBySharedHelper() {
        val helper = projectFile(
            "src/main/java/com/example/videobrowser/utils/WebViewEnhancerScript.kt"
        ).readText()
        val videoProtocol = projectFile(
            "src/main/java/com/example/videobrowser/video/WebViewVideoProtocol.kt"
        ).readText()
        val lifecycleController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserPageLifecycleScriptController.kt"
        ).readText()
        val elementPickerController = projectFile(
            "src/main/java/com/example/videobrowser/element/ElementPickerController.kt"
        ).readText()

        assertTrue(helper.contains("object WebViewEnhancerScript"))
        assertTrue(helper.contains("var enhancer=window.VideoBrowserEnhancer;"))
        assertTrue(helper.contains("if(typeof enhancer.\${call.functionName}==='function')"))
        assertTrue(videoProtocol.contains("WebViewEnhancerScript.call("))
        assertTrue(videoProtocol.contains("WebViewEnhancerScript.callAll("))
        assertTrue(lifecycleController.contains("WebViewEnhancerScript.call("))
        assertTrue(elementPickerController.contains("WebViewEnhancerScript.call(\"startElementPicker\")"))
        assertTrue(elementPickerController.contains("WebViewEnhancerScript.call(\"finishElementPicker\")"))

        listOf(videoProtocol, lifecycleController, elementPickerController).forEach { source ->
            assertFalse(source.contains("var enhancer=window.VideoBrowserEnhancer;"))
            assertFalse(source.contains("window.VideoBrowserEnhancer&&typeof"))
        }
    }

    @Test
    fun enhancerScriptHelperKeepsGuardAndArguments() {
        val script = WebViewEnhancerScript.call("suspend", "{pauseVideos:true}")
        val chain = WebViewEnhancerScript.callAll(
            WebViewEnhancerScript.Call("togglePlayPause"),
            WebViewEnhancerScript.Call("wakeControls")
        )

        assertTrue(script.contains("if(!enhancer)return;"))
        assertTrue(script.contains("typeof enhancer.suspend==='function'"))
        assertTrue(script.contains("enhancer.suspend({pauseVideos:true});"))
        assertTrue(
            chain.indexOf("enhancer.togglePlayPause();") <
                chain.indexOf("enhancer.wakeControls();")
        )
    }
}
