package com.sendi.deliveredrobot.model

/**
 * @author heky
 * @date 2023/11/14
 * @description 机器人上传顾客语音识别文字
 */
data class QueryIntentModel(
    val questionContent: String,
    val questionNumber: Long,
    val type: String = "queryIntent"
){
    override fun toString(): String {
        return """
            {
                "questionContent": "$questionContent",
                "questionNumber": $questionNumber,
                "type": "$type"
            }
        """.trimIndent()
    }
}