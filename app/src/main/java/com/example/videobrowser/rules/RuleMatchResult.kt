package com.example.videobrowser.rules

data class RuleMatchResult(
    val matched: Boolean,
    val action: RuleAction = RuleAction.NONE,
    val rule: Rule? = null
) {
    init {
        if (!matched) {
            require(action == RuleAction.NONE) { "Unmatched result must use NONE action." }
            require(rule == null) { "Unmatched result must not carry a rule." }
        } else {
            require(action != RuleAction.NONE) { "Matched result must use ALLOW or BLOCK." }
            require(rule != null) { "Matched result must carry a rule." }
        }
    }

    val shouldBlock: Boolean
        get() = matched && action == RuleAction.BLOCK

    val shouldAllow: Boolean
        get() = matched && action == RuleAction.ALLOW

    companion object {
        val NoMatch = RuleMatchResult(matched = false)

        fun block(rule: Rule): RuleMatchResult {
            return RuleMatchResult(
                matched = true,
                action = RuleAction.BLOCK,
                rule = rule
            )
        }

        fun allow(rule: Rule): RuleMatchResult {
            return RuleMatchResult(
                matched = true,
                action = RuleAction.ALLOW,
                rule = rule
            )
        }
    }
}
