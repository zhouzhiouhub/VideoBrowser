package com.example.videobrowser.utils

object WebViewEnhancerScript {
    data class Call(
        val functionName: String,
        val arguments: List<String> = emptyList()
    )

    fun call(functionName: String, vararg arguments: String): String {
        return callAll(Call(functionName, arguments.toList()))
    }

    fun callAll(vararg calls: Call): String {
        val guardedCalls = calls.joinToString(separator = "") { call ->
            "if(typeof enhancer.${call.functionName}==='function'){" +
                "enhancer.${call.functionName}(${call.arguments.joinToString()});" +
                "}"
        }
        return "(function(){var enhancer=window.VideoBrowserEnhancer;" +
            "if(!enhancer)return;" +
            guardedCalls +
            "})();"
    }
}
