package com.sendi.deliveredrobot.model

/**
 *   @author: heky
 *   @date: 2022-04-12
 *   @describe: 查询版本
 */
data class RequestVersionStatusModel(
    val type: String = "robotVersionCheck",
    var versionName: String,
    var versionCode: Int,
    var chassisVersion:String,
    var chassisVersionCode:String
){
    override fun toString():String{
        return """
            {"type":"$type","versionName":"$versionName","versionCode":$versionCode,"chassisVersion":"$chassisVersion","chassisVersionCode":"$chassisVersionCode"}
        """.trimIndent()
    }
}
