// settings.gradle.kts 是整个 Gradle 工程的入口。
// 它告诉 Gradle：到哪里下载插件和依赖，以及当前工程包含哪些模块。
pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

// 项目名会显示在 Gradle/Android Studio 的任务树里。
rootProject.name = "VideoBrowser"
// 当前项目只有一个 Android 应用模块：app。
include(":app")
