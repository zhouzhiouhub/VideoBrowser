# Incognito Isolation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement runtime-only private browsing isolation, home-screen visibility rules, and private-mode write blocking from `docs/superpowers/specs/2026-06-03-incognito-isolation-design.md`.

**Architecture:** Add a small runtime session layer that owns `STANDARD` vs `PRIVATE` state and swaps the active WebView without persisting mode. Keep the existing `BrowserManager` as the single controller facade, but allow it to attach to the currently active WebView so existing controllers keep working. Gate persistent writes in controllers by `isPrivateBrowsingEnabled()` and hide private-mode write-entry UI.

**Tech Stack:** Android Kotlin, AppCompat, WebView, JUnit4 local unit tests, Gradle Android plugin.

---

### Task 1: Runtime Mode Persistence Tests

**Files:**
- Modify: `app/src/test/java/com/example/videobrowser/settings/SettingsManagerTest.kt`
- Modify: `app/src/main/java/com/example/videobrowser/settings/SettingsManager.kt`

- [ ] **Step 1: Write the failing tests**

Add tests that prove legacy `private_browsing=true` is ignored and `setPrivateBrowsingEnabled(true)` does not persist runtime private mode:

```kotlin
@Test
fun privateBrowsingPreference_isIgnoredOnStartup() {
    val store = InMemoryPreferenceStore()
    store.putBoolean("private_browsing", true)

    val settings = SettingsManager(store)

    assertFalse(settings.isPrivateBrowsingEnabled())
}

@Test
fun setPrivateBrowsingEnabled_doesNotPersistPrivateMode() {
    val store = InMemoryPreferenceStore()
    val settings = SettingsManager(store)

    settings.setPrivateBrowsingEnabled(true)

    val reloaded = SettingsManager(store)
    assertFalse(reloaded.isPrivateBrowsingEnabled())
    assertFalse(store.contains("private_browsing"))
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests com.example.videobrowser.settings.SettingsManagerTest`

Expected: FAIL because current `SettingsManager` persists and reloads `private_browsing=true`.

- [ ] **Step 3: Write minimal implementation**

Change `SettingsManager.isPrivateBrowsingEnabled()` to return `false`, and change `setPrivateBrowsingEnabled(enabled: Boolean)` to remove `KEY_PRIVATE_BROWSING` so old saved state is migrated away.

- [ ] **Step 4: Run test to verify it passes**

Run: `.\gradlew.bat testDebugUnitTest --tests com.example.videobrowser.settings.SettingsManagerTest`

Expected: PASS.

### Task 2: Runtime Session Unit Tests

**Files:**
- Create: `app/src/main/java/com/example/videobrowser/browser/session/BrowsingSessionMode.kt`
- Create: `app/src/main/java/com/example/videobrowser/browser/session/RuntimePrivateBrowsingState.kt`
- Create: `app/src/test/java/com/example/videobrowser/browser/session/RuntimePrivateBrowsingStateTest.kt`

- [ ] **Step 1: Write the failing tests**

Test that runtime mode starts standard, enters private in memory, exits to standard, and records cleanup only when leaving private:

```kotlin
class RuntimePrivateBrowsingStateTest {
    @Test
    fun startsInStandardMode() {
        val state = RuntimePrivateBrowsingState()

        assertEquals(BrowsingSessionMode.STANDARD, state.mode)
        assertFalse(state.isPrivate)
    }

    @Test
    fun enterAndExitPrivateMode_areRuntimeOnly() {
        var cleanupCalls = 0
        val state = RuntimePrivateBrowsingState(onPrivateCleanup = { cleanupCalls++ })

        assertTrue(state.enterPrivate())
        assertEquals(BrowsingSessionMode.PRIVATE, state.mode)
        assertTrue(state.isPrivate)

        assertTrue(state.exitPrivate())
        assertEquals(BrowsingSessionMode.STANDARD, state.mode)
        assertEquals(1, cleanupCalls)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests com.example.videobrowser.browser.session.RuntimePrivateBrowsingStateTest`

Expected: FAIL because the classes do not exist.

- [ ] **Step 3: Write minimal implementation**

Create `BrowsingSessionMode` enum with `STANDARD` and `PRIVATE`. Create `RuntimePrivateBrowsingState` with `mode`, `isPrivate`, `enterPrivate()`, `exitPrivate()`, and `resetToStandard()`; call `onPrivateCleanup` exactly once when transitioning from private to standard.

- [ ] **Step 4: Run test to verify it passes**

Run: `.\gradlew.bat testDebugUnitTest --tests com.example.videobrowser.browser.session.RuntimePrivateBrowsingStateTest`

Expected: PASS.

### Task 3: Android Session Swapping

**Files:**
- Modify: `app/src/main/java/com/example/videobrowser/MainActivityViews.kt`
- Modify: `app/src/main/java/com/example/videobrowser/browser/BrowserManager.kt`
- Modify: `app/src/main/java/com/example/videobrowser/browser/BrowserControlsScrollController.kt`
- Create: `app/src/main/java/com/example/videobrowser/browser/session/PrivateBrowsingSession.kt`
- Modify: `app/src/main/java/com/example/videobrowser/MainActivity.kt`

- [ ] **Step 1: Implement attachable BrowserManager**

Change `BrowserManager` to store `private var webView: WebView`, expose `val activeWebView: WebView`, and add `fun attachWebView(webView: WebView)` that switches the active WebView. Keep existing methods delegating to the active WebView.

- [ ] **Step 2: Implement scroll-controller reattachment**

Change `BrowserControlsScrollController` to store `private var webView: WebView`, add `attachWebView(newWebView: WebView)`, clear old listeners with `setOnTouchListener(null)` and `setOnScrollChangeListener(null)`, then install the existing touch/scroll listeners on the new WebView.

- [ ] **Step 3: Implement PrivateBrowsingSession**

Create an Android coordinator that keeps the standard WebView, creates a temporary private WebView on enter, swaps it into `webViewContainer`, calls `setupActiveWebView(privateWebView, PRIVATE)`, and destroys/cleans it on exit or destroy before restoring the standard WebView and `STANDARD` setup.

- [ ] **Step 4: Wire MainActivity**

Bind `webViewContainer`, create `PrivateBrowsingSession`, replace calls to `settingsManager.isPrivateBrowsingEnabled()` with runtime `privateBrowsingSession.isPrivate`, implement `setPrivateBrowsingMode(enabled: Boolean)`, and call `privateBrowsingSession.destroy()` in `onDestroy()`.

- [ ] **Step 5: Run build**

Run: `.\gradlew.bat assembleDebug`

Expected: build succeeds after wiring all constructor changes.

### Task 4: Persistent Write Gates

**Files:**
- Modify: `app/src/main/java/com/example/videobrowser/browser/PageActionsController.kt`
- Modify: `app/src/main/java/com/example/videobrowser/browser/search/SearchProviderController.kt`
- Modify: `app/src/main/java/com/example/videobrowser/functioncenter/FunctionCenterPages.kt`
- Modify: `app/src/main/java/com/example/videobrowser/functioncenter/BrowserSettingsPage.kt`
- Modify: `app/src/main/java/com/example/videobrowser/functioncenter/CurrentSiteSettingsPage.kt`

- [ ] **Step 1: Gate history by runtime mode**

Inject `isPrivateBrowsingEnabled: () -> Boolean` into `PageActionsController` and use it in `addHistoryEntry(url)`; keep `toggleCurrentBookmark()` unchanged so user-initiated private bookmarks still write to bookmarks.

- [ ] **Step 2: Gate private switch by runtime session**

Change `PageActionsController.setPrivateBrowsingEnabled(enabled)` to call `changePrivateBrowsingMode(enabled)` instead of `settingsManager.setPrivateBrowsingEnabled(enabled)`, avoid clearing standard history, and avoid reloading the standard WebView during the switch.

- [ ] **Step 3: Gate search provider writes**

Inject `isPrivateBrowsingEnabled` into `SearchProviderController`. In `setup()`, only initialize missing `home_url` when not private. In `selectProvider(provider)`, update runtime selection but skip `settingsManager.setSearchEngineId` and `settingsManager.setHomeUrl` when private.

- [ ] **Step 4: Hide private-mode write-entry UI**

In `FunctionCenterPages`, hide current-site settings and element picker actions when private. In `BrowserSettingsPage`, show the private switch but omit global persistent setting rows, whitelist manager, clear browser data, and restore defaults while private. In `CurrentSiteSettingsPage`, avoid showing site-setting switches, whitelist, and element rule actions when private.

- [ ] **Step 5: Run build**

Run: `.\gradlew.bat assembleDebug`

Expected: build succeeds.

### Task 5: Home UI and Private Theme Rules

**Files:**
- Modify: `app/src/main/java/com/example/videobrowser/MainActivityViews.kt`
- Modify: `app/src/main/java/com/example/videobrowser/MainActivity.kt`
- Modify: `app/src/main/java/com/example/videobrowser/browser/BrowserControlsController.kt`
- Modify: `app/src/main/java/com/example/videobrowser/browser/search/SearchProviderController.kt`

- [ ] **Step 1: Hide home-screen controls**

In `BrowserControlsController.updateNavigationButtons()`, set `homeButton.visibility = GONE` and `bookmarkButton.visibility = GONE` when `isHomePageVisible()` is true; show both when normal web content is visible.

- [ ] **Step 2: Hide private badge**

Change `MainActivity.updatePrivateBrowsingUi()` so `privateBrowsingBadge` is always `GONE`; this removes both home and webpage private badge display.

- [ ] **Step 3: Hide search providers on private home**

Add a private-mode condition to `SearchProviderController.syncVisibility()` so the provider strip only appears on the standard home page.

- [ ] **Step 4: Apply runtime dark palette**

In `MainActivity.applyBrowsingModeTheme()`, apply dark colors to root, top/bottom bars, address bar, text, icons, progress, and web container when private; restore light colors when standard. Call it after mode changes and home-content changes.

- [ ] **Step 5: Run focused and full verification**

Run:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```

Expected: all unit tests pass and debug APK builds.

---

## Self-Review

- Spec coverage: runtime-only private mode, independent WebView, write gates, home visibility, private dark theme, cleanup on exit, and startup standard mode are covered.
- Placeholder scan: no `TBD`, `TODO`, or undefined implementation steps remain.
- Type consistency: runtime mode names are `BrowsingSessionMode.STANDARD` and `BrowsingSessionMode.PRIVATE`; the runtime state class and Android session class are separate to keep unit tests pure.
