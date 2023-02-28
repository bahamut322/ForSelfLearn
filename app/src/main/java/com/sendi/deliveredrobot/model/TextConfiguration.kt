package com.sendi.deliveredrobot.model

/**
 * @author swn
 * @describe 文字配置参数
 *  "fontContent": 文字,
 *  "fontColor": '文字颜色',
 *  "fontSize": '文字大小 1-大，2-中，3-小,
 *  "fontLayout": 文字布局 1-横向，2-纵向',
 *  "fontBackGround": "背景色  白黑蓝红绿橙紫",
 *  "textPosition":文字位置 0-居中 1-居上 2-居下,
 */
data class TextConfiguration(
    val fontContent: String,
    val fontColor: String,
    val fontSize: Int,
    val fontLayout: Int ,
    val fontBackGround: String,
    val textPosition: Int
)
