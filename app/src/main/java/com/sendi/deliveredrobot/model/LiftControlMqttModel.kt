package com.sendi.deliveredrobot.model

/**
 *   @author: heky
 *   @date: 2021/8/18 8:57
 *   @describe: 电梯控制
 */
data class LiftControlMqttModel(
//    val type: String = "callElevatorMove",
    val type: String = "queryElevatorMove",
    var control: Int,
//    var floorIndex: Int,
    var floorName: String,
    var time: Int,
    var elevator: String,
    var timeStamp: Long
){
    override fun toString():String{
        if(type == "getElevatorState"){
            return """
                {"type":"getElevatorState"}
            """.trimIndent()
        }
        return """
            {"type":"$type","control":$control,"floorName":"$floorName","time":$time,"elevator":"$elevator", "timeStamp":$timeStamp }
        """.trimIndent()
    }
}
