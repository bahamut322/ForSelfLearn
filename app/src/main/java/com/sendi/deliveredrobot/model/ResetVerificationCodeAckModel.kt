package com.sendi.deliveredrobot.model

/**
 * @author heky
 * @date 2022-08-18
 * @description
 */
data class ResetVerificationCodeAckModel(val type: String = "resetVerificationCodeAck"){
    override fun toString():String{
        return """
            {"type":"$type"}
        """.trimIndent()
    }
}