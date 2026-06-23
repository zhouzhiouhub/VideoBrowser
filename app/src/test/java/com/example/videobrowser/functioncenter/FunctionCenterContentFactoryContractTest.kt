package com.example.videobrowser.functioncenter

import com.example.videobrowser.testutil.projectFile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FunctionCenterContentFactoryContractTest {
    @Test
    fun fullWidthWrapContentLayoutIsShared() {
        val source = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterContentFactory.kt"
        ).readText()

        assertTrue(source.contains("private fun addFullWidthView("))
        assertTrue(source.contains("addFullWidthView(parent, section)"))
        assertTrue(source.contains("addFullWidthView(parent, row)"))
        assertTrue(source.contains("height: Int = ViewGroup.LayoutParams.WRAP_CONTENT"))
        assertTrue(source.contains("height = dp(46)"))
        assertTrue(source.contains("height = dp(1)"))
        assertEquals(9, Regex("addFullWidthView\\(").findAll(source).count() - 1)
        assertEquals(1, Regex("LinearLayout\\.LayoutParams\\(").findAll(source).count())
    }
}
