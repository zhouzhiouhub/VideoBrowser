package com.example.videobrowser.rules

enum class RuleType {
    URL_CONTAINS,
    URL_PATTERN,
    DOMAIN_CONTAINS
}

enum class RuleAction {
    ALLOW,
    BLOCK,
    NONE
}
