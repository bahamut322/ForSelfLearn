package com.sendi.deliveredrobot.model

data class FileInfoListModel(
    val result: Boolean,
    val msg: String,
    val data: List<FileInfoModel>
)
