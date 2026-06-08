# About Version Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add an About entry to the function page and display the app version derived from Git commit count in four-part decimal format.

**Architecture:** Use a pure Kotlin formatter for version math, Gradle build metadata for APK `versionCode` and `versionName`, and a small `AboutPage` class that renders existing function-center info rows. The function page shortcut catalog owns the entry order, so adding `ABOUT` there keeps ordering testable.

**Tech Stack:** Kotlin, Android Gradle Plugin Kotlin DSL, JUnit 4, Android function-center view helpers.

---

## File Structure

- Create `app/src/main/java/com/example/videobrowser/version/AppVersionFormatter.kt`
  - Converts Git commit counts into four-part decimal version names.
- Create `app/src/test/java/com/example/videobrowser/version/AppVersionFormatterTest.kt`
  - Unit tests for carry behavior.
- Modify `app/build.gradle.kts`
  - Reads Git commit count at configuration time.
  - Sets `versionCode`, `versionName`, and `BuildConfig.GIT_COMMIT_COUNT`.
- Modify `app/src/main/java/com/example/videobrowser/functioncenter/FunctionCenterProfileActionCatalog.kt`
  - Adds `ABOUT` after `USER_MANUAL_RULES`.
- Modify `app/src/test/java/com/example/videobrowser/functioncenter/FunctionCenterProfileActionCatalogTest.kt`
  - Verifies normal and private browsing shortcut order.
- Create `app/src/main/java/com/example/videobrowser/functioncenter/AboutPage.kt`
  - Shows app name, version, and Git commit count.
- Modify `app/src/main/java/com/example/videobrowser/functioncenter/FunctionCenterPages.kt`
  - Instantiates `AboutPage` and routes the `ABOUT` grid action.
- Modify `app/src/main/res/values/strings.xml`
  - Adds About page labels.
- Create `app/src/main/res/drawable/ic_info_24.xml`
  - Adds an info icon for the About shortcut.

---

### Task 1: Version Formatter

**Files:**
- Create: `app/src/test/java/com/example/videobrowser/version/AppVersionFormatterTest.kt`
- Create: `app/src/main/java/com/example/videobrowser/version/AppVersionFormatter.kt`

- [ ] **Step 1: Write the failing formatter test**

Create `app/src/test/java/com/example/videobrowser/version/AppVersionFormatterTest.kt`:

```kotlin
package com.example.videobrowser.version

import org.junit.Assert.assertEquals
import org.junit.Test

class AppVersionFormatterTest {
    @Test
    fun formatsCommitCountAsFourPartDecimalVersion() {
        assertEquals("0.0.0.0", AppVersionFormatter.formatCommitCount(0))
        assertEquals("0.0.0.1", AppVersionFormatter.formatCommitCount(1))
        assertEquals("0.0.0.9", AppVersionFormatter.formatCommitCount(9))
        assertEquals("0.0.1.0", AppVersionFormatter.formatCommitCount(10))
        assertEquals("0.1.0.0", AppVersionFormatter.formatCommitCount(100))
        assertEquals("1.0.0.0", AppVersionFormatter.formatCommitCount(1000))
        assertEquals("1.2.3.4", AppVersionFormatter.formatCommitCount(1234))
    }

    @Test
    fun negativeCommitCountsFormatAsZeroVersion() {
        assertEquals("0.0.0.0", AppVersionFormatter.formatCommitCount(-1))
    }
}
```

- [ ] **Step 2: Run the formatter test to verify it fails**

Run:

```powershell
.\gradlew.bat testDebugUnitTest --tests "com.example.videobrowser.version.AppVersionFormatterTest"
```

Expected: FAIL because `AppVersionFormatter` does not exist.

- [ ] **Step 3: Add the minimal formatter implementation**

Create `app/src/main/java/com/example/videobrowser/version/AppVersionFormatter.kt`:

```kotlin
package com.example.videobrowser.version

object AppVersionFormatter {
    fun formatCommitCount(commitCount: Int): String {
        val count = commitCount.coerceAtLeast(0)
        val major = count / 1000
        val hundreds = (count / 100) % 10
        val tens = (count / 10) % 10
        val ones = count % 10
        return "$major.$hundreds.$tens.$ones"
    }
}
```

- [ ] **Step 4: Run the formatter test to verify it passes**

Run:

```powershell
.\gradlew.bat testDebugUnitTest --tests "com.example.videobrowser.version.AppVersionFormatterTest"
```

Expected: PASS for both formatter tests.

- [ ] **Step 5: Commit formatter changes**

Run:

```powershell
git add app/src/main/java/com/example/videobrowser/version/AppVersionFormatter.kt app/src/test/java/com/example/videobrowser/version/AppVersionFormatterTest.kt
git commit -m "feat: add app version formatter"
```

---

### Task 2: Function Page Shortcut Order

**Files:**
- Modify: `app/src/main/java/com/example/videobrowser/functioncenter/FunctionCenterProfileActionCatalog.kt`
- Modify: `app/src/test/java/com/example/videobrowser/functioncenter/FunctionCenterProfileActionCatalogTest.kt`

- [ ] **Step 1: Update the catalog test first**

Edit `FunctionCenterProfileActionCatalogTest.kt` so the expected normal list includes `ABOUT` after `USER_MANUAL_RULES`, and the private list includes `ABOUT` after `FILE_OPERATIONS`:

```kotlin
assertEquals(
    listOf(
        "HISTORY",
        "BOOKMARKS",
        "DOWNLOADS",
        "FILE_OPERATIONS",
        "USER_MANUAL_RULES",
        "ABOUT"
    ),
    actions
)
```

```kotlin
assertEquals(
    listOf(
        "HISTORY",
        "BOOKMARKS",
        "DOWNLOADS",
        "FILE_OPERATIONS",
        "ABOUT"
    ),
    actions
)
```

- [ ] **Step 2: Run the catalog test to verify it fails**

Run:

```powershell
.\gradlew.bat testDebugUnitTest --tests "com.example.videobrowser.functioncenter.FunctionCenterProfileActionCatalogTest"
```

Expected: FAIL because `ABOUT` is not returned by the catalog.

- [ ] **Step 3: Add the ABOUT action to the catalog**

Edit `FunctionCenterProfileActionCatalog.kt`:

```kotlin
enum class FunctionCenterProfileAction {
    HISTORY,
    BOOKMARKS,
    DOWNLOADS,
    FILE_OPERATIONS,
    USER_MANUAL_RULES,
    ABOUT
}

object FunctionCenterProfileActionCatalog {
    fun shortcuts(isPrivateBrowsing: Boolean): List<FunctionCenterProfileAction> {
        return listOfNotNull(
            FunctionCenterProfileAction.HISTORY,
            FunctionCenterProfileAction.BOOKMARKS,
            FunctionCenterProfileAction.DOWNLOADS,
            FunctionCenterProfileAction.FILE_OPERATIONS,
            FunctionCenterProfileAction.USER_MANUAL_RULES.takeIf { !isPrivateBrowsing },
            FunctionCenterProfileAction.ABOUT
        )
    }
}
```

- [ ] **Step 4: Run the catalog test to verify it passes**

Run:

```powershell
.\gradlew.bat testDebugUnitTest --tests "com.example.videobrowser.functioncenter.FunctionCenterProfileActionCatalogTest"
```

Expected: PASS for both catalog tests.

- [ ] **Step 5: Commit catalog changes**

Run:

```powershell
git add app/src/main/java/com/example/videobrowser/functioncenter/FunctionCenterProfileActionCatalog.kt app/src/test/java/com/example/videobrowser/functioncenter/FunctionCenterProfileActionCatalogTest.kt
git commit -m "feat: add about shortcut action"
```

---

### Task 3: Build Metadata From Git Commit Count

**Files:**
- Modify: `app/build.gradle.kts`

- [ ] **Step 1: Add Git commit count metadata to Gradle**

Edit `app/build.gradle.kts` so the top of the file imports `ByteArrayOutputStream` and defines build helpers:

```kotlin
import java.io.ByteArrayOutputStream

plugins {
    alias(libs.plugins.android.application)
}

fun gitCommitCount(): Int {
    val output = ByteArrayOutputStream()
    return try {
        exec {
            commandLine("git", "rev-list", "--count", "HEAD")
            standardOutput = output
            isIgnoreExitValue = true
        }
        output.toString().trim().toIntOrNull() ?: 0
    } catch (_: Exception) {
        0
    }
}

fun formatCommitCountVersion(commitCount: Int): String {
    val count = commitCount.coerceAtLeast(0)
    val major = count / 1000
    val hundreds = (count / 100) % 10
    val tens = (count / 10) % 10
    val ones = count % 10
    return "$major.$hundreds.$tens.$ones"
}

val gitCommitCount = gitCommitCount()
```

Then update the `android` block:

```kotlin
android {
    namespace = "com.example.videobrowser"

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.example.videobrowser"
        minSdk = 24
        targetSdk = 36
        versionCode = gitCommitCount.coerceAtLeast(1)
        versionName = formatCommitCountVersion(gitCommitCount)

        buildConfigField("int", "GIT_COMMIT_COUNT", gitCommitCount.toString())

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}
```

Keep the existing `compileSdk`, `buildTypes`, `compileOptions`, and `dependencies` blocks unchanged except for their position inside `android`.

- [ ] **Step 2: Run a build configuration check**

Run:

```powershell
.\gradlew.bat testDebugUnitTest --tests "com.example.videobrowser.version.AppVersionFormatterTest"
```

Expected: PASS. This also verifies the Gradle script configures successfully with the Git-derived version metadata.

- [ ] **Step 3: Commit Gradle metadata changes**

Run:

```powershell
git add app/build.gradle.kts
git commit -m "feat: derive app version from git commits"
```

---

### Task 4: About Page UI

**Files:**
- Create: `app/src/main/java/com/example/videobrowser/functioncenter/AboutPage.kt`
- Modify: `app/src/main/java/com/example/videobrowser/functioncenter/FunctionCenterPages.kt`
- Modify: `app/src/main/res/values/strings.xml`
- Create: `app/src/main/res/drawable/ic_info_24.xml`

- [ ] **Step 1: Add string and icon resources**

Add these strings to `app/src/main/res/values/strings.xml` near other function-center strings:

```xml
<string name="title_about">关于</string>
<string name="action_about">关于</string>
<string name="action_about_summary">查看版本号和软件信息</string>
<string name="function_center_section_about">软件信息</string>
<string name="about_app_name">应用名称</string>
<string name="about_version">版本号</string>
<string name="about_git_commit_count">Git 提交次数</string>
<string name="about_git_commit_count_summary">%1$d 次提交</string>
```

Create `app/src/main/res/drawable/ic_info_24.xml`:

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="@android:color/white"
        android:pathData="M11,17H13V11H11V17ZM12,2C6.48,2 2,6.48 2,12C2,17.52 6.48,22 12,22C17.52,22 22,17.52 22,12C22,6.48 17.52,2 12,2ZM12,20C7.59,20 4,16.41 4,12C4,7.59 7.59,4 12,4C16.41,4 20,7.59 20,12C20,16.41 16.41,20 12,20ZM11,9H13V7H11V9Z" />
</vector>
```

- [ ] **Step 2: Create the About page**

Create `app/src/main/java/com/example/videobrowser/functioncenter/AboutPage.kt`:

```kotlin
package com.example.videobrowser.functioncenter

import com.example.videobrowser.BuildConfig
import com.example.videobrowser.R

class AboutPage(
    private val host: FunctionCenterPageHost,
    private val showProfilePage: () -> Unit
) {
    private val activity = host.activity

    fun show() {
        host.showPage(
            title = activity.getString(R.string.title_about),
            onBack = showProfilePage
        ) { content ->
            host.addFunctionSection(
                parent = content,
                title = activity.getString(R.string.function_center_section_about)
            ) { section ->
                host.addInfoRow(
                    parent = section,
                    title = activity.getString(R.string.about_app_name),
                    summary = activity.getString(R.string.app_name)
                )
                host.addInfoRow(
                    parent = section,
                    title = activity.getString(R.string.about_version),
                    summary = BuildConfig.VERSION_NAME
                )
                host.addInfoRow(
                    parent = section,
                    title = activity.getString(R.string.about_git_commit_count),
                    summary = activity.getString(
                        R.string.about_git_commit_count_summary,
                        BuildConfig.GIT_COMMIT_COUNT
                    )
                )
            }
        }
    }
}
```

- [ ] **Step 3: Wire ABOUT in FunctionCenterPages**

Add this property with the other page properties in `FunctionCenterPages.kt`:

```kotlin
private val aboutPage = AboutPage(
    host = host,
    showProfilePage = ::showProfilePage
)
```

Add this branch to `createProfileGridAction()`:

```kotlin
FunctionCenterProfileAction.ABOUT -> {
    FunctionCenterGridAction(
        title = activity.getString(R.string.action_about),
        summary = activity.getString(R.string.action_about_summary),
        iconResId = R.drawable.ic_info_24
    ) { aboutPage.show() }
}
```

- [ ] **Step 4: Run focused unit tests and compile Kotlin**

Run:

```powershell
.\gradlew.bat testDebugUnitTest --tests "com.example.videobrowser.functioncenter.FunctionCenterProfileActionCatalogTest" --tests "com.example.videobrowser.version.AppVersionFormatterTest"
```

Expected: PASS for both test classes.

Run:

```powershell
.\gradlew.bat compileDebugKotlin
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Commit About page UI changes**

Run:

```powershell
git add app/src/main/java/com/example/videobrowser/functioncenter/AboutPage.kt app/src/main/java/com/example/videobrowser/functioncenter/FunctionCenterPages.kt app/src/main/res/values/strings.xml app/src/main/res/drawable/ic_info_24.xml
git commit -m "feat: add about page"
```

---

### Task 5: Final Verification

**Files:**
- Read: all changed files

- [ ] **Step 1: Run all unit tests**

Run:

```powershell
.\gradlew.bat testDebugUnitTest
```

Expected: BUILD SUCCESSFUL with all local unit tests passing.

- [ ] **Step 2: Build the debug APK**

Run:

```powershell
.\gradlew.bat assembleDebug
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Inspect changed files**

Run:

```powershell
git status --short
git log --oneline -n 5
```

Expected: only intentional changes remain, or a clean worktree if every task commit was made. Recent commits include the formatter, shortcut action, Git version metadata, and About page commits.
