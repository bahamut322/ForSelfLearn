package com.sendi.deliveredrobot.model

/**
 * @author heky
 * @date 2023-02-20
 * @description 文件信息实体类
 * 	{
 * 	    "fileId": 71,
"fileName": "送餐机器人java层架构图",
"fileType": 2,
"fileUrl": "/-1/送餐机器人java层架构图.png",
"fileSuffix": ".png"
}
 */
data class FileInfoModel(
    val fileId: Int,
    val fileName: String,
    val fileType: Int,
    val fileUrl: String,
    val fileSuffix: String
)
