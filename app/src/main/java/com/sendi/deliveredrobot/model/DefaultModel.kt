package com.sendi.deliveredrobot.model

/**
 * @Author Swn
 * @Data 2023-07-27
 * @describe 默认样式
 */
data class DefaultModel @JvmOverloads constructor(
    var picPlayTime: Int? = 4,
    var file: String? = "",
    var type: Int? = 1,
    var textPosition: Int? = 0,
    var fontLayout: Int? = 0,
    var fontContent: String? = "",
    var fontBackGround: String? ="#FFFFFFFF",
    var fontColor: String? = "#FFFFFFFF",
    var fontSize: Int? = 1,
    var picType: Int? = 2,
    var videolayout : Int? = 0,
    var videoAudio : Int? = 0
)
