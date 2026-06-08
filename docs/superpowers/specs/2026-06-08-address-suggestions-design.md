# Address Bar Suggestions Design

## Goal

When the user types in the address bar, show useful suggestions before they submit:

- Prefer local browsing history when the input matches a saved title or URL.
- Fetch online search suggestions for the current search provider as a secondary source.
- Always provide a search fallback for the exact typed text.

The feature must keep the address bar responsive and must not make navigation depend on network suggestions.

## Current Context

`MainActivity` owns address submission through `loadAddressInput()`, which delegates URL/search parsing to `UrlUtils.resolveAddressInput()`.

History is stored through `SavedPageRepository.history()`. Normal browsing adds history through `PageActionsController.addHistoryEntry()`. Private browsing does not add history.

Search providers live in `browser/search/SearchProviderController` and `SearchProvider`. The app already has `INTERNET` permission and no dedicated HTTP client dependency.

## Behavior

While the address input has focus and contains non-blank text, the app shows a suggestion panel below the top bar.

Suggestion ordering:

1. Matching local history entries.
2. Online search suggestion keywords.
3. A fallback row that searches the exact input.

History matching checks both page title and display URL case-insensitively. History keeps repository order, which is most recent first. Duplicate suggestions are removed by URL for history rows and by normalized keyword for search rows.

Selecting a history suggestion opens that saved URL directly. Selecting an online suggestion searches that keyword with the selected provider. Selecting the fallback searches the typed input with the selected provider.

The panel hides when:

- The address input loses focus.
- The input is blank.
- A suggestion is selected.
- Browser controls are hidden.
- Video fullscreen UI is active.

## Privacy And Network Rules

Private browsing disables history suggestions and online suggestions. Only the local search fallback is shown while the user types, and no typed text is sent to a suggestion endpoint.

Online suggestions are best effort:

- Requests are debounced by about 300 ms.
- Requests run off the UI thread.
- Network timeout is short, about 1500 ms.
- Stale responses are ignored if the user has already typed something else.
- Failure, timeout, empty response, or unexpected response format leaves local history and fallback suggestions visible without showing an error.

Because the project has no OkHttp or coroutine dependency, implementation should use platform APIs: an `ExecutorService`, main-thread posting, and `HttpURLConnection`.

## Architecture

Add a small search suggestion package under `browser/search`.

`AddressSuggestion`
: A sealed/data model describing history, remote keyword, and fallback rows.

`AddressSuggestionRanker`
: Pure Kotlin logic that builds ordered suggestions from input, history pages, remote keywords, and selected search provider. It is unit-testable without Android UI.

`SearchSuggestionClient`
: Fetches and parses online suggestions for the selected search provider. It exposes a callback-based API and isolates provider-specific endpoint details.

`AddressSuggestionController`
: Owns the suggestion panel UI and address input watchers. It reads history from `SavedPageRepository`, asks `SearchSuggestionClient` for remote suggestions, renders rows, and invokes callbacks to open URLs or search keywords.

`MainActivity`
: Wires the controller with `SavedPageRepository`, `SearchProviderController`, `loadUrl()`, `UrlUtils.resolveAddressInput()`, browser-control visibility, and private browsing state.

## UI

The suggestion panel is a lightweight vertical list placed below `pageProgress` and above the home/search-provider area. It should match the existing browser surface colors and use compact rows.

History rows show:

- History icon.
- Page title.
- Display URL as secondary text.

Search suggestion rows show:

- Search icon.
- Suggested keyword.

Fallback row shows:

- Search icon.
- `搜索：<typed text>`.

The panel should cap visible rows at six to avoid covering too much of the home page.

## Testing

Add unit tests for `AddressSuggestionRanker`:

- History matches title and URL.
- History rows come before remote suggestions and fallback.
- Duplicate remote suggestions are removed.
- Fallback is present when input is non-blank.
- Private mode excludes history and remote suggestions.

Add unit tests for response parsing in `SearchSuggestionClient` where parsing is pure enough to test without network.

Run existing unit tests after implementation.

## Out Of Scope

- Persisting typed queries separately from browsing history.
- Adding a setting toggle for online suggestions.
- Supporting paid or key-based suggestion APIs.
- Showing visual loading indicators for remote suggestions.
