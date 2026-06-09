# G4 Synthetic Response Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add safe built-in noop / redirect responses for request rules without supporting arbitrary remote redirect resources.

**Architecture:** Keep response selection in `adblock/` and rule parsing in `rules/`. Rules may carry a safe `$redirect=<name>` resource name; only registry-approved names produce a synthetic response. Main-frame requests remain allowed before rule matching, and unknown redirect resources are skipped by the existing rule loading/compile diagnostics path.

**Tech Stack:** Kotlin, Android `WebResourceResponse`, existing `RuleEngine`, local JUnit tests, Android instrumented tests, Gradle `testDebugUnitTest`, `assembleDebug`, and targeted `connectedDebugAndroidTest`.

---

### Task 1: Built-In Synthetic Response Registry

**Files:**
- Create: `app/src/main/java/com/example/videobrowser/adblock/SyntheticResponseSpec.kt`
- Create: `app/src/main/java/com/example/videobrowser/adblock/SyntheticResponseRegistry.kt`
- Create: `app/src/main/java/com/example/videobrowser/adblock/SyntheticResponseFactory.kt`
- Test: `app/src/test/java/com/example/videobrowser/adblock/SyntheticResponseRegistryTest.kt`

- [ ] **Step 1: Write failing registry tests**

```kotlin
@Test
fun get_returnsOnlyBuiltInNoopResources() {
    val registry = SyntheticResponseRegistry()

    assertEquals("application/javascript", registry.get("noopjs")?.mimeType)
    assertEquals("text/css", registry.get("noopcss")?.mimeType)
    assertEquals("text/plain", registry.get("nooptext")?.mimeType)
    assertNull(registry.get("https://evil.test/payload.js"))
    assertNull(registry.get("unknown"))
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.videobrowser.adblock.SyntheticResponseRegistryTest"`

Expected: FAIL because the registry and spec classes do not exist.

- [ ] **Step 3: Implement registry and pure spec**

Create immutable specs for `noopjs`, `noopcss`, and `nooptext`. Each spec contains resource name, MIME type, UTF-8 encoding, status `200`, reason `OK`, and a small safe body. Reject blank, unknown, and URL-like names.

- [ ] **Step 4: Run focused test**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.videobrowser.adblock.SyntheticResponseRegistryTest"`

Expected: PASS.

- [ ] **Step 5: Commit**

Run:

```powershell
git add app/src/main/java/com/example/videobrowser/adblock app/src/test/java/com/example/videobrowser/adblock
git commit -m "feat: 添加内置 noop 响应注册表"
```

### Task 2: Redirect Rule Parsing And Matching

**Files:**
- Modify: `app/src/main/java/com/example/videobrowser/rules/Rule.kt`
- Modify: `app/src/main/java/com/example/videobrowser/rules/RuleCompiler.kt`
- Test: `app/src/test/java/com/example/videobrowser/rules/RuleCompilerTest.kt`
- Test: `app/src/test/java/com/example/videobrowser/rules/RuleFileLoaderTest.kt`
- Test: `app/src/test/java/com/example/videobrowser/rules/RuleEngineTest.kt`

- [ ] **Step 1: Write failing rule tests**

```kotlin
@Test
fun fromRequestRuleText_parsesSupportedRedirectResource() {
    val rule = requireNotNull(Rule.fromRequestRuleText("||ads.example.com^\$redirect=noopjs"))

    assertEquals("noopjs", rule.redirectResourceName)
    assertEquals(RuleAction.BLOCK, rule.action)
}

@Test
fun fromRequestRuleText_rejectsUnknownRedirectResource() {
    assertNull(Rule.fromRequestRuleText("||ads.example.com^\$redirect=https://evil.test/a.js"))
    assertNull(Rule.fromRequestRuleText("||ads.example.com^\$redirect=unknown"))
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.videobrowser.rules.RuleEngineTest" --tests "com.example.videobrowser.rules.RuleFileLoaderTest" --tests "com.example.videobrowser.rules.RuleCompilerTest"`

Expected: FAIL because `$redirect=` is currently rejected.

- [ ] **Step 3: Add safe redirect metadata**

Add `redirectResourceName: String?` to `Rule`. `parseRequestOptions()` accepts only `redirect=noopjs`, `redirect=noopcss`, and `redirect=nooptext`; unknown redirect resources return null so `RuleFileLoader` records the skipped rule. `RuleCompiler` creates a `RuleCapability.NoopResponse` for supported redirect rules and keeps the request rule matchable so the interceptor can select the synthetic response.

- [ ] **Step 4: Run focused tests**

Run the same focused command.

Expected: PASS.

- [ ] **Step 5: Commit**

Run:

```powershell
git add app/src/main/java/com/example/videobrowser/rules app/src/test/java/com/example/videobrowser/rules
git commit -m "feat: 解析安全 redirect 规则"
```

### Task 3: Interceptor Synthetic Response Selection

**Files:**
- Modify: `app/src/main/java/com/example/videobrowser/adblock/AdBlockRequestInterceptor.kt`
- Modify: `app/src/main/java/com/example/videobrowser/adblock/SyntheticResponseFactory.kt`
- Test: `app/src/androidTest/java/com/example/videobrowser/adblock/AdBlockRequestInterceptorInstrumentedTest.kt`

- [ ] **Step 1: Write failing instrumented test**

```kotlin
@Test
fun intercept_returnsNoopJsResponseForSupportedRedirectRule() {
    val ruleEngine = RuleEngine(
        listOf(requireNotNull(Rule.fromRequestRuleText("||ads.example.com^\$redirect=noopjs")))
    )
    val interceptor = AdBlockRequestInterceptor(AdBlockManager(ruleEngine = ruleEngine))
    val request = BrowserRequest.from(
        uri = Uri.parse("https://ads.example.com/script.js"),
        isForMainFrame = false
    )

    val response = requireNotNull(interceptor.intercept(request))

    assertEquals(200, response.statusCode)
    assertEquals("OK", response.reasonPhrase)
    assertEquals("application/javascript", response.mimeType)
    assertEquals("utf-8", response.encoding)
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\gradlew.bat connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.videobrowser.adblock.AdBlockRequestInterceptorInstrumentedTest`

Expected: FAIL until the interceptor returns synthetic responses for redirect rules.

- [ ] **Step 3: Implement response selection**

If `AdBlockDecision.shouldBlock` is false, return null. If the matched rule has `redirectResourceName` and the request is not a main frame, use `SyntheticResponseFactory` to create a `WebResourceResponse`; otherwise keep `EmptyResponseFactory.noContent()`. Main-frame protection remains in `AdBlockRequestPolicy`.

- [ ] **Step 4: Run focused instrumented test**

Run the same connected test command.

Expected: PASS on an available device or emulator. If no device is available, run `assembleDebugAndroidTest` and record the device limitation in Markdown.

- [ ] **Step 5: Commit**

Run:

```powershell
git add app/src/main/java/com/example/videobrowser/adblock app/src/androidTest/java/com/example/videobrowser/adblock
git commit -m "feat: 接入内置 noop 响应"
```

### Task 4: G4 Verification And Documentation

**Files:**
- Modify: `开发目标.md`
- Modify: `开发流程与进度.md`

- [ ] **Step 1: Run full verification**

Run:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
.\gradlew.bat assembleDebugAndroidTest
```

Run targeted connected instrumented test if a device is available.

- [ ] **Step 2: Update Markdown**

Mark G4-01 through G4-05 complete only if the instrumented test runs. If no device is available, mark G4-05 blocked with the exact command and limitation, while G4-01 through G4-04 can be complete.

- [ ] **Step 3: Commit**

Run:

```powershell
git add 开发目标.md 开发流程与进度.md
git commit -m "docs: 更新 G4 noop 响应进度"
```
