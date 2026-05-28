package com.example.videobrowser.site

interface SiteAdapter {
    val profile: SiteProfile

    fun matches(url: String): Boolean

    fun scriptFiles(): List<String> {
        return profile.scriptAssetPaths
    }

    fun cssSelectors(): List<String> {
        return profile.cssSelectors
    }

    fun domSelectors(): List<String> {
        return profile.domSelectors
    }
}
