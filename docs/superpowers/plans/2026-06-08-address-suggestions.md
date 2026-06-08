# Address Suggestions Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add address-bar suggestions that prioritize local history, supplement with online query suggestions, and always provide a search fallback.

**Architecture:** Keep ranking and response parsing in pure Kotlin classes under `browser/search` so they are unit-testable. Add a small Android controller for text watching, debounced remote fetches, row rendering, and callbacks into `MainActivity`.

**Tech Stack:** Kotlin, Android Views, `SavedPageRepository`, `HttpURLConnection`, `ExecutorService`, JUnit.

---

### Task 1: Suggestion Model And Ranker

**Files:**
- Create: `app/src/main/java/com/example/videobrowser/browser/search/AddressSuggestion.kt`
- Create: `app/src/main/java/com/example/videobrowser/browser/search/AddressSuggestionRanker.kt`
- Test: `app/src/test/java/com/example/videobrowser/browser/search/AddressSuggestionRankerTest.kt`

- [ ] **Step 1: Write failing tests**

Create `AddressSuggestionRankerTest` with tests that assert:

```kotlin
@Test
fun build_historyMatchesTitleAndUrlBeforeRemoteSuggestionsAndFallback()
```

uses two `SavedPage` entries and remote keywords, types `"同"`, and expects history rows first, remote rows next, and fallback last.

```kotlin
@Test
fun build_privateModeOnlyShowsFallback()
```

uses non-empty history and remote keywords with `includePrivateSources = false`, and expects one fallback row.

```kotlin
@Test
fun build_removesDuplicateRemoteKeywords()
```

passes duplicate remote keywords with different whitespace/case and expects only one remote row before fallback.

- [ ] **Step 2: Run ranker tests and verify RED**

Run: `.\gradlew.bat testDebugUnitTest --tests com.example.videobrowser.browser.search.AddressSuggestionRankerTest`

Expected: FAIL because `AddressSuggestion` and `AddressSuggestionRanker` do not exist.

- [ ] **Step 3: Implement model and ranker**

`AddressSuggestion` should model:

```kotlin
sealed class AddressSuggestion {
    data class History(val title: String, val url: String, val displayUrl: String) : AddressSuggestion()
    data class Remote(val keyword: String) : AddressSuggestion()
    data class Fallback(val keyword: String) : AddressSuggestion()
}
```

`AddressSuggestionRanker.build()` should accept input, history pages, remote keywords, and a flag for private sources. It should trim blank input, match title/display URL case-insensitively, keep history order, dedupe history by URL, dedupe remote by normalized keyword, cap the full list to six rows, and always include fallback when input is non-blank.

- [ ] **Step 4: Run ranker tests and verify GREEN**

Run: `.\gradlew.bat testDebugUnitTest --tests com.example.videobrowser.browser.search.AddressSuggestionRankerTest`

Expected: PASS.

### Task 2: Search Suggestion Client Parsing

**Files:**
- Create: `app/src/main/java/com/example/videobrowser/browser/search/SearchSuggestionClient.kt`
- Test: `app/src/test/java/com/example/videobrowser/browser/search/SearchSuggestionClientTest.kt`

- [ ] **Step 1: Write failing parser tests**

Create `SearchSuggestionClientTest` with tests that assert:

```kotlin
@Test
fun parseOpenSearchSuggestions_readsArraySecondItem()
```

parses `["同",["同花顺","同程旅行"]]` into `["同花顺", "同程旅行"]`.

```kotlin
@Test
fun parseSo360Suggestions_readsResultWordFields()
```

parses `{"errorcode":0,"result":[{"word":"同花顺"},{"word":"同城"}]}` into `["同花顺", "同城"]`.

```kotlin
@Test
fun parseSuggestions_ignoresMalformedPayload()
```

parses invalid text into an empty list.

- [ ] **Step 2: Run parser tests and verify RED**

Run: `.\gradlew.bat testDebugUnitTest --tests com.example.videobrowser.browser.search.SearchSuggestionClientTest`

Expected: FAIL because `SearchSuggestionClient` does not exist.

- [ ] **Step 3: Implement parser and client**

Add parser functions for OpenSearch-style arrays and 360 result objects. Add provider endpoint selection:

- `baidu`: `https://suggestion.baidu.com/su?wd=<query>&action=opensearch`
- `edge`: `https://api.bing.com/osjson.aspx?query=<query>`
- `so`: `https://sug.so.360.cn/suggest?word=<query>&encodein=utf-8&encodeout=utf-8`
- `sogou`, `quark`, `uc`: use the Baidu OpenSearch-style endpoint as a stable generic keyword source, while selected searches still use the chosen provider's `searchUrlPrefix`.

The fetch method should run on an executor, use UTF-8 URL encoding, set 1500 ms connect/read timeouts, and call back with an empty list on failure.

- [ ] **Step 4: Run parser tests and verify GREEN**

Run: `.\gradlew.bat testDebugUnitTest --tests com.example.videobrowser.browser.search.SearchSuggestionClientTest`

Expected: PASS.

### Task 3: Android Suggestion Panel

**Files:**
- Create: `app/src/main/java/com/example/videobrowser/browser/search/AddressSuggestionController.kt`
- Modify: `app/src/main/res/layout/activity_main.xml`
- Modify: `app/src/main/java/com/example/videobrowser/MainActivityViews.kt`
- Modify: `app/src/main/res/values/strings.xml`

- [ ] **Step 1: Add the panel container**

Add a hidden vertical `LinearLayout` named `addressSuggestionPanel` constrained below `pageProgress`, above `searchProviderScroll`. Move `searchProviderScroll` top constraint to `addressSuggestionPanel` so home search providers remain below suggestions.

- [ ] **Step 2: Bind the new view**

Add `addressSuggestionPanel: LinearLayout` to `MainActivityViews` and bind `R.id.addressSuggestionPanel`.

- [ ] **Step 3: Implement the controller**

The controller should:

- Add a `TextWatcher` to `addressInput`.
- Render local history/fallback immediately on text change.
- Post a delayed remote request after 300 ms.
- Ignore stale remote responses by sequence number and current input.
- Hide the panel when focus is lost, input is blank, controls are hidden, video fullscreen is active, or a row is selected.
- Render at most six rows using existing icons and colors.
- Call `openUrl(url)` for history and `searchKeyword(keyword)` for remote/fallback.

- [ ] **Step 4: Add strings**

Add compact strings for fallback row text and content descriptions:

```xml
<string name="address_suggestion_search">搜索：%1$s</string>
<string name="address_suggestion_history">历史记录：%1$s</string>
<string name="address_suggestion_keyword">搜索建议：%1$s</string>
```

### Task 4: MainActivity Wiring And Verification

**Files:**
- Modify: `app/src/main/java/com/example/videobrowser/MainActivity.kt`

- [ ] **Step 1: Wire controller construction**

Create `AddressSuggestionController` after repositories and `SearchProviderController` are initialized. Pass `SavedPageRepository`, `SearchSuggestionClient`, private-browsing state, selected provider, and callbacks.

- [ ] **Step 2: Wire visibility sync**

In `syncSearchProviderVisibility()` and browser mode changes, call the suggestion controller so suggestions hide when controls are hidden or fullscreen is active.

- [ ] **Step 3: Update address submission**

Before `loadAddressInput()` navigates, hide the suggestion panel. Keep `UrlUtils.resolveAddressInput()` as the single source for URL-vs-search resolution.

- [ ] **Step 4: Run focused tests**

Run:

```powershell
.\gradlew.bat testDebugUnitTest --tests com.example.videobrowser.browser.search.AddressSuggestionRankerTest
.\gradlew.bat testDebugUnitTest --tests com.example.videobrowser.browser.search.SearchSuggestionClientTest
```

Expected: PASS.

- [ ] **Step 5: Run full verification**

Run:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```

Expected: PASS.
