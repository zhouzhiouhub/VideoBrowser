package com.example.videobrowser.adblock

class AdBlockLogger(
    private val maxEntries: Int = DEFAULT_MAX_ENTRIES,
    private val clock: () -> Long = { System.currentTimeMillis() }
) {
    private val entries = ArrayDeque<AdBlockLogEntry>()

    fun log(
        action: AdBlockLogAction,
        url: String,
        host: String?,
        decision: AdBlockDecision
    ) {
        val rule = decision.ruleMatchResult.rule
        log(
            AdBlockLogEntry(
                timestampMillis = clock(),
                action = action,
                url = url,
                host = host,
                reason = decision.reason,
                ruleId = rule?.id,
                ruleSource = rule?.source,
                rulePattern = rule?.pattern
            )
        )
    }

    fun log(entry: AdBlockLogEntry) {
        entries.addFirst(entry)
        while (entries.size > maxEntries) {
            entries.removeLast()
        }
    }

    fun entries(): List<AdBlockLogEntry> {
        return entries.toList()
    }

    fun clear() {
        entries.clear()
    }

    companion object {
        private const val DEFAULT_MAX_ENTRIES = 80
    }
}
