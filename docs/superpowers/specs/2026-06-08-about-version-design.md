# About Version Design

## Goal

Add an About feature in the function page so users can see the app version.

The version must be derived from the number of Git commits and displayed as a four-part decimal version:

- `0` commits -> `0.0.0.0`
- `1` commit -> `0.0.0.1`
- `9` commits -> `0.0.0.9`
- `10` commits -> `0.0.1.0`
- `100` commits -> `0.1.0.0`
- `1000` commits -> `1.0.0.0`

The last three parts carry at ten, so the format is the commit count represented in base 10 across four visible segments.

## Current Context

`app/build.gradle.kts` currently sets fixed Android version metadata:

- `versionCode = 1`
- `versionName = "1.0"`

The function page shortcut grid is driven by `FunctionCenterProfileActionCatalog.shortcuts()` and rendered by `FunctionCenterPages.createProfileGridAction()`.

Existing page UI helpers already support simple information rows through `FunctionCenterPageHost.addInfoRow()`.

## Behavior

The function page shortcut grid adds an About entry immediately after the existing manual blocking rules entry.

Tapping About opens a function-center page titled `关于`. The page shows:

- App name.
- Current four-part version number.
- Git commit count used to produce that version.

The About page is available in normal browsing and private browsing because it does not expose browsing data.

## Version Source

At Gradle configuration time, the app module asks Git for the current commit count using:

`git rev-list --count HEAD`

That count is used as Android `versionCode` and formatted into `versionName`.

If Git is unavailable, the count falls back to `0` for formatting. Because Android `versionCode` must be positive for installable builds, `versionCode` uses at least `1`; `versionName` still follows the formatter output.

## Architecture

Add a small version utility in app code:

`AppVersionFormatter`
: Converts a non-negative commit count into the four-part version string.

Gradle keeps a matching formatter for build metadata so the generated Android package version matches app logic.

`AboutPage`
: Builds the About screen using the existing function-center host helpers and reads package version metadata from Android package info.

`FunctionCenterProfileActionCatalog`
: Adds `ABOUT` after `USER_MANUAL_RULES`.

`FunctionCenterPages`
: Wires the new profile action to `AboutPage.show()`.

## Testing

Add unit tests for `AppVersionFormatter`:

- Zero commit count formats as `0.0.0.0`.
- Single digit counts stay in the last segment.
- Counts carry from patch to minor at ten.
- Counts carry through the visible four segments.

Update `FunctionCenterProfileActionCatalogTest` so the function page order is:

`HISTORY`, `BOOKMARKS`, `DOWNLOADS`, `FILE_OPERATIONS`, `USER_MANUAL_RULES`, `ABOUT`.

Run the relevant unit tests and a Gradle build verification after implementation.

## Out Of Scope

- Editing release names manually.
- Showing changelog content.
- Adding a separate settings-page About entry.
