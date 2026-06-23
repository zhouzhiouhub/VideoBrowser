package com.example.videobrowser.utils

import com.example.videobrowser.testutil.projectFile
import org.junit.Assert.assertTrue
import org.junit.Test

class AndroidUriParserTest {
    @Test
    fun parseTrimmedOrNull_normalizesBlankInputBeforeParsing() {
        val source = projectFile(
            "src/main/java/com/example/videobrowser/utils/AndroidUriParser.kt"
        ).readText()

        assertTrue(source.contains("val normalized = value?.trim().orEmpty()"))
        assertTrue(source.contains("if (normalized.isEmpty())"))
        assertTrue(source.contains("return null"))
        assertTrue(source.contains("return parse(normalized)"))
    }
}
