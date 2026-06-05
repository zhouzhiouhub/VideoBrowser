# Download Feature Design

## Goal

Add a download feature page to VideoBrowser so downloads triggered from the browser are recorded in the app's function center while actual files continue to be saved by Android DownloadManager into the public Downloads directory.

## Scope

- Keep using Android `DownloadManager` for browser downloads.
- Save a local download record when a download is successfully enqueued.
- Add a "Downloads" entry to the function center root and profile shortcuts.
- Add a downloads page that lists recorded downloads, opens downloaded files through the system, and clears the local record list.
- Preserve current behavior that playable media URLs open in the native player instead of being downloaded.

## Out Of Scope

- Custom byte-stream downloader.
- In-app progress polling or pause/resume controls.
- Moving files out of the public Downloads directory.
- Downloading DRM, streaming-only, or site-protected media that cannot be handled by `DownloadManager`.

## Architecture

`DownloadController` remains the single browser download entry point. It resolves the file name, builds the `DownloadManager.Request`, enqueues it, and records metadata through a new `DownloadRecordRepository`.

`DownloadRecordRepository` stores a capped, escaped line-based record list in `PreferenceStore`. This keeps the repository testable in local JVM tests, where Android's `org.json` stub is not usable. Each record contains the download id, title, source URL, local Downloads file name, MIME type, and timestamp.

`DownloadsPage` renders the records inside the existing `FunctionCenterPageHost` UI. Opening a record uses a content URI from `DownloadManager.getUriForDownloadedFile(id)` when available and falls back to a public Downloads file URI intent only where the platform allows it.

## Error Handling

If the URL is blank, keep showing the existing download failure toast. If `DownloadManager.enqueue` fails, fall back to opening the URL externally as current code does and do not add a record. If a recorded file is no longer available, show a short failure toast and keep the record so the user can clear the list.

## Testing

Unit tests cover download record persistence, newest-first ordering, duplicate replacement by download id, limit trimming, corrupted JSON recovery, root action catalog ordering, and profile shortcut ordering. Build verification checks Kotlin wiring and resources.
