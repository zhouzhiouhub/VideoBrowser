# G6 Regression Stability Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Establish focused regression test collections for ad blocking, video playback/fullscreen, site-level switches, rule cache fallback, and release verification records.

**Architecture:** G6 adds tests only unless a failing regression test exposes a real production defect. The regression tests use existing public module APIs in `adblock/`, `rules/`, `settings/`, `inject/`, and WebView instrumentation; no new filtering syntax or runtime behavior is introduced.

**Tech Stack:** Kotlin/JUnit unit tests, Android WebView instrumentation tests, existing Gradle debug test tasks.

---

### Task 1: Ad Blocking And Rule Cache Regression Tests

**Files:**
- Create: `app/src/test/java/com/example/videobrowser/regression/AdBlockRegressionTest.kt`
- Modify: `app/src/test/java/com/example/videobrowser/rules/RuleFileLoaderTest.kt`

- [ ] **Step 1: Write failing regression tests**

Add tests that verify:

```kotlin
@Test
fun requestDecisionRegression_keepsWhitelistSiteDisableAllowAndNoopBoundaries()

@Test
fun loadRequestRules_usesCacheWhenAssetOpenFails()
```

The first test should exercise `AdBlockManager`, `AdBlockRequestInterceptor`, `RuleEngine`, and redirect/noop boundaries. The second should prove a missing asset stream does not prevent valid cache rules from loading.

- [ ] **Step 2: Run tests to verify RED**

Run: `.\gradlew.bat testDebugUnitTest --tests com.example.videobrowser.regression.AdBlockRegressionTest --tests com.example.videobrowser.rules.RuleFileLoaderTest`

Expected: FAIL because the new regression test class does not exist yet, and any missing cache fallback assertion is not implemented.

- [ ] **Step 3: Implement only tests or the minimal production fix**

If existing production code already satisfies the tests, keep the change test-only. If cache fallback fails, fix `RuleFileLoader` so asset open failure still allows cache files to load.

- [ ] **Step 4: Run tests to verify GREEN**

Run: `.\gradlew.bat testDebugUnitTest --tests com.example.videobrowser.regression.AdBlockRegressionTest --tests com.example.videobrowser.rules.RuleFileLoaderTest`

Expected: PASS.

- [ ] **Step 5: Commit**

```powershell
git add app/src/test/java/com/example/videobrowser/regression/AdBlockRegressionTest.kt app/src/test/java/com/example/videobrowser/rules/RuleFileLoaderTest.kt app/src/main/java/com/example/videobrowser/rules/RuleFileLoader.kt
git commit -m "test: add adblock regression coverage"
```

### Task 2: Site Switch And Page Feature Regression Tests

**Files:**
- Create: `app/src/test/java/com/example/videobrowser/inject/PageFeatureCoordinatorTest.kt`

- [ ] **Step 1: Write failing regression tests**

Add tests that verify:

```kotlin
@Test
fun currentSiteSwitches_disableAdBlockJsCleanupVideoAndWhitelistSeparately()

@Test
fun injectPageFeatures_appliesCurrentSiteCleanupVideoAndUserSelectors()
```

The first test should use `SettingsManager` and `AdBlockManager` to prove site-level ad-block disable, JS injection disable, video/cleanup disable, and user whitelist stay independent. The second should use `PageFeatureCoordinator` and `JsInjector` to prove site switches produce the expected `PageFeatureConfig`.

- [ ] **Step 2: Run tests to verify RED**

Run: `.\gradlew.bat testDebugUnitTest --tests com.example.videobrowser.inject.PageFeatureCoordinatorTest`

Expected: FAIL because the new regression test class does not exist yet.

- [ ] **Step 3: Implement tests and minimal helpers**

Use an in-memory `PreferenceStore` test helper and a `JsInjector` with a tiny common script. Do not instantiate `MainActivity`.

- [ ] **Step 4: Run tests to verify GREEN**

Run: `.\gradlew.bat testDebugUnitTest --tests com.example.videobrowser.inject.PageFeatureCoordinatorTest`

Expected: PASS.

- [ ] **Step 5: Commit**

```powershell
git add app/src/test/java/com/example/videobrowser/inject/PageFeatureCoordinatorTest.kt
git commit -m "test: add page feature regression coverage"
```

### Task 3: Video Playback And Fullscreen Regression Tests

**Files:**
- Create: `app/src/androidTest/java/com/example/videobrowser/regression/VideoPlaybackRegressionInstrumentedTest.kt`

- [ ] **Step 1: Write failing WebView regression test**

Add a WebView instrumentation test:

```kotlin
@Test
fun videoRegression_keepsControlsSpeedAndFullscreenStateWorking()
```

It should load local HTML with a `<video>`, inject `PageFeatureConfig(cleanupEnabled = false, videoEnabled = true)`, call `setPlaybackSpeed(1.25)`, dispatch Android-style fullscreen events, and assert controls remain enabled and playback speed is preserved.

- [ ] **Step 2: Run tests to verify RED**

Run: `.\gradlew.bat assembleDebugAndroidTest`

Then run: `.\gradlew.bat connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.example.videobrowser.regression.VideoPlaybackRegressionInstrumentedTest"`

Expected: FAIL before the new class exists or before test scaffolding is complete.

- [ ] **Step 3: Implement the instrumentation test**

Reuse `JsInjector` with real assets and keep HTML local to the test. Do not add new production video behavior.

- [ ] **Step 4: Run tests to verify GREEN**

Run: `.\gradlew.bat assembleDebugAndroidTest`

Then run: `.\gradlew.bat connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.example.videobrowser.regression.VideoPlaybackRegressionInstrumentedTest"`

Expected: PASS.

- [ ] **Step 5: Commit**

```powershell
git add app/src/androidTest/java/com/example/videobrowser/regression/VideoPlaybackRegressionInstrumentedTest.kt
git commit -m "test: add video playback regression coverage"
```

### Task 4: G6 Documentation And Final Verification

**Files:**
- Modify: `开发目标.md`
- Modify: `开发流程与进度.md`

- [ ] **Step 1: Update G6 checklist and progress records**

Mark G6-01 through G6-05 complete, add implementation notes, and record exact verification commands.

- [ ] **Step 2: Run final verification**

Run:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
.\gradlew.bat assembleDebugAndroidTest
.\gradlew.bat connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.example.videobrowser.regression.VideoPlaybackRegressionInstrumentedTest"
.\gradlew.bat connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.example.videobrowser.inject.JsInjectorInstrumentedTest,com.example.videobrowser.adblock.AdBlockRequestInterceptorInstrumentedTest"
```

Expected: all commands PASS.

- [ ] **Step 3: Commit**

```powershell
git add '开发目标.md' '开发流程与进度.md'
git commit -m "docs: 更新 G6 回归稳定性进度"
```

---

### Self-Review

- G6-01 is covered by `AdBlockRegressionTest`.
- G6-02 is covered by `VideoPlaybackRegressionInstrumentedTest`.
- G6-03 is covered by `PageFeatureCoordinatorTest`.
- G6-04 is covered by the RuleFileLoader cache fallback test.
- G6-05 is covered by the final verification records in both Markdown files.
