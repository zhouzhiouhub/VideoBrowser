package com.example.videobrowser.functioncenter

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AboutPageContractTest {
    @Test
    fun aboutPageOnlyDisplaysVersionNumber() {
        val source = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/AboutPage.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()

        assertTrue(source.contains("R.string.about_version"))
        assertFalse(source.contains("R.string.about_app_name"))
        assertFalse(source.contains("R.string.about_git_commit_count"))
        assertFalse(source.contains("BuildConfig.GIT_COMMIT_COUNT"))
        assertFalse(strings.contains("name=\"about_app_name\""))
        assertFalse(strings.contains("name=\"about_git_commit_count\""))
        assertFalse(strings.contains("name=\"about_git_commit_count_summary\""))
        assertTrue(strings.contains("name=\"action_about_summary\">查看版本号</string>"))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
