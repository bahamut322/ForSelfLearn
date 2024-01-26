package com.sendi.deliveredrobot.model


data class ReplyIntentModel(
    val images: List<String>?,
    val questionAnswer: String?,
    val questionNumber: Long?,
    val type: String?,
    val videos: List<String>?,
    val frames: List<String>?,
    var code:Int?
)