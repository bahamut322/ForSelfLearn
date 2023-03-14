package com.sendi.deliveredrobot.model

import org.litepal.crud.LitePalSupport

/**
 * @author swn
 * @describe 表情组配置参数
 *  groupId:表情组id
 *  strangerPic:看到陌生人
 *  vipPic:看到VIP
 *  walkPic:行走中
 *  blockPic:被阻挡
 *  arrivePic:讲解中（到点）
 *  overTaskPicic:任务结束返回时
 */
data class ExpressionConfiguration(
    val groupId: Int?,
    val strangerPic: String?,
    val vipPic: String?,
    val walkPic: String?,
    val blockPic: String?,
    val arrivePic: String?,
    val overTaskPic: String?
)
