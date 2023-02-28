package com.sendi.deliveredrobot.model

/**
 * @author swn
 * @describe 图片配置参数
 *  "pics": "3jpg,5.jpg,9.jpg,10.mp3",
 *  "picType": 图片布局 1-全图 2-居中
 *  "picPlayType": 图片播放方式 1-轮播,
 *  "picPlayTime": 轮播间隔X秒,
 */
data class PictureConfiguration(
    val pics: String,
    val picType: Int,
    val picPlayType: Int,
    val picPlayTime: Int
)
