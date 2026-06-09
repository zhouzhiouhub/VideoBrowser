# G3 Rule Decision Logging Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Complete G3 by centralizing request decision priority and making ad-block logs explain final decisions, candidate rules, override reasons, and recovery actions.

**Architecture:** Add a small `RuleDecisionResolver` in `adblock/` that owns the priority order from `开发目标.md`. Extend `RuleEngine` with a diagnostic request match summary so the resolver can log both explicit allow and block candidates even when a user whitelist or site toggle overrides them. Keep UI changes limited to the existing ad-block log page.

**Tech Stack:** Kotlin, Android WebView request interception, existing JUnit local tests, Gradle `testDebugUnitTest` and `assembleDebug`.

---

### Task 1: Rule Decision Resolver

**Files:**
- Create: `app/src/main/java/com/example/videobrowser/adblock/RuleDecisionResolver.kt`
- Modify: `app/src/main/java/com/example/videobrowser/adblock/AdBlockDecision.kt`
- Modify: `app/src/main/java/com/example/videobrowser/adblock/AdBlockRequestPolicy.kt`
- Modify: `app/src/main/java/com/example/videobrowser/rules/RuleEngine.kt`
- Test: `app/src/test/java/com/example/videobrowser/adblock/RuleDecisionResolverTest.kt`
- Test: `app/src/test/java/com/example/videobrowser/adblock/AdBlockRequestPolicyTest.kt`

- [ ] **Step 1: Write the failing resolver tests**

```kotlin
@Test
fun resolve_userWhitelistOverridesAllowAndBlockCandidates() {
    val resolver = RuleDecisionResolver()
    val allowRule = Rule.allowUrlContains("/safe-ad/", id = "allow:safe", source = "test")
    val blockRule = Rule.blockUrlContains("/safe-ad/", id = "block:safe", source = "test")

    val decision = resolver.resolve(
        RuleDecisionResolver.Input(
            enabled = true,
            userWhitelisted = true,
            siteAdBlockDisabled = false,
            context = subresourceContext("https://ads.test/safe-ad/script.js"),
            ruleSummary = RequestRuleMatchSummary(
                allowMatch = RuleMatchResult.allow(allowRule),
                blockMatch = RuleMatchResult.block(blockRule)
            )
        )
    )

    assertFalse(decision.shouldBlock)
    assertEquals(AdBlockDecisionReason.USER_WHITELISTED, decision.reason)
    assertEquals(AdBlockDecisionReason.RULE_BLOCKED, decision.overrideReason)
    assertEquals(listOf("allow:safe", "block:safe"), decision.candidateRules.map { it.id })
}

@Test
fun resolve_usesDocumentedPriorityForSiteDisabledAllowForceAndBlock() {
    val resolver = RuleDecisionResolver()
    val allowRule = Rule.allowUrlContains("/ad.js", id = "allow:ad", source = "test")
    val forceRule = Rule.blockUrlContains("/ad.js", id = "force:ad", source = "test")
    val blockRule = Rule.blockUrlContains("/ad.js", id = "block:ad", source = "test")

    val siteDecision = resolver.resolve(
        RuleDecisionResolver.Input(
            enabled = true,
            userWhitelisted = false,
            siteAdBlockDisabled = true,
            context = subresourceContext("https://ads.test/ad.js"),
            ruleSummary = RequestRuleMatchSummary(
                allowMatch = RuleMatchResult.allow(allowRule),
                forceBlockMatch = RuleMatchResult.block(forceRule),
                blockMatch = RuleMatchResult.block(blockRule)
            )
        )
    )
    assertFalse(siteDecision.shouldBlock)
    assertEquals(AdBlockDecisionReason.SITE_AD_BLOCK_DISABLED, siteDecision.reason)

    val allowDecision = resolver.resolve(
        RuleDecisionResolver.Input(
            enabled = true,
            userWhitelisted = false,
            siteAdBlockDisabled = false,
            context = subresourceContext("https://ads.test/ad.js"),
            ruleSummary = RequestRuleMatchSummary(
                allowMatch = RuleMatchResult.allow(allowRule),
                forceBlockMatch = RuleMatchResult.block(forceRule),
                blockMatch = RuleMatchResult.block(blockRule)
            )
        )
    )
    assertFalse(allowDecision.shouldBlock)
    assertEquals(AdBlockDecisionReason.RULE_ALLOWED, allowDecision.reason)

    val forceDecision = resolver.resolve(
        RuleDecisionResolver.Input(
            enabled = true,
            userWhitelisted = false,
            siteAdBlockDisabled = false,
            context = subresourceContext("https://ads.test/ad.js"),
            ruleSummary = RequestRuleMatchSummary(
                forceBlockMatch = RuleMatchResult.block(forceRule),
                blockMatch = RuleMatchResult.block(blockRule)
            )
        )
    )
    assertTrue(forceDecision.shouldBlock)
    assertEquals(AdBlockDecisionReason.FORCE_RULE_BLOCKED, forceDecision.reason)
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.videobrowser.adblock.RuleDecisionResolverTest"`

Expected: FAIL because `RuleDecisionResolver`, `RequestRuleMatchSummary`, `forceBlockMatch`, `overrideReason`, `candidateRules`, and `FORCE_RULE_BLOCKED` do not exist yet.

- [ ] **Step 3: Implement the resolver and diagnostic summary**

Add `RequestRuleMatchSummary` next to `RuleMatchResult`, expose `RuleEngine.matchRequestSummary(context)`, add resolver input/output fields to `AdBlockDecision`, and change `AdBlockRequestPolicy.evaluate()` to call the resolver. The priority order is:

1. global disabled
2. main frame protection
3. non-HTTP scheme protection
4. user whitelist
5. current site disabled
6. explicit allow rule
7. force block rule
8. normal block rule
9. no match allow

- [ ] **Step 4: Run focused tests**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.videobrowser.adblock.RuleDecisionResolverTest" --tests "com.example.videobrowser.adblock.AdBlockRequestPolicyTest"`

Expected: PASS.

- [ ] **Step 5: Commit**

Run:

```powershell
git add app/src/main/java/com/example/videobrowser/adblock app/src/main/java/com/example/videobrowser/rules app/src/test/java/com/example/videobrowser/adblock
git commit -m "feat: 集中广告请求决策优先级"
```

### Task 2: Explainable Ad-Block Logs

**Files:**
- Modify: `app/src/main/java/com/example/videobrowser/adblock/AdBlockLogEntry.kt`
- Modify: `app/src/main/java/com/example/videobrowser/adblock/AdBlockLogger.kt`
- Modify: `app/src/main/java/com/example/videobrowser/adblock/AdBlockDecision.kt`
- Modify: `app/src/main/java/com/example/videobrowser/functioncenter/AdBlockLogPage.kt`
- Test: `app/src/test/java/com/example/videobrowser/adblock/AdBlockLoggerTest.kt`

- [ ] **Step 1: Write the failing logger tests**

```kotlin
@Test
fun log_recordsFinalDecisionCandidatesAndOverrideReason() {
    val logger = AdBlockLogger(clock = { 42L })
    val blockRule = Rule.blockUrlContains("/ad.js", id = "block:ad", source = "test-source")
    val decision = AdBlockDecision.allow(
        reason = AdBlockDecisionReason.USER_WHITELISTED,
        overrideReason = AdBlockDecisionReason.RULE_BLOCKED,
        candidateRules = listOf(blockRule)
    )

    logger.log(AdBlockLogAction.ALLOW, "https://ads.test/ad.js", "ads.test", decision)

    val entry = logger.entries().single()
    assertEquals(AdBlockDecisionReason.USER_WHITELISTED, entry.finalReason)
    assertEquals(AdBlockDecisionReason.RULE_BLOCKED, entry.overrideReason)
    assertEquals(listOf("block:ad"), entry.candidateRuleIds)
    assertEquals(listOf("test-source"), entry.candidateRuleSources)
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.videobrowser.adblock.AdBlockLoggerTest"`

Expected: FAIL because the new log fields do not exist.

- [ ] **Step 3: Extend log model and page summaries**

Keep existing `reason`, `ruleId`, `ruleSource`, and `rulePattern` for compatibility. Add `finalReason`, `overrideReason`, `candidateRuleIds`, `candidateRuleSources`, and `candidateRulePatterns` with defaults. Populate them in `AdBlockLogger.log()`. In `AdBlockLogPage`, show the final reason, override reason if present, and compact candidate count/source text in the row summary.

- [ ] **Step 4: Run focused tests**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.videobrowser.adblock.AdBlockLoggerTest"`

Expected: PASS.

- [ ] **Step 5: Commit**

Run:

```powershell
git add app/src/main/java/com/example/videobrowser/adblock app/src/main/java/com/example/videobrowser/functioncenter app/src/test/java/com/example/videobrowser/adblock
git commit -m "feat: 记录广告拦截决策诊断信息"
```

### Task 3: False-Positive Recovery Regression And Documentation

**Files:**
- Modify: `app/src/main/java/com/example/videobrowser/functioncenter/AdBlockLogPage.kt`
- Test: `app/src/test/java/com/example/videobrowser/adblock/AdBlockRequestPolicyTest.kt`
- Modify: `开发目标.md`
- Modify: `开发流程与进度.md`

- [ ] **Step 1: Write regression tests for false-positive recovery**

Add or extend tests so a matching block candidate becomes allowed when the user whitelist is active and when the current site toggle is disabled. Assert the final reason and override reason are logged in `AdBlockDecision`.

- [ ] **Step 2: Run tests to verify failures before implementation**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.example.videobrowser.adblock.AdBlockRequestPolicyTest"`

Expected: FAIL until Task 1 output fields are wired through policy.

- [ ] **Step 3: Add recovery action labels where the log already supports actions**

Keep the existing blocked-row click behavior that adds the request host to the user whitelist. For allowed rows caused by `SITE_AD_BLOCK_DISABLED`, keep them informational because restoring site interception is already available from the current-site settings page and should not be triggered for historical entries from another site.

- [ ] **Step 4: Run full local verification**

Run:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```

Expected: both commands pass.

- [ ] **Step 5: Update Markdown progress and commit**

Mark G3-01 through G3-05 complete in `开发目标.md`, add verification rows for `testDebugUnitTest` and `assembleDebug`, and update the current pointer in `开发流程与进度.md` to G3 complete / next stage G4. Commit:

```powershell
git add 开发目标.md 开发流程与进度.md app/src/main/java/com/example/videobrowser app/src/test/java/com/example/videobrowser
git commit -m "docs: 更新 G3 决策日志进度"
```
