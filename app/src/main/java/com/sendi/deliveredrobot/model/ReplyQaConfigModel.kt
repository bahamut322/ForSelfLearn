package com.sendi.deliveredrobot.model

data class ReplyQaConfigModel(
    val guideTexts: List<String>?,
    val timeStamp: Long? = 0,
    val type: String,
    val unknownTexts: List<String>?,
    val standardQuestions: List<String>?
)