package com.sendi.deliveredrobot.entity

import org.litepal.crud.LitePalSupport

/**
 * @Author Swn
 * @Data 2023/11/7
 * @describe
 */
data class Table_Shopping_Config(
    //功能名称
    var name: String? = "业务办理",
    //首次点击提示
    var firstPrompt: String? = "欢迎进入${name},我们将引导前往目的地。",
    //任务完成提示
    var completePrompt: String? = "您已成功到达目标点，感谢使用我的服务。",
    //中断任务提示
    var interruptPrompt: String? = "${name}已中断，如需继续请重新发起请求",
    //时间戳
    var baseTimeStamp: Long? = 0,
//    //目标点列表
//    var actions: List<ShoppingActionDB?>? = null

) : LitePalSupport()
