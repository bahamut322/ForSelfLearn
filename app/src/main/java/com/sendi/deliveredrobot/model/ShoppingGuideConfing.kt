package com.sendi.deliveredrobot.model

/**
 * @Author Swn
 * @Data 2023/11/7
 * @describe
 */
data class ShoppingGuideConfing (
    //功能名称
    var name: String? = "业务办理",
    //首次点击提示
    var firstPrompt: String? = "",
    //任务完成提示
    var completePrompt: String? = "",
    //中断任务提示
    var interruptPrompt: String? = "",
    //时间戳
    var baseTimeStamp: Long? = 0,
//    //目标点列表
//    var actions: List<ShoppingActions?>? = null
)