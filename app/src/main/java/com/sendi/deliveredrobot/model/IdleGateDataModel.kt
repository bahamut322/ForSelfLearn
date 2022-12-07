package com.sendi.deliveredrobot.model

data class IdleGateDataModel(
    val type: String = "idleGateData",
    val num: Int,
    val orderId: String?,
    val accept: Boolean?,
    var reportType: Int
){
    override fun toString(): String {
        return """
            {"type":"$type","num":$num,"orderId":"$orderId","accept":$accept,"reportType":$reportType}
        """.trimIndent()
    }
}