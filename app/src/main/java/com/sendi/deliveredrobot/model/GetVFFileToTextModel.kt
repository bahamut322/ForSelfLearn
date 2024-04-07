package com.sendi.deliveredrobot.model
data class GetVFFileToTextModel(
    val code: Int,
    val data: Data,
    val message: String
)

data class Data(
    val id: String,
    val reply: String
)