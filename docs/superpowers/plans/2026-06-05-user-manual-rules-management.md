# User Manual Rules Management Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Show a manual rules management icon entry on the Feature page shortcut grid.

**Architecture:** Reuse the existing `UserManualRulesPage`. Add `USER_MANUAL_RULES` to the profile shortcut-grid catalog in normal mode, hide it in private browsing, and keep the profile data section scoped to restore defaults.

**Tech Stack:** Android Kotlin, JUnit 4.

---

### Task 1: Expose Manual Rules In Profile Shortcut Grid

**Files:**
- Modify: `app/src/test/java/com/example/videobrowser/functioncenter/FunctionCenterProfileActionCatalogTest.kt`
- Modify: `app/src/test/java/com/example/videobrowser/functioncenter/FunctionCenterDataManagementActionCatalogTest.kt`
- Modify: `app/src/main/java/com/example/videobrowser/functioncenter/FunctionCenterProfileActionCatalog.kt`
- Modify: `app/src/main/java/com/example/videobrowser/functioncenter/FunctionCenterPages.kt`
- Modify: `app/src/main/java/com/example/videobrowser/functioncenter/FunctionCenterDataManagementActionCatalog.kt`
- Create: `app/src/main/res/drawable/ic_rule_24.xml`

- [ ] **Step 1: Write the failing test**

Change profile shortcut tests to assert normal and private modes:

```kotlin
assertEquals(
    listOf("HISTORY", "BOOKMARKS", "DOWNLOADS", "FILE_OPERATIONS", "USER_MANUAL_RULES"),
    FunctionCenterProfileActionCatalog.shortcuts(isPrivateBrowsing = false)
        .map { action -> action.name }
)
assertEquals(
    listOf("HISTORY", "BOOKMARKS", "DOWNLOADS", "FILE_OPERATIONS"),
    FunctionCenterProfileActionCatalog.shortcuts(isPrivateBrowsing = true)
        .map { action -> action.name }
)
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests com.example.videobrowser.functioncenter.FunctionCenterDataManagementActionCatalogTest`

Expected: failure showing profile actions still equal only `RESTORE_DEFAULT_SETTINGS`.

- [ ] **Step 3: Write minimal implementation**

Change `shortcuts(isPrivateBrowsing: Boolean)` to return:

```kotlin
return listOfNotNull(
    FunctionCenterProfileAction.HISTORY,
    FunctionCenterProfileAction.BOOKMARKS,
    FunctionCenterProfileAction.DOWNLOADS,
    FunctionCenterProfileAction.FILE_OPERATIONS,
    FunctionCenterProfileAction.USER_MANUAL_RULES.takeIf { !isPrivateBrowsing }
)
```

Add a `USER_MANUAL_RULES` branch in `FunctionCenterPages.createProfileGridAction()`:

```kotlin
FunctionCenterGridAction(
    title = activity.getString(R.string.action_manage_user_manual_rules_short),
    summary = activity.getString(R.string.action_manage_user_manual_rules_summary),
    iconResId = R.drawable.ic_rule_24
) { userManualRulesPage.show() }
```

- [ ] **Step 4: Run test to verify it passes**

Run: `.\gradlew.bat testDebugUnitTest --tests com.example.videobrowser.functioncenter.FunctionCenterDataManagementActionCatalogTest`

Expected: test class passes.

- [ ] **Step 5: Run broader verification**

Run: `.\gradlew.bat testDebugUnitTest`

Expected: unit tests pass.
