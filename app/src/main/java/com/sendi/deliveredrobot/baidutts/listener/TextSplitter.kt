package com.sendi.deliveredrobot.baidutts.listener

/**
 * @Author Swn
 * @Data 2024/1/10
 * @describe 文字分割接口
 */
interface TextSplitter {
    fun splitTextByPunctuation(text: String?): List<String>
}
