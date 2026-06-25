package com.example.videobrowser.utils

import com.example.videobrowser.testutil.projectFile

import java.text.DateFormat
import java.util.Date
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShortDateTimeFormatterContractTest {
    @Test
    fun `short date time formatter keeps platform short date time behavior`() {
        val timestampMillis = 1_700_000_000_000L

        assertEquals(
            DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                .format(Date(timestampMillis)),
            ShortDateTimeFormatter.format(timestampMillis)
        )
    }

    @Test
    fun `function center pages use shared short date time formatter`() {
        val downloadsFormatter = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/DownloadsPageTextFormatter.kt"
        ).readText()
        val playbackHistoryText = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/PlaybackHistoryDisplayText.kt"
        ).readText()
        val savedPageRecordSection = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/SavedPageRecordSection.kt"
        ).readText()

        assertTrue(downloadsFormatter.contains("ShortDateTimeFormatter.format(record.createdAtMillis)"))
        assertTrue(playbackHistoryText.contains("ShortDateTimeFormatter.format(updatedAtMillis)"))
        assertTrue(savedPageRecordSection.contains("timestamp?.let(ShortDateTimeFormatter::format)"))
        assertFalse(downloadsFormatter.contains("getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)"))
        assertFalse(playbackHistoryText.contains("getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)"))
        assertFalse(savedPageRecordSection.contains("getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)"))
    }

}
