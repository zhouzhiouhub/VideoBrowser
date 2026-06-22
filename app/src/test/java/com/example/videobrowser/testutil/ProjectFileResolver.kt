package com.example.videobrowser.testutil

import java.io.File

fun projectFile(path: String): File {
    val workingDirectory = File("").absoluteFile
    return listOfNotNull(
        File(workingDirectory, path),
        File(workingDirectory, "app/$path"),
        workingDirectory.parentFile?.let { parent -> File(parent, path) }
    ).first { it.exists() }
}
