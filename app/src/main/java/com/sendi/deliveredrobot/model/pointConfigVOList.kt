package com.sendi.deliveredrobot.model

/**
 * @author swn
 * @describe 目标点列表
 */
data class pointConfigVOList(
    val name : String?,
    val walkText : String? = "",
    val explanation : String? = "",
    val walkVoice : String? = "",
    val explanationVoice : String? = "",
    val scope : Int?,
    val bigScreenConfig :TopLevelConfig? = null,
    val touchScreenConfig : TopLevelConfig? = null
)
