package com.sendi.deliveredrobot.model

/**
 * @author swn
 * @describe 顶层配置类
 * screen：配置屏幕 0：小屏 1：大屏
 * type：配置类型
 * argPic：图片配置参数
 * argFont：文字配置参数
 * argVideo：视频配置参数
 * argRadio：音频配置参数
 * argPicGroup：表情组配置参数
 */
data class TopLevelConfig(
    val screen: Int?,
    val type: Int?,
    val argPic: PictureConfiguration? = null,
    val argFont: TextConfiguration? = null,
    val argVideo: VideoConfiguration? = null,
    val argRadio: String? = null,//暂无
    val argPicGroup: ExpressionConfiguration? = null
)
