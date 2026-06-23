package com.example.videobrowser.utils

import com.example.videobrowser.testutil.projectFile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppDialogContractTest {
    @Test
    fun alertDialogBuildersAreCentralizedInAppDialog() {
        val appDialog = projectFile(
            "src/main/java/com/example/videobrowser/utils/AppDialog.kt"
        ).readText()
        val directBuilderOwners = projectFile("src/main/java/com/example/videobrowser")
            .walkTopDown()
            .filter { file -> file.isFile && file.extension == "kt" }
            .filter { file ->
                Regex("AlertDialog\\.Builder\\(").containsMatchIn(file.readText())
            }
            .map { file -> file.name }
            .toList()

        assertTrue(appDialog.contains("object AppDialog"))
        assertTrue(appDialog.contains("fun builder(context: Context): AlertDialog.Builder"))
        assertEquals(1, Regex("AlertDialog\\.Builder\\(context\\)").findAll(appDialog).count())
        assertEquals(listOf("AppDialog.kt"), directBuilderOwners)
    }
}
