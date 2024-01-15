package com.sendi.deliveredrobot.model

import com.sendi.deliveredrobot.entity.Table_Face
import com.sendi.deliveredrobot.entity.entitySql.QuerySql

/**
 * @Author Swn
 * @Data 2024/1/11
 * @describe 迎宾配置Model
 */
data class ReplyGreetConfigModel(
    var greetPoint: String?, //迎宾点
    var firstPrompt: String? = "开启迎宾模式",//首次提示语
    var strangerPrompt: String? = "您好呀，很高兴见到你",//陌生人提示语
    var vipPrompt: String? = "您好呀，很高兴见到你哟",//VIP提示语
    var exitPrompt: String? = "迎宾结束，我要去忙其他的啦~",//退出提示语
    var timeStamp: Long? = 0,//时间戳
    var bigScreenConfig: TopLevelConfig? = null,//大屏幕
    var touchScreenConfig: TopLevelConfig? = null,//小屏幕
    var faceFeats: List<Table_Face?>? = null//人脸特征
)
