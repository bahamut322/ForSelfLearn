package com.sendi.deliveredrobot.model

/**
 * @Author Swn
 * @Data 2023/12/13
 * @describe 人脸相似度列表
 */
 data class Similarity(
    val similarity: List<Double>// 确保这里是Double类型的列表
 )