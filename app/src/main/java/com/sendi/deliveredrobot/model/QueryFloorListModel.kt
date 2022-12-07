package com.sendi.deliveredrobot.model

/**
 * @author heky
 * @date 2022-10-18
 */
data class QueryFloorListModel(val type: String = "queryFloorList"){
    override fun toString(): String {
        return """
            {"type":"$type"}
        """.trimIndent()
    }
}
