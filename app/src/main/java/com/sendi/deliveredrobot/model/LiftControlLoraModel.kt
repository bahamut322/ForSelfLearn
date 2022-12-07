package com.sendi.deliveredrobot.model


/**
 *   @author: heky
 *   @date: 2021/8/18 8:57
 *   @describe: 电梯控制
 */
data class LiftControlLoraModel(
    val type: Int = 1,
    var control: Int,
//    var floorIndex: Int,
    var floorName: String,
    var time: Int,
    var elevator: String,
    var timeStamp: Long
){
    override fun toString():String{
        return """
            {"type":"$type","control":$control,"floorName":"$floorName","time":$time,"elevator":"$elevator","timeStamp":$timeStamp}
        """.trimIndent()
    }

//    fun transToJsonObject(): JSONObject {
//        JSONObject().apply {
//            put("type",type)
//            put("control",control)
//            put("floor_index",floorIndex)
//            put("time",time)
//        }
//    }

}
