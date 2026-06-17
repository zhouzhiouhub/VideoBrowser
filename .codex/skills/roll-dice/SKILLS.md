---
name: roll-dice
description: Project engineering guardrails for VideoBrowser. Use when modifying this repository, especially Android/Kotlin code, to keep changes modular and prevent repeatedly adding unrelated responsibilities into one large file.
---

# VideoBrowser Engineering Rules

## Prevent Single-File Growth

- Before editing, inspect the target file's current responsibilities and nearby modules. Do not default to adding new state, callbacks, permissions, WebView behavior, persistence, navigation, or business logic into an existing entry file.
- Treat `MainActivity.kt`, fragments, adapters, view hosts, and assembly files as coordination layers. Keep them focused on lifecycle delegation, view binding, dependency wiring, and high-level orchestration.
- If a change would add a new responsibility to a file that already owns multiple responsibilities, extract the responsibility into a dedicated module before or during the implementation.
- If a file is already over 250 lines, or the planned change would add more than about 50 lines, first look for an extraction point. Prefer adding a `Controller`, `Provider`, `Store`, `Components`, or `AssemblyController` that matches existing project patterns.
- Do not hide growth by adding large private helper sections, deeply nested functions, or unrelated regions inside the same file. Moving complexity behind local functions is not modularization.
- Keep each module's constructor dependencies explicit. Pass only the collaborators it needs, and return a small components object when several related objects must be wired together.

## Android/Kotlin Placement Rules

- Keep Activity code limited to Android entry behavior: `onCreate`, lifecycle overrides, input dispatch, view binding, and calls into feature/scaffold assembly objects.
- Put WebView setup, WebView clients, navigation, request interception, fullscreen video, page actions, session state, file picking, permissions, and persistence into their own `browser` submodules or existing domain packages.
- Follow existing naming patterns such as `Browser*Controller`, `Browser*Components`, and `Browser*AssemblyController` instead of inventing a new architecture for one feature.
- Prefer top-level classes in focused files over adding unrelated inner classes to an existing large file.
- Keep package ownership clear: add code near the feature it belongs to, not near the file that happened to call it first.

## Required Workflow

- Read the current file and related modules before editing. Use `rg` or `rg --files` first when searching.
- Check `git status --short` before edits and preserve unrelated user changes.
- Make the smallest coherent extraction that reduces file responsibility while keeping behavior unchanged.
- Add or update focused contract/unit tests when wiring, lifecycle behavior, permissions, request handling, or user-visible flows change.
- Run relevant verification after changes. For this Android project, prefer `.\gradlew.bat testDebugUnitTest` when Kotlin wiring or behavior changes.
- Run `git diff --check` before finishing if files were edited.
- Do not commit automatically unless the user explicitly asks for commits or the active task already requires incremental commits.

## Documentation Rules

- Add KDoc for newly extracted public or internal coordination classes when their role is not obvious from the name.
- Document constructor parameters at parameter level for assembly/components classes, especially when dependencies are callbacks, providers, Android lifecycle objects, or delayed-initialization collaborators.
- Keep comments useful and concise. Explain ownership, lifecycle timing, delayed initialization, or non-obvious behavior; do not add comments that merely restate simple assignments.

## Stop Conditions

- If the direct implementation path keeps growing one large file, stop and modularize first.
- If there is no clear target module, create a small focused module with explicit responsibilities rather than appending to an entry file.
- If modularization could change behavior in a risky way, preserve the old behavior with tests before moving code.
