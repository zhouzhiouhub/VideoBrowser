# Baidu Style Browser Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Restyle the current Android browser shell to match Baidu mobile browser patterns.

**Architecture:** Keep the existing WebView and controllers. Update native resources, layout, control wiring, and function center composition so the app shell visually matches the supplied Baidu screenshots.

**Tech Stack:** Android XML layouts/resources, Kotlin controllers, Gradle unit tests.

---

### Task 1: Baidu Defaults

**Files:**
- Modify: `app/src/test/java/com/example/videobrowser/settings/SettingsManagerTest.kt`
- Modify: `app/src/main/java/com/example/videobrowser/settings/SettingsManager.kt`

- [ ] Add failing assertions that default home/search use Baidu.
- [ ] Run `.\gradlew.bat testDebugUnitTest`.
- [ ] Change `DEFAULT_SEARCH_ENGINE_ID` to `baidu` and `DEFAULT_HOME_URL` to `https://m.baidu.com/`.
- [ ] Re-run `.\gradlew.bat testDebugUnitTest`.

### Task 2: Native Shell Resources

**Files:**
- Modify: `app/src/main/res/values/colors.xml`
- Modify: `app/src/main/res/values/strings.xml`
- Add: Baidu-style vector drawables and button backgrounds under `app/src/main/res/drawable`.

- [ ] Add Baidu blue, light shell colors, and white card backgrounds.
- [ ] Add strings for Wenxin, tabs, profile, login, and menu history labels.
- [ ] Add vector icons for Baidu paw/profile, microphone, camera, share, history, download, settings, files, and more.

### Task 3: Browser Layout

**Files:**
- Modify: `app/src/main/res/layout/activity_main.xml`
- Modify: `app/src/main/java/com/example/videobrowser/MainActivityViews.kt`
- Modify: `app/src/main/java/com/example/videobrowser/MainActivity.kt`

- [ ] Restyle the top bar to a blue-stroked search field.
- [ ] Replace bottom controls with back, menu, Wenxin, tabs/home, and profile.
- [ ] Bind the new profile and Wenxin views.
- [ ] Keep home navigation available through the tab/home button.

### Task 4: Controls And Pages

**Files:**
- Modify: `app/src/main/java/com/example/videobrowser/browser/BrowserControlsController.kt`
- Modify: `app/src/main/java/com/example/videobrowser/functioncenter/FunctionCenterViewFactory.kt`
- Modify: `app/src/main/java/com/example/videobrowser/functioncenter/FunctionCenterPages.kt`

- [ ] Wire the menu button to the existing half sheet.
- [ ] Wire the avatar button to an independent full page.
- [ ] Redesign the half sheet with login row, two-row icon grid, and browsing history preview.
- [ ] Keep the existing action callbacks for share, bookmark, history, file/download, refresh, settings, and more.

### Task 5: Verification

**Files:**
- Verify only.

- [ ] Run `.\gradlew.bat testDebugUnitTest`.
- [ ] Run `.\gradlew.bat assembleDebug`.
- [ ] Inspect the final diff and summarize changed files.
