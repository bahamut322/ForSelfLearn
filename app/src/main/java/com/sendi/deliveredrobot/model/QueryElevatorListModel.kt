package com.sendi.deliveredrobot.model

/**
 * @author heky
 * @date 2022-10-18
 */
data class QueryElevatorListModel(val type: String = "queryElevatorList"){
    override fun toString(): String {
        return """
            {"type":"$type"}
        """.trimIndent()
    }
}

