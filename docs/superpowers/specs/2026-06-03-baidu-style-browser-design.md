# Baidu Style Browser Design

## Goal

Restyle the current native browser shell to follow Baidu mobile browser patterns while keeping the existing WebView browsing engine and feature controllers.

## Scope

- Default home and default search provider use Baidu.
- Native top bar uses a Baidu-like rounded search field with blue stroke and compact icons.
- Native bottom bar follows the Baidu browsing layout: back, menu, Wenxin pill, tabs/home, and avatar.
- The menu button opens a Baidu-like half sheet with login/profile header, icon grid actions, and browsing history preview.
- The avatar button opens an independent full page, not a half sheet.

## Out Of Scope

- Replacing WebView content with a custom search page.
- Implementing multi-tab browsing.
- Changing adblock, video enhancement, file browsing, or download behavior beyond menu entry placement.

## Architecture

Keep changes in the existing Android native UI layer. `activity_main.xml` defines the shell structure, `BrowserControlsController` wires bottom-bar behavior, `FunctionCenterPages` builds the menu and profile page, and `SettingsManager`/`SearchProviderController` provide Baidu defaults.

## Testing

Unit tests cover default search/home settings. Android build verifies resource, layout, and Kotlin wiring.
