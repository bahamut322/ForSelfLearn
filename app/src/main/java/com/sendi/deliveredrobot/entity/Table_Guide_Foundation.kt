package com.sendi.deliveredrobot.entity

import org.litepal.crud.LitePalSupport

/**
 * @Author Swn
 * @Data 2023/11/14
 * @describe 讲解配置
 */
data class Table_Guide_Foundation (
    var firstPrompt : String? = "欢迎使用引领模式，我们将引导前往目的地",
    var movePrompt : String? = "请跟随指示前往目标点",
    var arrivePrompt : String? = "您已成功到达目标点，感谢使用我的服务。",
    var interruptPrompt : String? = "引领已中断，如需继续请重新发起请求。",
    //大屏配置
    var bigScreenConfig: Table_Big_Screen? = null,
    //小屏配置
    var touchScreenConfig: Table_Touch_Screen? = null,

    var timeStamp : Long? = 0
): LitePalSupport()