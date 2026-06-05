# Download Feature Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a downloads page to the function center and record browser downloads initiated through WebView or the current URL action.

**Architecture:** Continue using Android `DownloadManager` for file storage. Add a focused repository for download metadata, wire `DownloadController` to record successful enqueues, and render records through a new `DownloadsPage` using existing function center components.

**Tech Stack:** Kotlin, Android WebView, Android DownloadManager, SharedPreferences-backed `PreferenceStore`, JUnit unit tests.

---

### Task 1: Download Record Repository

**Files:**
- Create: `app/src/main/java/com/example/videobrowser/download/DownloadRecord.kt`
- Create: `app/src/main/java/com/example/videobrowser/download/DownloadRecordRepository.kt`
- Test: `app/src/test/java/com/example/videobrowser/download/DownloadRecordRepositoryTest.kt`

- [ ] Write failing tests for save, ordering, duplicate replacement, limit trimming, and corrupted JSON recovery.
- [ ] Run `.\gradlew.bat testDebugUnitTest --tests com.example.videobrowser.download.DownloadRecordRepositoryTest` and verify the repository is missing.
- [ ] Implement `DownloadRecord` and `DownloadRecordRepository`.
- [ ] Re-run the repository test and verify it passes.

### Task 2: Download Controller Recording

**Files:**
- Modify: `app/src/main/java/com/example/videobrowser/download/DownloadController.kt`
- Modify: `app/src/main/java/com/example/videobrowser/MainActivity.kt`

- [ ] Inject `DownloadRecordRepository` into `DownloadController`.
- [ ] Record only after `DownloadManager.enqueue` returns a download id.
- [ ] Preserve native-player handling for playable media URLs and external fallback on enqueue failure.

### Task 3: Function Center Downloads Page

**Files:**
- Create: `app/src/main/java/com/example/videobrowser/functioncenter/DownloadsPage.kt`
- Modify: `app/src/main/java/com/example/videobrowser/functioncenter/FunctionCenterPages.kt`
- Modify: `app/src/main/java/com/example/videobrowser/functioncenter/FunctionCenterRootActionCatalog.kt`
- Modify: `app/src/main/java/com/example/videobrowser/functioncenter/FunctionCenterProfileActionCatalog.kt`
- Modify: `app/src/main/res/values/strings.xml`

- [ ] Add failing catalog tests for the new downloads entry ordering.
- [ ] Add `DownloadsPage` with list, open, and clear actions.
- [ ] Wire root and profile shortcut entries to show the downloads page.
- [ ] Add user-facing strings.

### Task 4: Verification

**Files:**
- Test: relevant unit tests and Android build.

- [ ] Run focused repository and catalog tests.
- [ ] Run `.\gradlew.bat testDebugUnitTest`.
- [ ] Run `.\gradlew.bat assembleDebug`.
