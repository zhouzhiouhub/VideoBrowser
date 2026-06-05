# User Manual Rules Management Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Show a manual rules management entry on the Feature page.

**Architecture:** Reuse the existing `UserManualRulesPage` and data-management action routing. Change the profile data action catalog so the Feature page renders the existing rule manager entry before reset in normal mode and hides it in private browsing.

**Tech Stack:** Android Kotlin, JUnit 4.

---

### Task 1: Expose Manual Rules In Profile Data Actions

**Files:**
- Modify: `app/src/test/java/com/example/videobrowser/functioncenter/FunctionCenterDataManagementActionCatalogTest.kt`
- Modify: `app/src/main/java/com/example/videobrowser/functioncenter/FunctionCenterDataManagementActionCatalog.kt`
- Modify: `app/src/main/java/com/example/videobrowser/functioncenter/BrowserSettingsPage.kt`

- [ ] **Step 1: Write the failing test**

Change `profileDataManagementOnlyShowsRestoreDefaultSettings` to assert normal and private modes:

```kotlin
assertEquals(
    listOf("USER_MANUAL_RULES", "RESTORE_DEFAULT_SETTINGS"),
    FunctionCenterDataManagementActionCatalog.profileActions(isPrivateBrowsing = false)
        .map { action -> action.name }
)
assertEquals(
    listOf("RESTORE_DEFAULT_SETTINGS"),
    FunctionCenterDataManagementActionCatalog.profileActions(isPrivateBrowsing = true)
        .map { action -> action.name }
)
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests com.example.videobrowser.functioncenter.FunctionCenterDataManagementActionCatalogTest`

Expected: failure showing profile actions still equal only `RESTORE_DEFAULT_SETTINGS`.

- [ ] **Step 3: Write minimal implementation**

Change `profileActions(isPrivateBrowsing: Boolean)` to return:

```kotlin
return listOfNotNull(
    FunctionCenterDataManagementAction.USER_MANUAL_RULES.takeIf { !isPrivateBrowsing },
    FunctionCenterDataManagementAction.RESTORE_DEFAULT_SETTINGS
)
```

Change `BrowserSettingsPage.addProfileDataManagement()` to call:

```kotlin
FunctionCenterDataManagementActionCatalog.profileActions(isPrivateBrowsingEnabled())
```

- [ ] **Step 4: Run test to verify it passes**

Run: `.\gradlew.bat testDebugUnitTest --tests com.example.videobrowser.functioncenter.FunctionCenterDataManagementActionCatalogTest`

Expected: test class passes.

- [ ] **Step 5: Run broader verification**

Run: `.\gradlew.bat testDebugUnitTest`

Expected: unit tests pass.
