# G5 Safe Scriptlet Mapping Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a safe scriptlet rule path that maps whitelisted rule names to local VideoBrowser hooks without executing subscription JavaScript.

**Architecture:** Parse scriptlet rules into a separate `ScriptletRule` model, validate them through `ScriptletRegistry`, compile valid rules into existing `RuleCapability.SafeHook`, and expose those hooks through `RuleEngine` to `JsInjector`. `common.js` receives only structured config fields for local hook behavior: window.open keyword blocking, fetch keyword blocking, skip-button clicking, and video-control enabling.

**Tech Stack:** Kotlin/JVM unit tests, Android WebView instrumentation tests, existing `RuleFileLoader`, `RuleCompiler`, `RuleEngine`, `JsInjector`, and `app/src/main/assets/scripts/common.js`.

---

### Task 1: Scriptlet Rule Model And Registry

**Files:**
- Create: `app/src/main/java/com/example/videobrowser/rules/ScriptletRule.kt`
- Create: `app/src/main/java/com/example/videobrowser/rules/ScriptletRegistry.kt`
- Test: `app/src/test/java/com/example/videobrowser/rules/ScriptletRegistryTest.kt`

- [ ] **Step 1: Write the failing registry tests**

```kotlin
@Test
fun parseSupportedScriptlet_keepsNameArgumentsAndDomainScope() {
    val rule = ScriptletRegistry.parse(
        text = "example.com,~safe.example.com##+js(window-open-block-keyword, /ad-popup/)",
        id = "asset:rules/scriptlet_rules.txt:1",
        source = "asset:rules/scriptlet_rules.txt"
    )

    assertEquals("window-open-block-keyword", rule.value.name)
    assertEquals(listOf("/ad-popup/"), rule.value.arguments)
    assertTrue(rule.value.domainScope.matches("www.example.com"))
    assertFalse(rule.value.domainScope.matches("safe.example.com"))
}

@Test
fun parseUnknownOrUnsafeScriptlet_returnsSkippedReason() {
    val result = ScriptletRegistry.parse(
        text = "example.com##+js(run-raw-js, alert(1))",
        id = "asset:rules/scriptlet_rules.txt:2",
        source = "asset:rules/scriptlet_rules.txt"
    )

    assertEquals("unsupported scriptlet", result.skippedReason)
}
```

- [ ] **Step 2: Run tests to verify RED**

Run: `.\gradlew.bat testDebugUnitTest --tests com.example.videobrowser.rules.ScriptletRegistryTest`

Expected: FAIL because `ScriptletRule` and `ScriptletRegistry` do not exist.

- [ ] **Step 3: Implement the minimal model and registry**

Create `ScriptletRule` with `id`, `name`, `arguments`, `source`, and `domainScope`.

Create `ScriptletRegistry` with a whitelist:

```text
window-open-block-keyword: exactly 1 safe keyword argument
fetch-block-keyword: exactly 1 safe keyword argument
click-skip-buttons: no arguments
enable-video-controls: no arguments
```

Safe keyword arguments are trimmed strings with length 3..100 and no control characters, `<`, `>`, or `javascript:`.

- [ ] **Step 4: Run tests to verify GREEN**

Run: `.\gradlew.bat testDebugUnitTest --tests com.example.videobrowser.rules.ScriptletRegistryTest`

Expected: PASS.

- [ ] **Step 5: Commit**

```powershell
git add app/src/main/java/com/example/videobrowser/rules/ScriptletRule.kt app/src/main/java/com/example/videobrowser/rules/ScriptletRegistry.kt app/src/test/java/com/example/videobrowser/rules/ScriptletRegistryTest.kt
git commit -m "feat: add safe scriptlet registry"
```

### Task 2: Scriptlet Rule Loading And Compilation

**Files:**
- Modify: `app/src/main/java/com/example/videobrowser/rules/RuleFileLoader.kt`
- Modify: `app/src/main/java/com/example/videobrowser/rules/RuleCompiler.kt`
- Modify: `app/src/main/java/com/example/videobrowser/rules/RuleEngineFactory.kt`
- Test: `app/src/test/java/com/example/videobrowser/rules/RuleFileLoaderTest.kt`
- Test: `app/src/test/java/com/example/videobrowser/rules/RuleCompilerTest.kt`

- [ ] **Step 1: Write failing loader and compiler tests**

```kotlin
@Test
fun loadScriptletRules_readsAssetsAndCacheAndSkipsUnknownRules() {
    val cacheDirectory = temporaryFolder.newFolder()
    cacheDirectory.resolve(RuleFileLoader.SCRIPTLET_RULES_CACHE_FILE)
        .writeText("example.com##+js(fetch-block-keyword, /cache-ad/)\n", Charsets.UTF_8)
    val loader = loaderFor(
        cacheDirectory = cacheDirectory,
        assets = mapOf(
            RuleFileLoader.SCRIPTLET_RULES_ASSET to """
                example.com##+js(window-open-block-keyword, /popup-ad/)
                example.com##+js(unknown-scriptlet, value)
            """.trimIndent()
        )
    )

    val result = loader.loadScriptletRules()

    assertEquals(2, result.rules.size)
    assertEquals(1, result.skippedRules.size)
    assertEquals("unsupported scriptlet", result.skippedRules.single().reason)
}

@Test
fun compile_createsSafeHookCapabilitiesForValidatedScriptlets() {
    val rule = ScriptletRule(
        id = "scriptlet:1",
        name = "fetch-block-keyword",
        arguments = listOf("/pagead/"),
        source = "test",
        domainScope = DomainScope(includedDomains = setOf("example.com"))
    )

    val result = RuleCompiler().compile(
        requestRules = emptyList(),
        elementRules = emptyList(),
        scriptletRules = listOf(rule)
    )

    assertEquals(1, result.safeHookCapabilities.size)
    assertEquals("fetch-block-keyword", result.safeHookCapabilities.single().hookName)
}
```

- [ ] **Step 2: Run tests to verify RED**

Run: `.\gradlew.bat testDebugUnitTest --tests com.example.videobrowser.rules.RuleFileLoaderTest --tests com.example.videobrowser.rules.RuleCompilerTest`

Expected: FAIL because `loadScriptletRules`, asset constants, and the compiler parameter do not exist.

- [ ] **Step 3: Implement loader and compiler wiring**

Add `loadScriptletRules()` with:

```text
asset path: rules/scriptlet_rules.txt
cache file: scriptlet_rules.txt
parser: ScriptletRegistry.parse(...)
```

Extend `RuleCompiler.compile(requestRules, elementRules, scriptletRules = emptyList())` and create `RuleCapability.SafeHook` for validated `ScriptletRule` objects. Store the original `ScriptletRule` on the capability so domain matching can be applied later.

- [ ] **Step 4: Run tests to verify GREEN**

Run: `.\gradlew.bat testDebugUnitTest --tests com.example.videobrowser.rules.RuleFileLoaderTest --tests com.example.videobrowser.rules.RuleCompilerTest`

Expected: PASS.

- [ ] **Step 5: Commit**

```powershell
git add app/src/main/java/com/example/videobrowser/rules/RuleFileLoader.kt app/src/main/java/com/example/videobrowser/rules/RuleCompiler.kt app/src/main/java/com/example/videobrowser/rules/RuleEngineFactory.kt app/src/test/java/com/example/videobrowser/rules/RuleFileLoaderTest.kt app/src/test/java/com/example/videobrowser/rules/RuleCompilerTest.kt
git commit -m "feat: compile safe scriptlet rules"
```

### Task 3: RuleEngine And JsInjector Config Mapping

**Files:**
- Modify: `app/src/main/java/com/example/videobrowser/rules/RuleEngine.kt`
- Modify: `app/src/main/java/com/example/videobrowser/inject/JsInjector.kt`
- Test: `app/src/test/java/com/example/videobrowser/rules/RuleEngineTest.kt`
- Test: `app/src/test/java/com/example/videobrowser/inject/JsInjectorTest.kt`

- [ ] **Step 1: Write failing mapping tests**

```kotlin
@Test
fun scriptletHooksFor_filtersByPageDomainAndDeduplicatesArguments() {
    val engine = RuleEngine(
        rules = emptyList(),
        scriptletRules = listOf(
            ScriptletRule("hook:1", "fetch-block-keyword", listOf("/ad/"), domainScope = DomainScope(includedDomains = setOf("example.com"))),
            ScriptletRule("hook:2", "window-open-block-keyword", listOf("/popup/"), domainScope = DomainScope(includedDomains = setOf("example.com"))),
            ScriptletRule("hook:3", "click-skip-buttons", emptyList(), domainScope = DomainScope(includedDomains = setOf("other.com")))
        )
    )

    assertEquals(listOf("/ad/"), engine.scriptletFetchBlockedKeywordsFor("https://www.example.com/watch"))
    assertEquals(listOf("/popup/"), engine.scriptletWindowOpenBlockedKeywordsFor("https://www.example.com/watch"))
    assertFalse(engine.isScriptletSkipButtonsEnabledFor("https://www.example.com/watch"))
}

@Test
fun inject_addsScriptletHookConfigWithoutRemovingSiteScripts() {
    val evaluatedScripts = mutableListOf<String>()
    val injector = JsInjector(
        scriptLoader = scriptLoaderForSiteScripts(),
        evaluateJavascript = { script -> evaluatedScripts += script },
        siteAdapterRegistry = SiteAdapterRegistry.default(),
        ruleEngine = RuleEngine(
            rules = emptyList(),
            scriptletRules = listOf(
                ScriptletRule("hook:1", "window-open-block-keyword", listOf("/popup/"))
            )
        )
    )

    injector.inject(PageFeatureConfig(cleanupEnabled = true, videoEnabled = true), pageUrl = "https://m.youtube.com/watch?v=1")

    val script = evaluatedScripts.single()
    assertTrue(script.contains("\"scriptletWindowOpenBlockedKeywords\":[\"/popup/\"]"))
    assertTrue(script.contains("window.VideoBrowserSiteAdapters[\"youtube\"].apply(config);"))
}
```

- [ ] **Step 2: Run tests to verify RED**

Run: `.\gradlew.bat testDebugUnitTest --tests com.example.videobrowser.rules.RuleEngineTest --tests com.example.videobrowser.inject.JsInjectorTest`

Expected: FAIL because `RuleEngine` has no scriptlet rule constructor parameter and `PageFeatureConfig` has no scriptlet fields.

- [ ] **Step 3: Implement config mapping**

Extend `RuleEngine` constructor with `scriptletRules: List<ScriptletRule> = emptyList()` and add:

```kotlin
fun scriptletWindowOpenBlockedKeywordsFor(pageUrl: String?): List<String>
fun scriptletFetchBlockedKeywordsFor(pageUrl: String?): List<String>
fun isScriptletSkipButtonsEnabledFor(pageUrl: String?): Boolean
fun isScriptletVideoControlsEnabledFor(pageUrl: String?): Boolean
```

Extend `PageFeatureConfig` with:

```kotlin
val scriptletWindowOpenBlockedKeywords: List<String> = emptyList()
val scriptletFetchBlockedKeywords: List<String> = emptyList()
val scriptletSkipButtonsEnabled: Boolean = false
val scriptletVideoControlsEnabled: Boolean = false
```

Merge `RuleEngine` scriptlet values into `effectiveConfig` and JSON serialization while preserving site adapter loading.

- [ ] **Step 4: Run tests to verify GREEN**

Run: `.\gradlew.bat testDebugUnitTest --tests com.example.videobrowser.rules.RuleEngineTest --tests com.example.videobrowser.inject.JsInjectorTest`

Expected: PASS.

- [ ] **Step 5: Commit**

```powershell
git add app/src/main/java/com/example/videobrowser/rules/RuleEngine.kt app/src/main/java/com/example/videobrowser/inject/JsInjector.kt app/src/test/java/com/example/videobrowser/rules/RuleEngineTest.kt app/src/test/java/com/example/videobrowser/inject/JsInjectorTest.kt
git commit -m "feat: map scriptlets to page config"
```

### Task 4: common.js Hook Consumption And WebView Verification

**Files:**
- Modify: `app/src/main/assets/scripts/common.js`
- Test: `app/src/androidTest/java/com/example/videobrowser/inject/JsInjectorInstrumentedTest.kt`

- [ ] **Step 1: Write failing WebView instrumentation tests**

```kotlin
@Test
fun scriptletWindowOpenAndFetchKeywordsWorkWhenCleanupDisabled() {
    loadHtml(TEST_HTML)
    injectPageFeatures(
        config = PageFeatureConfig(
            cleanupEnabled = false,
            videoEnabled = false,
            scriptletWindowOpenBlockedKeywords = listOf("/popup-ad/"),
            scriptletFetchBlockedKeywords = listOf("/fetch-ad/")
        )
    )

    val result = evaluateJsonArray("""
        (function () {
          var opened = window.open('/popup-ad/landing');
          return Promise.resolve()
            .then(function () { return window.fetch('/fetch-ad/pixel'); })
            .then(function () { return [opened === null, false]; })
            .catch(function () { return [opened === null, true]; });
        })();
    """.trimIndent())

    assertTrue(result.getBoolean(0))
    assertTrue(result.getBoolean(1))
}

@Test
fun scriptletSkipButtonsCanRunWhenVideoEnhancementDisabled() {
    loadHtml(TEST_HTML)
    injectPageFeatures(
        config = PageFeatureConfig(
            cleanupEnabled = false,
            videoEnabled = false,
            scriptletSkipButtonsEnabled = true
        )
    )

    val result = evaluateJsonArray("[(window.__skipClicked === true)]")

    assertTrue(result.getBoolean(0))
}
```

- [ ] **Step 2: Run tests to verify RED**

Run: `.\gradlew.bat assembleDebugAndroidTest`

Then run: `.\gradlew.bat connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.example.videobrowser.inject.JsInjectorInstrumentedTest"`

Expected: FAIL because `common.js` does not read scriptlet-specific config fields.

- [ ] **Step 3: Implement common.js consumption**

Add keyword readers for `scriptletWindowOpenBlockedKeywords` and `scriptletFetchBlockedKeywords`. Update `window.open` and `fetch` hooks so these scriptlet keyword lists work even when `cleanupEnabled` is false. Update `clickSkipButtons()` so it runs when either `videoEnabled` or `scriptletSkipButtonsEnabled` is true. Keep existing site-specific adapter injection and existing `blockedUrlKeywords` behavior unchanged.

- [ ] **Step 4: Run tests to verify GREEN**

Run: `.\gradlew.bat assembleDebugAndroidTest`

Then run: `.\gradlew.bat connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.example.videobrowser.inject.JsInjectorInstrumentedTest"`

Expected: PASS.

- [ ] **Step 5: Commit**

```powershell
git add app/src/main/assets/scripts/common.js app/src/androidTest/java/com/example/videobrowser/inject/JsInjectorInstrumentedTest.kt
git commit -m "feat: consume safe scriptlet hooks"
```

### Task 5: G5 Documentation And Full Verification

**Files:**
- Modify: `开发目标.md`
- Modify: `开发流程与进度.md`

- [ ] **Step 1: Update G5 checklist and progress records**

Mark G5-01 through G5-06 complete, add the exact test/build commands and results, and move the next task pointer to G6.

- [ ] **Step 2: Run final verification**

Run:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
.\gradlew.bat assembleDebugAndroidTest
.\gradlew.bat connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.example.videobrowser.inject.JsInjectorInstrumentedTest"
```

Expected: all commands PASS.

- [ ] **Step 3: Commit**

```powershell
git add '开发目标.md' '开发流程与进度.md'
git commit -m "docs: 更新 G5 scriptlet 映射进度"
```

---

### Self-Review

- G5-01 is covered by `ScriptletRule`.
- G5-02 is covered by `ScriptletRegistry`.
- G5-03 is covered by `loadScriptletRules()` skipped-rule records.
- G5-04 is covered by the architecture and tests: no JavaScript source from rules is emitted into injection scripts.
- G5-05 is covered by `PageFeatureConfig` fields and `common.js` hook consumption.
- G5-06 is covered by JVM tests plus `JsInjectorInstrumentedTest`.
