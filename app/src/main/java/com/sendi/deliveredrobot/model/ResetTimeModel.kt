package com.sendi.deliveredrobot.model

/**
 * @Author Swn
 * @Data 2023/8/21
 * @describe
 */
data class ResetTimeModel (val type: String = "queryTimeStamp"){
    override fun toString():String{
        return """
            {"type":"$type"}
        """.trimIndent()
    }
}