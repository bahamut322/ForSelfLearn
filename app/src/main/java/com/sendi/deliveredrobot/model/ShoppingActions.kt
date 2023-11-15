package com.sendi.deliveredrobot.model


/**
 * @Author Swn
 * @Data 2023/11/7
 * @describe
 */
data class ShoppingActions( //导购子功能名称
    var name: String? = "",
    //导购子功能动作类型 1：定点 2：去某点
    var actionType: Int? = 2,
    //目标点名字
    var pointName: String? = "",
    //等待时间
    var waitingTime: Int? = 20,
    //大屏配置
    var bigScreenConfig: TopLevelConfig? = null,
    //小屏配置
    var touchScreenConfig: TopLevelConfig? = null,
    //定点时的文本：当导购功能默认值为1的时候
    var standText: String? = "",
    //到达时的文本:当导购功能默认值为2的时候
    var arriveText: String? = "",
    //去某点时的文本:当导购功能默认值为2的时候
    var moveText: String? = "",
    //子功能时间戳
    var timeStamp: Long? = 0,
    //对应总图名字
    var rootMapName : String = ""

)
