package com.sendi.deliveredrobot.model

data class ReplyQaConfigModel(
    val guideTexts: List<String>?,
    val timeStamp: String?,
    val type: String,
    val unknownTexts: List<String>?,
    val standardQuestions: List<String>?
)