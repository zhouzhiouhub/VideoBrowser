package com.example.videobrowser

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Test

class MainActivityLayoutContractTest {
    @Test
    fun addressBarDoesNotExposeUnimplementedVoiceOrCameraEntries() {
        val idNames = R.id::class.java.declaredFields.map { it.name }
        val layout = projectFile("src/main/res/layout/activity_main.xml").readText()

        assertFalse(idNames.contains("voiceIcon"))
        assertFalse(layout.contains("@drawable/ic_camera_24"))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
