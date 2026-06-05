# User Manual Rules Management Design

## Goal

Expose management for user-added element blocking rules from the existing Feature page shortcut grid.

## Scope

- Add a visible icon entry for manual element blocking rules in the Feature page's top browser shortcut grid.
- Hide the entry while private browsing is enabled, matching other persistent user-data actions.
- Reuse the existing manual rules page for listing saved rules.
- Keep existing management actions: remove one rule, clear all rules.
- Reload the current page after removal or clearing so the changed rules take effect.

## Out Of Scope

- Editing selector text.
- Manually typing new host and selector rules.
- Changing built-in request, CSS, or DOM rule files.

## UI

The entry uses the existing shortcut-grid style and strings:

- Grid title: "屏蔽规则"
- Page title: "手动屏蔽规则"
- Summary: "查看或删除手动添加的元素屏蔽规则"
- Icon: rule/checklist vector icon.

## Data

Rules stay in SharedPreferences file `browser_preferences`, key `user_element_hide_rules`, with one `host<TAB>selector` rule per line.

## Testing

Update function-center catalog tests to require the profile shortcut grid to expose `USER_MANUAL_RULES` in normal mode and hide it in private browsing. The profile data section stays scoped to `RESTORE_DEFAULT_SETTINGS`.
