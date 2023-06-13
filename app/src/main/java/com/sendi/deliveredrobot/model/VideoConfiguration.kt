package com.sendi.deliveredrobot.model

/**
 * @author swn
 * @describe 视频配置参数
 *  `videos`   text '文件名称',
 *  `videoFrame` (255) '视频第一帧关联图片ID',
 *  `videoAudio` tinyint(1) '视频是否播放声音',
 */
data class VideoConfiguration(
    val videos: String,
    val videoAudio: Int? = 1 ,
    val videoFrame: String?,
    val videoPlayType : Int? = 1,
    val videoLayOut : Int? = 0
)
