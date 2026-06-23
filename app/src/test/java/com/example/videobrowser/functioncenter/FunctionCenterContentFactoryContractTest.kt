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
        assertEquals(7, Regex("addFullWidthView\\(").findAll(source).count() - 1)
        assertEquals(1, Regex("ViewGroup\\.LayoutParams\\.WRAP_CONTENT").findAll(source).count())
    }
}
