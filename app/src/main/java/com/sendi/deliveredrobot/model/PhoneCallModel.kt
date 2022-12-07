package com.sendi.deliveredrobot.model

/**
 *   @author: heky
 *   @date: 2021/8/18 8:57
 *   @describe: 话务控制
 */
data class PhoneCallModel(
    val type: String = "phoneCall",
    var number: String,
    var note: String,
    var floor:String = ""
){
    override fun toString():String{
        return """
            {"type":"$type","number":"$number","note":"$note","floor":"$floor"}
        """.trimIndent()
    }
}
