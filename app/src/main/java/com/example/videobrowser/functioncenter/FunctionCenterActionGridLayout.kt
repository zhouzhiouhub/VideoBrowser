package com.example.videobrowser.functioncenter

object FunctionCenterActionGridLayout {
    private const val COLUMN_COUNT = 5

    fun rows(actionCount: Int): List<List<Int?>> {
        if (actionCount <= 0) {
            return emptyList()
        }

        return (0 until actionCount)
            .chunked(COLUMN_COUNT)
            .map { row ->
                row + List(COLUMN_COUNT - row.size) { null }
            }
    }
}
