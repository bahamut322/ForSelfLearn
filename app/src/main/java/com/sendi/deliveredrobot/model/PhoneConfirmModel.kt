package com.sendi.deliveredrobot.model

/**
 *   @author: heky
 *   @date: 2021/8/18 8:57
 *   @describe: 话务确认（停止继续拨号）
 */
data class PhoneConfirmModel(
    val type: String = "phoneConfirm",
    var number: String
){
    override fun toString():String{
        return """
            {"type":"$type","number":"$number"}
        """.trimIndent()
    }
}
