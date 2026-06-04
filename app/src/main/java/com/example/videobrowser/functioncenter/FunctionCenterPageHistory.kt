package com.example.videobrowser.functioncenter

class FunctionCenterPageHistory<T> {
    private val pages = ArrayDeque<T>()

    fun push(page: T) {
        pages.addLast(page)
    }

    fun pop(): T? {
        return if (pages.isEmpty()) null else pages.removeLast()
    }

    fun clear() {
        pages.clear()
    }
}
