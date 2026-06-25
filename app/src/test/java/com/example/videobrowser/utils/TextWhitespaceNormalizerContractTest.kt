package com.example.videobrowser.utils

import com.example.videobrowser.testutil.projectFile

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TextWhitespaceNormalizerContractTest {
    @Test
    fun `text cleanup callers share whitespace normalization`() {
        val shortcutStore = projectFile(
            "src/main/java/com/example/videobrowser/settings/CustomShortcutStore.kt"
        ).readText()
        val settingsCssSelectorNormalizer = projectFile(
            "src/main/java/com/example/videobrowser/settings/SettingsCssSelectorNormalizer.kt"
        ).readText()
        val downloadSafetyPolicy = projectFile(
            "src/main/java/com/example/videobrowser/download/DownloadSafetyPolicy.kt"
        ).readText()
        val playbackHistoryRepository = projectFile(
            "src/main/java/com/example/videobrowser/video/PlaybackHistoryRepository.kt"
        ).readText()
        val savedPageRepository = projectFile(
            "src/main/java/com/example/videobrowser/storage/SavedPageCodec.kt"
        ).readText()
        val nativeBridge = projectFile(
            "src/main/java/com/example/videobrowser/browser/VideoBrowserNativeBridge.kt"
        ).readText()
        val pageArchiveFileName = projectFile(
            "src/main/java/com/example/videobrowser/browser/PageArchiveFileName.kt"
        ).readText()
        val pagePrintController = projectFile(
            "src/main/java/com/example/videobrowser/browser/PagePrintController.kt"
        ).readText()
        val browserLaunchController = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserLaunchController.kt"
        ).readText()
        val urlUtils = projectFile(
            "src/main/java/com/example/videobrowser/utils/UrlUtils.kt"
        ).readText()
        val searchUrlQueryParser = projectFile(
            "src/main/java/com/example/videobrowser/utils/SearchUrlQueryParser.kt"
        ).readText()
        val searchQueryTerms = projectFile(
            "src/main/java/com/example/videobrowser/utils/SearchQueryTerms.kt"
        ).readText()

        listOf(
            shortcutStore,
            settingsCssSelectorNormalizer,
            downloadSafetyPolicy,
            playbackHistoryRepository,
            savedPageRepository,
            nativeBridge,
            pageArchiveFileName,
            pagePrintController,
            browserLaunchController,
            urlUtils,
            searchUrlQueryParser,
            searchQueryTerms
        ).forEach { source ->
            assertTrue(source.contains("TextWhitespaceNormalizer"))
            assertFalse(source.contains("Regex(\"\\\\s+\")"))
        }
    }

}
