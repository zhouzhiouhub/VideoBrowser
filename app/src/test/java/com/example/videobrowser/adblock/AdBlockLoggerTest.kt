package com.example.videobrowser.adblock

import com.example.videobrowser.rules.Rule
import com.example.videobrowser.rules.RuleMatchResult
import org.junit.Assert.assertEquals
import org.junit.Test

class AdBlockLoggerTest {
    @Test
    fun log_keepsNewestEntriesWithinLimit() {
        var now = 1000L
        val logger = AdBlockLogger(
            maxEntries = 2,
            clock = { now++ }
        )
        val decision = AdBlockDecision.blockByRule(
            RuleMatchResult.block(
                Rule.blockUrlContains(
                    pattern = "/pagead/",
                    id = "test:block",
                    source = "test-source"
                )
            )
        )

        logger.log(AdBlockLogAction.BLOCK, "https://a.test/pagead/1.js", "a.test", decision)
        logger.log(AdBlockLogAction.BLOCK, "https://b.test/pagead/2.js", "b.test", decision)
        logger.log(AdBlockLogAction.BLOCK, "https://c.test/pagead/3.js", "c.test", decision)

        val entries = logger.entries()
        assertEquals(2, entries.size)
        assertEquals("c.test", entries[0].host)
        assertEquals("b.test", entries[1].host)
        assertEquals("test:block", entries[0].ruleId)
        assertEquals("test-source", entries[0].ruleSource)
    }

    @Test
    fun clear_removesEntries() {
        val logger = AdBlockLogger()
        logger.log(
            AdBlockLogEntry(
                timestampMillis = 1L,
                action = AdBlockLogAction.ALLOW,
                url = "https://example.com/",
                host = "example.com",
                reason = AdBlockDecisionReason.USER_WHITELISTED,
                ruleId = null,
                ruleSource = null,
                rulePattern = null
            )
        )

        logger.clear()

        assertEquals(emptyList<AdBlockLogEntry>(), logger.entries())
    }
}
