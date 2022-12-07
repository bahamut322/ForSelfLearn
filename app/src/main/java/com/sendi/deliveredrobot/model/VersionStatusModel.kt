package com.sendi.deliveredrobot.model

/**
 * @author heky
 * @date 2022-04-12
 * @describe app版本信息model
 */
data class VersionStatusModel(
    val flag: Boolean = false,
    val size: Int? = 0,
    val path: String? = "",
    val version: String? = ""
)