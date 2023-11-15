package com.sendi.deliveredrobot.entity

import org.litepal.crud.LitePalSupport

/**
 * @Author Swn
 * @Data 2023/11/7
 * @describe 导购目标点列表
 */
data class ShoppingActionDB(
    //导购子功能名称
    var name: String? = "",
    //导购子功能动作类型 1：定点 2：去某点
    var actionType: Int? = 2,
    //目标点名字
    var pointName: String? = "",
    //等待时间
    var waitingTime: Int? = 20,
    //大屏配置
    var bigScreenConfig: BigScreenConfigDB? = null,
    //小屏配置
    var touchScreenConfig: TouchScreenConfigDB? = null,
    //定点时的文本：当导购功能默认值为1的时候
    var standText: String? = "请跟随指示前往目标点",
    //到达时的文本:当导购功能默认值为2的时候
    var arriveText: String? = "您已成功到达${name}，感谢使用我的服务。",
    //去某点时的文本:当导购功能默认值为2的时候
    var moveText: String? = "您好，我们将引导前往目的地。",
    //子功能时间戳
    var timestamp: Long? = 0,
    //对应总图名字
    var rootMapName : String? = ""
): LitePalSupport()