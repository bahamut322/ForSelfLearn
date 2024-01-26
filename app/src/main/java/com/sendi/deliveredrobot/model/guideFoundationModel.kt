package com.sendi.deliveredrobot.model


/**
 * @Author Swn
 * @Data 2023/11/14
 * @describe
 */
data class guideFoundationModel (
    val firstPrompt : String? = "欢迎使用引领模式，我们将引导前往目的地",
    val movePrompt : String? = "请跟随指示前往目标点",
    val arrivePrompt : String? = "您已成功到达目标点，感谢使用我的服务。",
    val interruptPrompt : String? = "引领已中断，如需继续请重新发起请求。",
    //大屏配置
    var bigScreenConfig: TopLevelConfig? = null,
    //小屏配置
    var touchScreenConfig: TopLevelConfig? = null,

    val timeStamp : Long? = 0
)