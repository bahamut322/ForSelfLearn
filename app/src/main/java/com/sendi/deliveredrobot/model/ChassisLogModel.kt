package com.sendi.deliveredrobot.model

/**
 * create by yujx
 * @date 2022/02/18
 * 底盘日志上传
 */
data class ChassisLogModel (
    val type: String = "chassisLog",
    var time: String,
    var log: String
){
    constructor(time: String, log: String) : this("chassisLog",time,log) {

    }

    override fun toString():String{
        return """
            {"type":"$type","time":"$time","log":"$log"}
        """.trimIndent()
    }
}
