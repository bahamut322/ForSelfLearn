package com.sendi.deliveredrobot.model

/**
 * @author swn
 * @describe 顶层配置类
 * screen：配置屏幕 0：小屏 1：大屏
 * type：配置类型 1-图片、2-视频、3-音频、4-表情组、5-视频第一帧、6-文字、7-图片+文字
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
){
    companion object{
        const val TYPE_PIC = 1
        const val TYPE_VIDEO = 2
        const val TYPE_AUDIO = 3
        const val TYPE_EXPRESSION = 4
        const val TYPE_VIDEO_FIRST_FRAME = 5
        const val TYPE_TEXT = 6
        const val TYPE_PIC_TEXT = 7
    }
}
