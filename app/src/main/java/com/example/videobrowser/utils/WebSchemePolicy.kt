package com.example.videobrowser.utils

object WebSchemePolicy {
    fun isHttpScheme(scheme: String?): Boolean {
        return scheme.equals("http", ignoreCase = true)
    }

    fun isHttpsScheme(scheme: String?): Boolean {
        return scheme.equals("https", ignoreCase = true)
    }

    fun isHttpOrHttpsScheme(scheme: String?): Boolean {
        return isHttpScheme(scheme) || isHttpsScheme(scheme)
    }

    fun isWebViewLoadableScheme(scheme: String?): Boolean {
        return isHttpOrHttpsScheme(scheme) || scheme.equals("about", ignoreCase = true)
    }
}
